package l2r.gameserver.network.serverpackets;

import l2r.gameserver.dao.MailDAO;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.clientpackets.RequestExDeleteReceivedPost;
import l2r.gameserver.network.clientpackets.RequestExPostItemList;
import l2r.gameserver.network.clientpackets.RequestExRequestReceivedPost;
import l2r.gameserver.network.clientpackets.RequestExRequestReceivedPostList;

import java.util.Collections;
import java.util.List;


/**
 * Появляется при нажатии на кнопку "почта" или "received mail", входящие письма
 * <br> Ответ на {@link RequestExRequestReceivedPostList}.
 * <br> При нажатии на письмо в списке шлется {@link RequestExRequestReceivedPost} а в ответ {@link ExReplyReceivedPost}.
 * <br> При попытке удалить письмо шлется {@link RequestExDeleteReceivedPost}.
 * <br> При нажатии кнопки send mail шлется {@link RequestExPostItemList}.
 * @see ExShowSentPostList аналогичный список отправленной почты
 */
public class ExShowReceivedPostList extends L2GameServerPacket
{
	private final List<Mail> mails;

	public ExShowReceivedPostList(Player cha)
	{
		mails = MailDAO.getInstance().getReceivedMailByOwnerId(cha.getObjectId());
		Collections.sort(mails);
	}

	// d dx[dSSddddddd]
	@Override
	protected void writeImpl()
	{
		writeEx(0xAA);
		writeD((int)(System.currentTimeMillis() / 1000L));
		writeD(mails.size()); // количество писем
		for(Mail mail : mails)
		{
			writeD(mail.getMessageId()); // уникальный id письма
			writeS(mail.getTopic()); // топик
			writeS(mail.getSenderName()); // отправитель
			writeD(mail.isPayOnDelivery() ? 1 : 0); // если тут 1 то письмо требует оплаты
			writeD(mail.getExpireTime()); // время действительности письма
			writeD(mail.isUnread() ? 1: 0); // письмо не прочитано - его нельзя удалить и оно выделяется ярким цветом
			writeD(mail.getType() == Mail.SenderType.NORMAL ? 0 : 1); // returnable
			writeD(mail.getAttachments().isEmpty() ? 0 : 1); // 1 - письмо с приложением, 0 - просто письмо
			writeD(mail.isReturned() ? 1 : 0);
			writeD(mail.getType().ordinal()); // 1 - отправителем значится "**News Informer**"
			writeD(0x00);
		}
	}
}