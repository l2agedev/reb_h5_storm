/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PledgeCrest;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.randoms.CaptchaImage;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.Log;

import gov.nasa.worldwind.formats.dds.DDSConverter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Captcha implements IVoicedCommandHandler
{
	public static final Logger _log = LoggerFactory.getLogger(Captcha.class);
	
	private static final String[] _voicedCommands =
	{
		"entercaptcha",
		"requestnewcaptcha"
	};
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if (!Config.ENABLE_CAPTCHA)
			return false;
		
		int playerServerId = activeChar.getServerId();
		
		if (command.equalsIgnoreCase("entercaptcha"))
		{	

			if (args.isEmpty() || args == null)
			{
				if (activeChar.antiFlood.canRequestCaptcha())
				{
					CaptchaImage.reduceCaptchaTries(activeChar);
					CaptchaImage.requestAnotherCaptcha(activeChar);
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "Captcha", "Cannot send empty box... type the code from image.");
				}
				else
				{
					String customHtm = HtmCache.getInstance().getNotNull("mods/Captcha/WaitForNewCaptcha.htm", activeChar);
					NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setHtml(customHtm);
					html.replace("%playerName%", "" + activeChar.getName());
					html.replace("%tries%", "" + CaptchaImage.getCaptchaTries(activeChar));
					html.replace("%serverId%", "" + playerServerId);
					html.replace("%punishmentType%", "" + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE);
					html.replace("%punishmentTime%", "" + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME);
					activeChar.sendPacket(html);
					
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "Captcha", "AntiFlood, please wait before request new captcha.");
				}
				
				return false;
			}
			
			StringTokenizer st = new StringTokenizer(args);
			try
			{
				String enteredcode = null;
				String actualcode = String.valueOf(CaptchaImage.getCaptchaCode(activeChar));
					
				if (st.hasMoreTokens())
					enteredcode = st.nextToken();
				
				if (enteredcode != null && actualcode != null)
				{
					if (enteredcode.equalsIgnoreCase(actualcode)) // code confirmed
					{
						activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
						activeChar.setIsInvul(false);
						activeChar.stopParalyzed();
						activeChar.unblock();
						CaptchaImage.resetCaptcha(activeChar);
						CaptchaImage.endPunishTask();
						CaptchaImage.endMessageTask();
						
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "Captcha", "Code has been confirmed, happy farming :)");
						Log.bots("Captcha: " + activeChar.getName() + " successfuly confirmed the code.");
						
						return false;
					}
					
				}
				if (enteredcode == null || !enteredcode.equals(actualcode)) // wrong code...
				{
					if (CaptchaImage.getCaptchaTries(activeChar) > 1)
					{
						String htmContent = HtmCache.getInstance().getNotNull("mods/Captcha/WrongCode.htm", activeChar);
						
						CaptchaImage.reduceCaptchaTries(activeChar);
						CaptchaImage.newCaptchaCode(activeChar);
						
						//Random image file name
						int imgId = IdFactory.getInstance().getNextId();
						
						// Convertion from .png to .dds, and crest packed send
						try
						{
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(CaptchaImage.generateCaptcha(CaptchaImage.getCaptchaCode(activeChar)), "png", baos);
							baos.flush();
							ByteBuffer buffer = DDSConverter.convertToDDS(ByteBuffer.wrap(baos.toByteArray()), "lol/png");
							activeChar.sendPacket(new PledgeCrest(imgId, buffer.array()));
							baos.close();
						}
						catch (Exception e)
						{
							_log.warn("", e);
						}
						
						CaptchaImage.endPunishTask();
						CaptchaImage.endMessageTask();
						CaptchaImage.starTasksPunishment(activeChar);
						CaptchaImage.starTasksMessage(activeChar);
						
						NpcHtmlMessage html = new NpcHtmlMessage(0);
						html.setHtml(htmContent);
						html.replace("%imgId%", "" + imgId);
						html.replace("%serverId%", "" + playerServerId);
						html.replace("%playerName%", "" + activeChar.getName());
						html.replace("%tries%", "" + CaptchaImage.getCaptchaTries(activeChar));
						html.replace("%punishmentType%", "" + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE);
						html.replace("%punishmentTime%", "" + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME);
						activeChar.sendPacket(html);
						
						return false;
					}
					else
					{
						// here will run method with jailing player
						String customHtm = HtmCache.getInstance().getNotNull("mods/Captcha/NoMoreAttempts.htm", activeChar);
						
						activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
						
						if (activeChar.isFlying())
							activeChar.setTransformation(0);
						
						activeChar.setIsInvul(false);
						activeChar.stopParalyzed();
						activeChar.unblock();
						
						CaptchaImage.endPunishTask();
						CaptchaImage.endMessageTask();
						
						CaptchaImage.resetCaptcha(activeChar);
						
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
						npcHtmlMessage.setHtml(customHtm);
						activeChar.sendPacket(npcHtmlMessage);
						
						_log.info("Player " + activeChar.getName() + " does not have anymore attemts for captcha and has been " + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE);
						
						Log.bots("Captcha: " + activeChar.getName() + " failed on captcha-test [NO MORE CAPTCHA TRIES] and will recive punishment: " + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE);
							
						switch(Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE)
						{
							case "KICK":
							{
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.captcha.message1", activeChar));
								activeChar.kick();
								break;
							}
							case "BANCHAR":
							{
								int punishTime = Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME;
								if (punishTime != 0 && activeChar != null)
								{
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.captcha.message2", activeChar));
									AutoBan.Banned(activeChar.getName(), -100, punishTime, "Failed on Captcha", activeChar.getName());
									activeChar.kick();
								}
								else
									_log.warn("Captcha System: Something went wrong, player is null or time is null");
								break;
							}
							case "JAIL":
							{
								int punishTime = Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME;
								if (punishTime != 0 && activeChar != null)
								{
									
									AutoBan.Jail(activeChar.getName(), punishTime, "Failed on Captcha", null);
								}
								else
									_log.warn("Captcha System: Something went wrong, player is null or time is null");
								break;
							}
						}
						
						return false;
					}
				}
				
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}
		}
		else if (command.equalsIgnoreCase("requestnewcaptcha"))
		{
			if (activeChar != null)
			{
				if (activeChar.antiFlood.canRequestCaptcha())
				{
					if (CaptchaImage.getCaptchaRequests(activeChar) == 5)
					{
						CaptchaImage.requestAnotherCaptcha(activeChar);
						
						activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "Captcha", "You have reached the limit to request new captcha on next failed captcha you will be kicked.");
						return false;
					}
					else if (CaptchaImage.getCaptchaRequests(activeChar) >= 6)
					{
						activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
						
						if (activeChar.isFlying())
							activeChar.setTransformation(0);
						
						activeChar.setIsInvul(false);
						activeChar.stopParalyzed();
						activeChar.unblock();
						
						CaptchaImage.endPunishTask();
						CaptchaImage.endMessageTask();
						
						CaptchaImage.resetCaptcha(activeChar);
						
						Log.bots("Captcha: " + activeChar.getName() + " failed on captcha-test [TOO MANY CAPTCHA REQUESTS] and will recive punishment: " + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE);
						
						switch(Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE)
						{
							case "KICK":
							{
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.captcha.message3", activeChar));
								activeChar.kick();
								break;
							}
							case "BANCHAR":
							{
								int punishTime = Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME;
								if (punishTime != 0 && activeChar != null)
								{
									activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.voicecommands.impl.captcha.message4", activeChar));
									AutoBan.Banned(activeChar.getName(), -100, punishTime, "Failed on Captcha", activeChar.getName());
									activeChar.kick();
								}
								else
									_log.warn("Captcha System: Something went wrong, player is null or time is null");
								break;
							}
							case "JAIL":
							{
								int punishTime = Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME;
								if (punishTime != 0 && activeChar != null)
								{
									
									AutoBan.Jail(activeChar.getName(), punishTime, "Failed on Captcha", null);
								}
								else
									_log.warn("Captcha System: Something went wrong, player is null or time is null");
								break;
							}
						}
					}
					else
						CaptchaImage.requestAnotherCaptcha(activeChar);	
				}
				else
				{
					String customHtm = HtmCache.getInstance().getNotNull("mods/Captcha/WaitForNewCaptcha.htm", activeChar);
					NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setHtml(customHtm);
					html.replace("%playerName%", "" + activeChar.getName());
					html.replace("%tries%", "" + CaptchaImage.getCaptchaTries(activeChar));
					html.replace("%serverId%", "" + playerServerId);
					html.replace("%punishmentType%", "" + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TYPE);
					html.replace("%punishmentTime%", "" + Config.CAPTCHA_FAILED_CAPTCHA_PUNISHMENT_TIME);
					activeChar.sendPacket(html);
					
					activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "Captcha", "AntiFlood, please wait before request new captcha.");
				}
			}
		}
		
		return false;
	}
}
