package l2r.gameserver.model.instances;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.network.serverpackets.NpcInfo;
import l2r.gameserver.templates.npc.NpcTemplate;

public class BlockInstance extends NpcInstance
{
	private boolean _isRed;
	
	public BlockInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setShowName(false);
	}
	
	public boolean isRed()
	{
		return _isRed;
	}
	
	public void setRed(boolean red)
	{
		_isRed = red;
		broadcastCharInfo();
	}
	
	public void changeColor()
	{
		setRed(!_isRed);
	}
	
	public void changeColor(Player attacker)
	{
		synchronized (this)
		{
			if (_isRed)
			{
				_isRed = false;
				setColor(0x00);
				broadcastPacket(new NpcInfo(this, attacker));
			}
			else
			{
				_isRed = true;
				setColor(0x53);
				broadcastPacket(new NpcInfo(this, attacker));
			}
		}
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		if (!attacker.isPlayer())
			return false;
		
		if (attacker.getPlayer().getTeam() == TeamType.BLUE && !isRed())
			return true;
		
		if (attacker.getPlayer().getTeam() == TeamType.RED && isRed())
			return true;
		return false;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (!attacker.isPlayer())
			return false;
		
		if (attacker.getPlayer().getTeam() == TeamType.BLUE && !isRed())
			return true;
		
		if (attacker.getPlayer().getTeam() == TeamType.RED && isRed())
			return true;
		
		return false;
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
	}
	
	@Override
	public boolean isNameAbove()
	{
		return false;
	}
	
	@Override
	public int getFormId()
	{
		return _isRed ? 0x53 : 0;
	}
	
	@Override
	public boolean isInvul()
	{
		return true;
	}
}
