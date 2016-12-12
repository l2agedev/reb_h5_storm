package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.GameClient.GameClientState;
import l2r.gameserver.network.serverpackets.ActionFail;
import l2r.gameserver.network.serverpackets.CharSelected;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.TimeUtils;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;

	/**
	 * Format: cdhddd
	 */
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();

		if (Config.SECOND_AUTH_ENABLED && !client.getSecondaryAuth().isAuthed())
		{
			client.getSecondaryAuth().openDialog();
			return;
		}
		
		if(client.getActiveChar() != null)
			return;

		Player activeChar = client.loadCharFromDisk(_charSlot);
		if(activeChar == null)
		{
			sendPacket(ActionFail.STATIC);
			return;
		}
		
		int objId = client.getObjectIdForSlot(_charSlot);
		if(AutoBan.isBanned(objId))
		{
			if (Config.SHOW_BAN_INFO_IN_CHARACTER_SELECT)
			{
				String htmlban = HtmCache.getInstance().getNotNull("baninfo.htm", activeChar);
				
				String bannedby = AutoBan.getBannedBy(objId);
				if (bannedby.isEmpty() || bannedby == null)
					bannedby = "Missing Data";
				
				String reason = AutoBan.getBanReason(objId);
				if (reason.isEmpty() || reason == null)
					reason = "Missing Reason";
				
				String enddate = TimeUtils.convertDateToString(AutoBan.getEndBanDate(objId) * 1000);
				if (enddate.isEmpty() || enddate == null)
					enddate = "Bad Date";
				
				NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setHtml(htmlban);
				html.replace("%bannedby%", bannedby);
				html.replace("%endDate%", enddate);
				html.replace("%reason%", reason);
				activeChar.sendPacket(html);
			}
			
			sendPacket(ActionFail.STATIC);
			return;
		}

		if(activeChar.getAccessLevel().getLevel() < 0)
			activeChar.setAccessLevel(0);
		
		activeChar.setServerId(CharacterDAO.getInstance().getLastServerId(activeChar.getAccountName()));
		
		client.setState(GameClientState.ENTER_GAME);

		sendPacket(new CharSelected(activeChar, client.getSessionKey().playOkID1));
	}
}