package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.dao.AccountsDAO;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.AccountData;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.gspackets.AccountDataRequest;
import l2r.gameserver.network.loginservercon.gspackets.ChangeAllowedHwid;
import l2r.gameserver.network.loginservercon.gspackets.ChangeAllowedIp;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

public class Security implements IVoicedCommandHandler
{

	private String[] _commandList = { "lock", "unlock", "lockIp", "lockHwid", "unlockIp", "unlockHwid" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (!Config.ALLOW_LOCK_COMMAND)
			return false;
		
		if(command.equalsIgnoreCase("lock"))
		{
			AccountData data =  AccountsDAO.getAccountData(activeChar.getAccountName());
			
			String hwidbutton = "";
			String ipbutton = "";
			
			if (data != null)
			{
				if (data.allowedHwids.isEmpty())
					hwidbutton = "<button width=80 height=20 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF action=\"bypass -h user_lockHwid\" value=\"Lock Hwid\">";
				else
					hwidbutton = "<button width=80 height=20 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF action=\"bypass -h user_unlockHwid\" value=\"Unlock Hwid\">";
				if (!Config.ALLOW_HWID_LOCK)
					hwidbutton = "&nbsp;";
				
				if (data.allowedIps.isEmpty())
					ipbutton = "<button width=80 height=20 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF action=\"bypass -h user_lockIp\" value=\"Lock IP\">";
				else
					ipbutton = "<button width=80 height=20 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF action=\"bypass -h user_unlockIp\" value=\"Unlock IP\">";
				if (!Config.ALLOW_IP_LOCK)
					ipbutton = "&nbsp;";
			}
			
			NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
			html.setFile("command/lock/lock.htm");
			html.replace("%ip_block%", IpBlockStatus());
			html.replace("%hwid_block%", HwidBlockStatus());
			html.replace("%hwid_val%", HwidBlockBy());
			html.replace("%curIP%", activeChar.getIP());
			
			html.replace("%hwidlockstatus%", hwidbutton);
			html.replace("%iplockstatus%", ipbutton);
			
			activeChar.sendPacket(html);
			return true;
		}

		else if(command.equalsIgnoreCase("lockIp"))
		{
			if(!Config.ALLOW_IP_LOCK)
				return true;

			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), activeChar.getIP()));
			AuthServerCommunication.getInstance().sendPacket(new AccountDataRequest(activeChar.getAccountName()));
			
			NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
			html.setFile("command/lock/lock_ip.htm");
			html.replace("%curIP%", activeChar.getIP());
			activeChar.sendPacket(html);
			return true;
		}

		else if(command.equalsIgnoreCase("lockHwid"))
		{

			if(!Config.ALLOW_HWID_LOCK)
				return false;
			
			if (!activeChar.hasHWID())
				return false;
			
			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), activeChar.getHWID()));
			AuthServerCommunication.getInstance().sendPacket(new AccountDataRequest(activeChar.getAccountName()));
			NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
			html.setFile("command/lock/lock_hwid.htm");
			activeChar.sendPacket(html);

			return true;
		}

		else if(command.equalsIgnoreCase("unlockIp"))
		{
			if(!Config.ALLOW_IP_LOCK)
				return true;
			
			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), ""));
			AuthServerCommunication.getInstance().sendPacket(new AccountDataRequest(activeChar.getAccountName()));

			NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
			html.setFile("command/lock/unlock_ip.htm");
			html.replace("%curIP", activeChar.getIP());
			activeChar.sendPacket(html);
			return true;
		}

		else if(command.equalsIgnoreCase("unlockHwid"))
		{
			if(!Config.ALLOW_HWID_LOCK)
				return true;
			
			AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), ""));
			AuthServerCommunication.getInstance().sendPacket(new AccountDataRequest(activeChar.getAccountName()));

			NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
			html.setFile("command/lock/unlock_hwid.htm");
			activeChar.sendPacket(html);

			return true;
		}

		return true;
	}

	private String IpBlockStatus()
	{
		if(Config.ALLOW_IP_LOCK)
			return "Allowed";
		else
			return "Disabled";
	}

	private String HwidBlockStatus()
	{
		if(Config.ALLOW_HWID_LOCK)
			return "Allowed";
		return "Disabled";
	}

	private String HwidBlockBy()
	{
		String result = "(CPU/HDD)";

		switch(Config.HWID_LOCK_MASK)
		{
			case 2:
				result = "(HDD)";
				break;
			case 4:
				result = "(BIOS)";
				break;
			case 6:
				result = "(BIOS/HDD)";
				break;
			case 8:
				result = "(CPU)";
				break;
			case 10:
				result = "(CPU/HDD)";
				break;
			case 12:
				result = "(CPU/BIOS)";
				break;
			case 14:
				result = "(CPU/HDD/BIOS)";
				break;
			case 1:
			case 3:
			case 5:
			case 7:
			case 9:
			case 11:
			case 13:
			default:
				result = "(unknown)";

		}
		return result;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}