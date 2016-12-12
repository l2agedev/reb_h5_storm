package l2r.gameserver.model.instances;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectTasks;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MyTargetSelected;
import l2r.gameserver.network.serverpackets.NpcInfo;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.taskmanager.EffectTaskManager;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Location;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public final class TrapInstance extends NpcInstance
{
	private static class CastTask extends RunnableImpl
	{
		private HardReference<NpcInstance> _trapRef;

		public CastTask(TrapInstance trap)
		{
			_trapRef = trap.getRef();
		}

		@Override
		public void runImpl() throws Exception
		{
			TrapInstance trap = (TrapInstance)_trapRef.get();

			if(trap == null)
				return;

			Creature owner =  trap.getPlayer();
			if(owner == null)
				return;

			for(Creature target : trap.getAroundCharacters(200, 200))
			{
				if(target != owner && trap._skill.checkTarget(owner, target, null, false, false) == null)
				{
					List<Creature> targets = trap._skill.getTargets(trap, trap, false);
					targets.remove(trap);
					//trap._skill.useSkill(trap, targets);
					trap.callSkill(trap._skill, targets, true);
					
					if(target.isPlayer())
						target.sendMessage(new CustomMessage("common.Trap", target.getPlayer()));
					
					trap.decayMe(); // trap.deleteMe(); In retail traps decay, not instantly removed.
					break;
				}
			}
		}
	}
	
	private final HardReference<? extends Creature> _ownerRef;
	private final Skill _skill;
	private ScheduledFuture<?> _targetTask;
	private ScheduledFuture<?> _destroyTask;
	private boolean _detected;

	public TrapInstance(int objectId, NpcTemplate template, Creature owner, Skill skill)
	{
		this(objectId, template, owner, skill, owner.getLoc());
	}

	public TrapInstance(int objectId, NpcTemplate template, Creature owner, Skill skill, Location loc)
	{
		super(objectId, template);
		_ownerRef = owner.getRef();
		_skill = skill;

		setReflection(owner.getReflection());
		setLevel(owner.getLevel());
		setTitle(owner.getName());
		setLoc(loc);
	}

	@Override
	public boolean isTrap()
	{
		return true;
	}

	@Override
	public Player getPlayer()
	{
		if (_ownerRef.get() == null)
			return null;
		
		return _ownerRef.get().getPlayer();
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_destroyTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), 120000L);
		_targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new CastTask(this), 250L, 250L);
	}

	@Override
	public void broadcastCharInfo()
	{
		if (!isDetected())
			return;
		super.broadcastCharInfo();
	}

	@Override
	protected void onDelete()
	{
		Player owner = getPlayer();
		if(owner != null)
			owner.removeTrap(this);
		
		if(_destroyTask != null)
			_destroyTask.cancel(false);
		_destroyTask = null;
		
		if(_targetTask != null)
			_targetTask.cancel(false);
		_targetTask = null;
		
		super.onDelete();
	}

	public boolean isDetected()
	{
		return _detected;
	}

	public void setDetected(boolean detected)
	{
		_detected = detected;
	}

	@Override
	public int getPAtk(Creature target)
	{
		Creature owner = getPlayer();
		return owner == null ? 0 : owner.getPAtk(target);
	}

	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		Creature owner = getPlayer();
		return owner == null ? 0 : owner.getMAtk(target, skill);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{}

	@Override
	public void showChatWindow(Player player, String filename, Object... replace)
	{}

	@Override
	public void onBypassFeedback(Player player, String command)
	{}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
		}
		player.sendActionFailed();
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer)
	{
		// если не обезврежена и не овнер, ниче не показываем
		if(!isDetected() && getPlayer() != forPlayer)
			return Collections.emptyList();

		return Collections.<L2GameServerPacket>singletonList(new NpcInfo(this, forPlayer));
	}
}