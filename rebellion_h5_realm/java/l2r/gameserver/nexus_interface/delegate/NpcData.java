package l2r.gameserver.nexus_interface.delegate;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.World;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcInfo;
import l2r.gameserver.nexus_engine.l2r.delegate.INpcData;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.tables.SkillTable;

import java.util.List;

import javolution.util.FastTable;

/**
 * @author hNoke
 *
 */
public class NpcData extends CharacterData implements INpcData
{
	private int _team;
	private boolean deleted = false;
	
	public NpcData(NpcInstance npc)
	{
		super(npc);
	}
	
	@Override
	public void deleteMe()
	{
		if(!deleted)
			((NpcInstance)_owner).deleteMe();
		
		deleted = true;
	}
	
	@Override
	public ObjectData getObjectData()
	{
		return new ObjectData(_owner);
	}
	
	@Override
	public void setName(String name)
	{
		_owner.setName(name);
	}
	
	@Override
	public void setTitle(String t)
	{
		_owner.setTitle(t);
	}
	
	@Override
	public int getNpcId()
	{
		return ((NpcInstance) _owner).getNpcId();
	}
	
	@Override
	public void setEventTeam(int team)
	{
		_team = team;
	}
	
	@Override
	public int getEventTeam()
	{
		return _team;
	}
	
	@Override
	public void broadcastNpcInfo()
	{
		for(Player player : World.getAroundPlayers(((NpcInstance) _owner)))
			player.sendPacket(new NpcInfo((NpcInstance) _owner, player));
	}
	
	@Override
	public void broadcastSkillUse(CharacterData owner, CharacterData target, int skillId, int level)
	{
		Skill skill = SkillTable.getInstance().getInfo(skillId, level);
		
		if (skill != null)
			getOwner().broadcastPacket(new MagicSkillUse(owner.getOwner(), target.getOwner(), skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
	}

	public List<PlayerEventInfo> getAroundPlayers(int range)
	{
		List<PlayerEventInfo> players = new FastTable<PlayerEventInfo>();
		for(Player player : World.getAroundPlayers((NpcInstance) _owner, range, 400)) // confirm height
		{
			if (player == null || player.getEventInfo() == null)
				continue;
			
			players.add(player.getEventInfo());
		}
		
		return players;
	}
	
	// Infern0 disable this for now
	/*
	public void setGlobalEvent(GlobalEvent event)
	{
		if(_owner != null)
			((NpcInstance )_owner).setGlobalEvent(event);
	}
	*/
}
