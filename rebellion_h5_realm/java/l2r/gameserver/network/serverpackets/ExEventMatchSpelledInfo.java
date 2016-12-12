package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;


public class ExEventMatchSpelledInfo extends L2GameServerPacket
{
	// chdd(dhd)
	private Player _player;

	public ExEventMatchSpelledInfo(Player player)
	{
		_player = player;
	}

	@Override
	protected void writeImpl()
	{
		Effect[] effects = _player.getEffectList().getAllFirstEffects();
		writeEx(0x04);
		writeD(_player.getObjectId());
		writeD(effects.length);
		for(Effect eff : effects)
		{
			writeD(eff.getSkill().getId());
			writeH(eff.getSkill().getLevel());
			writeD((int)eff.getDuration());
		}
	}
}