package l2r.gameserver.taskmanager;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.threading.SteppingRunnableQueueManager;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.dao.AccountBonusDAO;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.actor.instances.player.Bonus;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.ExBR_PremiumState;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

import java.util.concurrent.Future;

public class LazyPrecisionTaskManager extends SteppingRunnableQueueManager
{
	private static final LazyPrecisionTaskManager _instance = new LazyPrecisionTaskManager();

	public static final LazyPrecisionTaskManager getInstance()
	{
		return _instance;
	}

	private LazyPrecisionTaskManager()
	{
		super(1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		//Очистка каждые 60 секунд
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				LazyPrecisionTaskManager.this.purge();
			}

		}, 60000L, 60000L);
	}

	public Future<?> addPCCafePointsTask(final Player player)
	{
		long delay = Config.PCBANG_POINTS_DELAY * 60000L;

		return scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				if(player.isInOfflineMode() || player.getLevel() < Config.PCBANG_POINTS_MIN_LVL || player.isInJail())
					return;

				player.addPcBangPoints(Config.PCBANG_POINTS_BONUS, Config.PCBANG_POINTS_BONUS_DOUBLE_CHANCE > 0 && Rnd.chance(Config.PCBANG_POINTS_BONUS_DOUBLE_CHANCE));
			}

		}, delay, delay);
	}

	public Future<?> addVitalityRegenTask(final Player player)
	{
		long delay = 60000L;

		return scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				if(player.isInOfflineMode() || !player.isInPeaceZone() || player.isInJail())
					return;

				player.setVitality(player.getVitality() + 1); // одно очко раз в минуту
			}

		}, delay, delay);
	}

	public Future<?> startBonusExpirationTask(final Player player)
	{
		long delay = player.getBonus().getBonusExpire() * 1000L - System.currentTimeMillis();

		return schedule(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				player.getBonus().setBonusExpire(0);
				
				player.getBonus().setRateXp(1.);
				player.getBonus().setRateSp(1.);
				player.getBonus().setDropAdena(1.);
				player.getBonus().setDropItems(1.);
				player.getBonus().setDropSpoil(1.);
				
				player.getBonus().setQuestDropRate(1.);
				player.getBonus().setQuestRewardRate(1.);

				if(player.getParty() != null)
					player.getParty().recalculatePartyData();

				String msg = new CustomMessage("scripts.services.RateBonus.LuckEnded", player).toString();
				player.sendPacket(new ExShowScreenMessage(msg, 10000, ScreenMessageAlign.TOP_CENTER, true), new ExBR_PremiumState(player, false));
				player.sendMessage(msg);

				if(Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_GAMESERVER)
					AccountBonusDAO.getInstance().delete(player.getAccountName());
			}

		}, delay);
	}

	public Future<?> addNpcAnimationTask(final NpcInstance npc)
	{
		return scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				if(npc.isVisible() && !npc.isActionsDisabled() && !npc.isMoving() && !npc.isInCombat())
					npc.onRandomAnimation();
			}

		}, 1000L, Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION) * 1000L);
	}
}
