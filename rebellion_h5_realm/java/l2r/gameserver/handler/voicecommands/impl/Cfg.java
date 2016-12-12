package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ClientSetTime;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.utils.Language;

import java.util.ArrayList;
import java.util.List;

public class Cfg extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "cfg" };

	@SuppressWarnings("unused")
	private static List<String> _params = new ArrayList<String>();
	@SuppressWarnings("unused")
	private static boolean paramLoaded;
	
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if (!Config.ENABLE_CFG_COMMAND)
			return false;
		
		if (Config.SECURITY_ENABLED && Config.SECURITY_CFG_ENABLED && activeChar.getSecurity())
		{
			activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "SECURITY", (activeChar.isLangRus() ? "Для того, чтобы это сделать, идентифицировать себя с помощью .security" : "In order to do this, identify yourself via .security"));
			return false;
		}
		
		/*
		if(!paramLoaded)
		{
			_params.add("DroplistIcons");
			_params.add("PlayerKillSystem");
			_params.add("findparty");
			_params.add("NoExp");
			_params.add(Player.NO_TRADERS_VAR);
			_params.add("noShift");
			_params.add(Player.NO_ANIMATION_OF_CAST_VAR);
			_params.add("SkillsHideChance");
			_params.add("EnchantAnimationDisable");
			_params.add("noCarrier");
			_params.add("emotions");
			
			paramLoaded = true;
		}
		*/
		
		if(command.equals("cfg"))
		{
			if(args != null)
			{
				String[] param = args.split(" ");
				if(param.length == 2)
				{
					if(param[0].equalsIgnoreCase("DroplistIcons"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("DroplistIcons", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("DroplistIcons");
					
					if(param[0].equalsIgnoreCase("disableFogAndRain"))
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setDisableFogAndRain(true);
							activeChar.sendPacket(new ClientSetTime(activeChar));
							activeChar.setVar("disableFogAndRain", "true", -1);
						}
						else if(param[1].equalsIgnoreCase("off"))
						{
							activeChar.setDisableFogAndRain(false);
							activeChar.sendPacket(new ClientSetTime(activeChar));
							activeChar.unsetVar("disableFogAndRain");
						}

					if(Config.ENABLE_PLAYER_KILL_SYSTEM && param[0].equalsIgnoreCase("PlayerKillSystem"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("PlayerKillSystem", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("PlayerKillSystem");
					
					if(Config.PARTY_SEARCH_COMMANDS && param[0].equalsIgnoreCase("findparty"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("findparty", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("findparty");
					
					if(Config.ALLOW_PLAYER_CHANGE_LANGUAGE && param[0].equalsIgnoreCase("lang"))
						if(param[1].equalsIgnoreCase("en") && !activeChar.isLangEng()) // if someone know the command can abuse it.
							activeChar.setLanguage(Language.ENGLISH);
						else if(param[1].equalsIgnoreCase("ru") && !activeChar.isLangRus()) // if someone know the command can abuse it.
							activeChar.setLanguage(Language.RUSSIAN);
					
					if(param[0].equalsIgnoreCase("NoExp"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("NoExp", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("NoExp");

					
					if(param[0].equalsIgnoreCase(Player.NO_TRADERS_VAR))
					{
						boolean canUseCommand = activeChar.antiFlood.canDisableOfflineShops();
						if (!canUseCommand)
						{
							activeChar.sendChatMessage(0, ChatType.TELL.ordinal(), "Cfg", "You cannot use this command now, please try again in 10 seconds.");
							return false;
						}
						
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowTraders(true);
							for (Player shops : activeChar.getAroundPlayers())
							{
								if (shops == null)
									continue;
								
								if(shops.isInStoreMode() && !shops.isInBuffStoreMode())
									activeChar.sendPacket(activeChar.removeVisibleObject(shops, null));
							}
							
							activeChar.setVar(Player.NO_TRADERS_VAR, "1", -1);
						}
						else if(param[1].equalsIgnoreCase("off"))
						{
							activeChar.setNotShowTraders(false);
							for (Player shops : activeChar.getAroundPlayers())
							{
								if (shops == null)
									continue;
								
								if(shops.isInStoreMode() && !shops.isInBuffStoreMode())
									activeChar.sendPacket(activeChar.addVisibleObject(shops));
							}
							
							activeChar.unsetVar(Player.NO_TRADERS_VAR);
						}
					}

					if(param[0].equalsIgnoreCase(Player.NO_ANIMATION_OF_CAST_VAR))
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowBuffAnim(true);
							activeChar.setVar(Player.NO_ANIMATION_OF_CAST_VAR, "1", -1);
						}
						else if(param[1].equalsIgnoreCase("off"))
						{
							activeChar.setNotShowBuffAnim(false);
							activeChar.unsetVar(Player.NO_ANIMATION_OF_CAST_VAR);
						}

					if(param[0].equalsIgnoreCase("noShift"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("noShift", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("noShift");

					/*
					if(param[0].equalsIgnoreCase("noColor"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("noColor", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("noColor");
					
					if(param[0].equalsIgnoreCase("noPathfind"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("no_pf", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("no_pf");
					*/
					if (param[0].equalsIgnoreCase("SkillsHideChance"))
						if (param[1].equalsIgnoreCase("on"))
							activeChar.setVar("SkillsHideChance", "1", -1);
						else if (param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("SkillsHideChance");
					
					if (param[0].equalsIgnoreCase("EnchantAnimationDisable"))
						if (param[1].equalsIgnoreCase("on"))
							activeChar.setVar("EnchantAnimationDisable", "1", -1);
						else if (param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("EnchantAnimationDisable");
					
					if (param[0].equalsIgnoreCase("emotions"))
						if (param[1].equalsIgnoreCase("on"))
							activeChar.setVar("emotions", "1", -1);
						else if (param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("emotions");
					
					if(Config.SERVICES_ENABLE_NO_CARRIER && param[0].equalsIgnoreCase("noCarrier"))
					{
						int time = Config.SERVICES_NO_CARRIER_TIME;

						 if (param[0].equalsIgnoreCase("noCarrier"))
							if (param[1].equalsIgnoreCase("on"))
								activeChar.setVar("noCarrier", String.valueOf(time), -1);
							else if (param[1].equalsIgnoreCase("off"))
								activeChar.unsetVar("noCarrier");
					}
					
					if (param[0].equalsIgnoreCase("noEnglish") && activeChar.isLangRus())
					{
						if (param[1].equalsIgnoreCase("on"))
							activeChar.setVar("noEnglish", "true", -1);
						if (param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("noEnglish");
					}
					
					if (param[0].equalsIgnoreCase("noRussian") && activeChar.isLangEng())
					{
						if (param[1].equalsIgnoreCase("on"))
							activeChar.setVar("noCyrilic", "true", -1);
						if (param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("noCyrilic");
					}
					
					if (param[0].equalsIgnoreCase("showVisuals"))
					{
						if (param[1].equalsIgnoreCase("on"))
							activeChar.unsetVar("showVisuals");
						if (param[1].equalsIgnoreCase("off"))
							activeChar.setVar("showVisuals", "false", -1);
					}
					
					/*
					if(param[0].equalsIgnoreCase("translit"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("translit", "tl", -1);
						else if(param[1].equalsIgnoreCase("la"))
							activeChar.setVar("translit", "tc", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("translit");
							*/
					if(param[0].equalsIgnoreCase("autoloot") && Config.AUTO_LOOT_INDIVIDUAL)
						activeChar.setAutoLoot(Boolean.parseBoolean(param[1]));

					if(param[0].equalsIgnoreCase("autoloota") && Config.AUTO_LOOT_INDIVIDUAL)
						activeChar.setAutoLootOnlyAdena(Boolean.parseBoolean(param[1]));

					if(param[0].equalsIgnoreCase("autolooth") && Config.AUTO_LOOT_INDIVIDUAL)
						activeChar.setAutoLootHerbs(Boolean.parseBoolean(param[1]));
				}
			}
		}

		String dialog = HtmCache.getInstance().getNotNull("command/cfg.htm", activeChar);
		
		boolean hasDisabled;
		// lang
		if (Config.ALLOW_PLAYER_CHANGE_LANGUAGE)
		{
			dialog = dialog.replaceFirst("%lang%", activeChar.isLangEng() ? "<font color=\"FF8000\">English</font>" : "<font color=\"FF8000\">Русский</font>");
			
			dialog = dialog.replaceFirst("%langbt%", activeChar.isLangRus() ? "<font name=hs12 color=\"0174DF\">English</font>" : "<font name=hs12 color=\"F2F5A9\">Русский</font>");
			dialog = dialog.replaceFirst("%langbp%", activeChar.isLangRus() ? "bypass -h user_cfg lang en" : "bypass -h user_cfg lang ru");
		}
		else
		{
			dialog = dialog.replaceFirst("%lang%",  "<font color=\"3ADF00\">" + activeChar.getLanguage().toString() + "</font>");
			
			dialog = dialog.replaceFirst("%langbt%", "<font color=\"808080\">Cannot be changed!</font>");
			dialog = dialog.replaceFirst("%langbp%", "#");
		}
		
		// noCyrilic
		if(activeChar.isLangEng())
		{
			hasDisabled = activeChar.getVarB("noCyrilic", false);
			dialog = dialog.replaceFirst("%disableChat%", hasDisabled ? "Show [Russian] chat spam" : "Remove [Russian] chat spam");
			dialog = dialog.replaceFirst("%disableChatbypass%", hasDisabled ? "bypass -h user_cfg noRussian off" : "bypass -h user_cfg noRussian on");
		}
		
		// noCyrilic
		if(activeChar.isLangRus())
		{
			hasDisabled = activeChar.getVarB("noEnglish", false);
			dialog = dialog.replaceFirst("%disableChat%", hasDisabled ? "Show [English] chat spam" : "Remove [English] chat spam");
			dialog = dialog.replaceFirst("%disableChatbypass%", hasDisabled ? "bypass -h user_cfg noEnglish off" : "bypass -h user_cfg noEnglish on");
		}
		
		// DroplistIcons
		hasDisabled = activeChar.getVarB("DroplistIcons", false);
		dialog = dialog.replaceFirst("%dli%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%dlibp%", hasDisabled ? "bypass -h user_cfg DroplistIcons off" : "bypass -h user_cfg DroplistIcons on");
		
		// noPf
		hasDisabled = activeChar.getVarB("no_pf", false);
		dialog = dialog.replaceFirst("%noPf%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%noPfbp%", hasDisabled ? "bypass -h user_cfg noPathfind off" : "bypass -h user_cfg noPathfind on");
		
		// NoExp
		hasDisabled = activeChar.getVarB("NoExp", false);
		dialog = dialog.replaceFirst("%noe%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%noebp%", hasDisabled ? "bypass -h user_cfg NoExp off" : "bypass -h user_cfg NoExp on");
		
		// notraders
		hasDisabled = activeChar.getVarB("notraders", false);
		dialog = dialog.replaceFirst("%notraders%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%notradersbp%", hasDisabled ? "bypass -h user_cfg notraders off" : "bypass -h user_cfg notraders on");
		
		// notShowBuffAnim
		hasDisabled = activeChar.getVarB("notShowBuffAnim", false);
		dialog = dialog.replaceFirst("%notShowBuffAnim%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%notShowBuffAnimbp%", hasDisabled ? "bypass -h user_cfg notShowBuffAnim off" : "bypass -h user_cfg notShowBuffAnim on");
		
		// noShift
		hasDisabled = activeChar.getVarB("noShift", false);
		dialog = dialog.replaceFirst("%noShift%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%noShiftbp%", hasDisabled ? "bypass -h user_cfg noShift off" : "bypass -h user_cfg noShift on");
		
		// noColor
		hasDisabled = activeChar.getVarB("noColor", false);
		dialog = dialog.replaceFirst("%noColor%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%noColorbp%", hasDisabled ? "bypass -h user_cfg noColor off" : "bypass -h user_cfg noColor on");
		
		// SkillsHideChance
		hasDisabled = activeChar.getVarB("SkillsHideChance", false);
		dialog = dialog.replaceFirst("%SkillsHideChance%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%SkillsHideChancebp%", hasDisabled ? "bypass -h user_cfg SkillsHideChance off" : "bypass -h user_cfg SkillsHideChance on");
		
		// EnchantAnimationDisable
		hasDisabled = activeChar.getVarB("EnchantAnimationDisable", false);
		dialog = dialog.replaceFirst("%EnchantAnimationDisable%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%EnchantAnimationDisablebp%", hasDisabled ? "bypass -h user_cfg EnchantAnimationDisable off" : "bypass -h user_cfg EnchantAnimationDisable on");
		
		// noCarrier
		hasDisabled = activeChar.getVar("noCarrier") != null;
		dialog = dialog.replaceFirst("%noCarrier%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%noCarrierbp%", hasDisabled ? "bypass -h user_cfg noCarrier off" : "bypass -h user_cfg noCarrier on");
		
		// emotions
		hasDisabled = activeChar.getVarB("emotions", false);
		dialog = dialog.replaceFirst("%emotions%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%emotionsbp%", hasDisabled ? "bypass -h user_cfg emotions off" : "bypass -h user_cfg emotions on");
		
		// PlayerKillSystem
		hasDisabled = activeChar.getVarB("PlayerKillSystem", false);
		dialog = dialog.replaceFirst("%killSystem%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%killSystembp%", hasDisabled ? "bypass -h user_cfg PlayerKillSystem off" : "bypass -h user_cfg PlayerKillSystem on");
		
		// findparty
		hasDisabled = activeChar.getVarB("findparty", false);
		dialog = dialog.replaceFirst("%findParty%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%findPartybp%", hasDisabled ? "bypass -h user_cfg findparty off" : "bypass -h user_cfg findparty on");
		
		// disableFogAndRain
		hasDisabled = activeChar.getVarB("disableFogAndRain", false);
		dialog = dialog.replaceFirst("%disableFogAndRain%", hasDisabled ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
		dialog = dialog.replaceFirst("%disableFogAndRainbp%", hasDisabled ? "bypass -h user_cfg disableFogAndRain off" : "bypass -h user_cfg disableFogAndRain on");
		
		if (Config.AUTO_LOOT_INDIVIDUAL)
		{
			//autoLoot
			dialog = dialog.replaceFirst("%autoLoot%", activeChar.isAutoLootEnabled() ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
			dialog = dialog.replaceFirst("%autoLootbp%", activeChar.isAutoLootEnabled() ? "bypass -h user_cfg autoloot false" : "bypass -h user_cfg autoloot true");
			
			//autoLootAdena
			dialog = dialog.replaceFirst("%autoLootAdena%", activeChar.isAutoLootOnlyAdenaEnabled() ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
			dialog = dialog.replaceFirst("%autoLootAdenabp%", activeChar.isAutoLootOnlyAdenaEnabled() ? "bypass -h user_cfg autoloota false" : "bypass -h user_cfg autoloota true");
			
			//autoLootHerbs
			dialog = dialog.replaceFirst("%autoLootHerbs%", activeChar.isAutoLootHerbsEnabled() ? "<font name=hs12 color=\"01DF01\">Enabled</font>" : "<font name=hs12 color=\"DF0101\">Disabled</font>");
			dialog = dialog.replaceFirst("%autoLootHerbsbp%", activeChar.isAutoLootHerbsEnabled() ? "bypass -h user_cfg autolooth false" : "bypass -h user_cfg autolooth true");
		}
		else
		{
			dialog = dialog.replaceFirst("%autoLoot%", "<font color=\"808080\">Cannot be changed!</font>");
			dialog = dialog.replaceFirst("%autoLootbp%", "#");
			
			dialog = dialog.replaceFirst("%autoLootAdena%", "<font color=\"808080\">Cannot be changed!</font>");
			dialog = dialog.replaceFirst("%autoLootAdenabp%", "#");
			
			dialog = dialog.replaceFirst("%autoLootHerbs%", "<font color=\"808080\">Cannot be changed!</font>");
			dialog = dialog.replaceFirst("%autoLootHerbsbp%", "#");
		}
		
		/*
		for(String par : _params)
			if(activeChar.getVar(par) == null)
			{
				dialog = dialog.replaceAll("%bypass" + par + "%", "bypass -h user_cfg " + par + " on");
				dialog = dialog.replaceAll("%buttonfore" + par + "%", "%image:8001.png%");
				dialog = dialog.replaceAll("%buttonback" + par + "%", "%image:8001.png%");
			}
			else
			{
				dialog = dialog.replaceAll("%bypass" + par + "%", "bypass -h user_cfg " + par + " of");
				dialog = dialog.replaceAll("%buttonfore" + par + "%", "%image:8002.png%");
				dialog = dialog.replaceAll("%buttonback" + par + "%", "%image:8002.png%");
			}
		*/
		
		/*
		String tl = activeChar.getVar("translit");
		if(tl == null)
			dialog = dialog.replaceFirst("%translit%", "<font name=hs12 color=\"DF0101\">Off</font>");
		else if(tl.equals("tl"))
			dialog = dialog.replaceFirst("%translit%", "On");
		else
			dialog = dialog.replaceFirst("%translit%", "Lt");
			

		StringBuilder events = new StringBuilder();
		for(GlobalEvent e : activeChar.getEvents())
			events.append(e.toString()).append("<br>");
		dialog = dialog.replace("%events%", events.toString());
		*/
		show(dialog, activeChar);

		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}