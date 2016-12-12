package l2r.gameserver.model.instances;

import l2r.commons.threading.RunnableImpl;
import l2r.commons.util.Rnd;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectTasks;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.taskmanager.EffectTaskManager;
import l2r.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class SymbolInstance extends NpcInstance
{
	private final Creature _owner;
	private final Skill _skill;
	private final int _durationInMilis;
	private ScheduledFuture<?> _targetTask;
	private ScheduledFuture<?> _destroyTask;

	public SymbolInstance(int objectId, NpcTemplate template, Creature owner, Skill skill, int durationInMilis)
	{
		super(objectId, template);
		_owner = owner;
		_skill = skill;
		_durationInMilis = durationInMilis;

		setReflection(owner.getReflection());
		setLevel(owner.getLevel());
		setTitle(owner.getName());
	}

	@Override
	public Player getPlayer()
	{
		return _owner != null ? _owner.getPlayer() : null;
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		_destroyTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), _durationInMilis);

		_targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new RunnableImpl(){

			@Override
			public void runImpl() throws Exception
			{
				for(Creature target : getAroundCharacters(200, 200))
					if(_skill.checkTarget(SymbolInstance.this, target, null, false, false) == null)
					{
						List<Creature> targets = new ArrayList<Creature>();

						if(!_skill.isAoE() && _skill.checkTargetSkill(target, _owner, false))
							targets.add(target);
						else
							for(Creature t : getAroundCharacters(_skill.getSkillRadius(), 128))
								if((_skill.checkTarget(SymbolInstance.this, t, null, false, false) == null) && _skill.checkTargetSkill(target, _owner, false))
									targets.add(target);

						_skill.useSkill(SymbolInstance.this, targets);
					}
			}
		}, 1000L, Rnd.get(4000L, 7000L));
	}

	@Override
	protected void onDelete()
	{
		if(_destroyTask != null)
			_destroyTask.cancel(false);
		_destroyTask = null;
		if(_targetTask != null)
			_targetTask.cancel(false);
		_targetTask = null;
		super.onDelete();
	}

	@Override
	public int getPAtk(Creature target)
	{
		return _owner == null ? 0 : _owner.getPAtk(target);
	}

	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		return _owner == null ? 0 : _owner.getMAtk(target, skill);
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
		player.sendActionFailed();
	}

	@Override
	public boolean isTargetable()
	{
		return false;
	}
	
	@Override
	public Clan getClan()
	{
		return null;
	}
}
