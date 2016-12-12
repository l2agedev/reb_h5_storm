package l2r.gameserver.network.clientpackets;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.Config;
import l2r.gameserver.dao.MailDAO;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ExChangePostState;
import l2r.gameserver.network.serverpackets.ExReplyReceivedPost;
import l2r.gameserver.network.serverpackets.ExShowReceivedPostList;
import l2r.gameserver.network.serverpackets.components.ChatType;

/**
 * Запрос информации об полученном письме. Появляется при нажатии на письмо из списка {@link ExShowReceivedPostList}.
 * @see RequestExRequestSentPost
 */
public class RequestExRequestReceivedPost extends L2GameClientPacket
{
	private int postId;

	/**
	 * format: d
	 */
	@Override
	protected void readImpl()
	{
		postId = readD(); // id письма
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if (Config.SECURITY_ENABLED && Config.SECURITY_READ_OWN_MAILS && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return;
		}
		
		Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			if(mail.isUnread())
			{
				mail.setUnread(false);
				mail.setJdbcState(JdbcEntityState.UPDATED);
				mail.update();
				activeChar.sendPacket(new ExChangePostState(true, Mail.READED, mail));
			}

			activeChar.sendPacket(new ExReplyReceivedPost(mail));
			return;
		}

		activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}