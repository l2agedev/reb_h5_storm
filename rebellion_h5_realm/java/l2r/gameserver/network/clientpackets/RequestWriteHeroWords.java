package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.network.serverpackets.components.ChatType;

/**
 * Format chS
 * c (id) 0xD0
 * h (subid) 0x0C
 * S the hero's words :)
 *
 */
public class RequestWriteHeroWords extends L2GameClientPacket
{
	private String _heroWords;

	@Override
	protected void readImpl()
	{
		_heroWords = readS();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null || !player.isHero())
			return;

		if(_heroWords == null || _heroWords.length() > 300)
			return;

		if (Config.SECURITY_ENABLED && Config.SECURITY_HERO_HEROVOICE && player.getSecurity())
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (player.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		Hero.getInstance().setHeroMessage(player.getObjectId(), _heroWords);
	}
}