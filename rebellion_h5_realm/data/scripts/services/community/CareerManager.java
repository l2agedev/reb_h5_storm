package services.community;

import l2r.gameserver.Config;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SubClass;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.ClassType;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.Language;
import l2r.gameserver.utils.Util;

import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CareerManager implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CareerManager.class);
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.BOARD_ENABLE_CLASS_MASTER)
		{
			_log.info("CommunityBoard: Manage Career service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.BOARD_ENABLE_CLASS_MASTER)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbscareer;", "_bbscareer;sub;", "_bbscareer;classmaster;change_class;", "_bbscareer;certification;" };
	}
	
	@Override
	public void onBypassCommand(Player activeChar, String command)
	{
		if(!CheckCondition(activeChar))
			return;
		
		if(command.startsWith("_bbscareer;"))
		{
			ClassId classId = activeChar.getClassId();
			int jobLevel = classId.getLevel();
			int level = activeChar.getLevel();
			StringBuilder html = new StringBuilder();
			html.append("<br><br><center>");
			if(Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() || !Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
				jobLevel = 4;

			if((level >= 20 && jobLevel == 1 || level >= 40 && jobLevel == 2 || level >= 76 && jobLevel == 3) && Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
			{
				html.append("<center>");
				html.append("<br><br>");
				ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEM_LIST[jobLevel]);
				if(activeChar.getLanguage() == Language.ENGLISH)
				{
					html.append("You have to pay: <font color=\"LEVEL\">");
					html.append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LIST[jobLevel])).append("</font> <font color=\"LEVEL\">").append(item.getName()).append("</font> to change profession<br>");						
				}
				else
				{
					html.append("Вы должны заплатить: <font color=\"LEVEL\">");
					html.append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LIST[jobLevel])).append("</font> <font color=\"LEVEL\">").append(item.getName()).append("</font> для смены профессии<br>");
				}
				
				for(ClassId cid : ClassId.values())
				{
					if(cid == ClassId.inspector)
						continue;
					if(cid.childOf(classId) && cid.level() == classId.level() + 1)
						html.append("<center><button value=\"").append(cid.name()).append("\" action=\"bypass _bbscareer;classmaster;change_class;").append(cid.getId()).append(";").append(Config.CLASS_MASTERS_PRICE_ITEM_LIST[jobLevel]).append(";").append(Config.CLASS_MASTERS_PRICE_LIST[jobLevel]).append("\" width=150 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");

				}
				
				html.append("<br><br>");
				html.append("</center>");
			}
			else
			{
				html.append("<br><br>");
				html.append("<center>");
				switch(jobLevel)
				{
					case 1:
						if(activeChar.getLanguage() == Language.ENGLISH)
						{
							html.append("<font name=hs12>Greetings <font name=hs12 color=FF4000>" + activeChar.getName() + "</font> your current profession is <font name=hs12 color=00FF40>" + Util.getFullClassName(activeChar.getActiveClassId()) + "</font></font><br>");
							html.append("<img src=\"l2ui.squaregray\" width=\"500\" height=\"1\"><br>");
							html.append("To change your profession you have to reach: <font color=F2C202>level 20</font><br>");
							html.append("To activate the subclass you have to reach <font color=F2C202>level 75</font><br>");
							html.append("To become a nobleman, you have to bleed to subclass <font color=F2C202>level 76</font><br>");					
						}
						else
						{
							html.append("Приветствую <font color=F2C202>" + activeChar.getName() + "</font> ваша текущая профессия <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>20-го уровня</font><br>");
							html.append("Для активации сабклассов вы должны достичь <font color=F2C202>75-го уровня</font><br>");
							html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня</font><br>");
						}
						html.append(getSubClassesHtml(activeChar, true));
						break;
					case 2:
						if(activeChar.getLanguage() == Language.ENGLISH)
						{
							html.append("<font name=hs12>Greetings <font name=hs12 color=FF4000>" + activeChar.getName() + "</font> your current profession is <font name=hs12 color=00FF40>" + Util.getFullClassName(activeChar.getActiveClassId()) + "</font></font><br>");
							html.append("<img src=\"l2ui.squaregray\" width=\"500\" height=\"1\"><br>");
							html.append("To change your profession you have to reach: <font color=F2C202>level 40</font><br>");
							html.append("To activate the subclass you have to reach <font color=F2C202>level 75</font><br>");
							html.append("To become a nobleman, you have to bleed to subclass <font color=F2C202>7level 76</font><br>");						
						}
						else
						{
							html.append("Приветствую <font color=F2C202>" + activeChar.getName() + "</font> ваша текущая профессия <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>40-го уровня</font><br>");
							html.append("Для активации сабклассов вы должны достичь <font color=F2C202>75-го уровня</font><br>");
							html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня</font><br>");
						}
						html.append(getSubClassesHtml(activeChar, true));
						break;
					case 3:
						if(activeChar.getLanguage() == Language.ENGLISH)
						{
							html.append("<font name=hs12>Greetings <font name=hs12 color=FF4000>" + activeChar.getName() + "</font> your current profession is <font name=hs12 color=00FF40>" + Util.getFullClassName(activeChar.getActiveClassId()) + "</font></font><br>");
							html.append("<img src=\"l2ui.squaregray\" width=\"500\" height=\"1\"><br>");
							html.append("To change your profession you have to reach: <font color=F2C202>level 76</font><br>");
							html.append("To activate the subclass you have to reach <font color=F2C202>level 75</font><br>");
							html.append("To become a nobleman, you have to bleed to subclass <font color=F2C202>level 76</font><br>");						
						}
						else
						{
							html.append("Приветствую <font color=F2C202>" + activeChar.getName() + "</font> ваша текущая профессия <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("Для того чтобы сменить вашу профессию вы должны достичь: <font color=F2C202>76-го уровня</font><br>");
							html.append("Для активации сабклассов вы должны достичь <font color=F2C202>75-го уровня</font><br>");
							html.append("Чтобы стать дворянином вы должны прокачать сабкласс до <font color=F2C202>76-го уровня</font><br>");
						}
						html.append(getSubClassesHtml(activeChar, true));
						break;
					case 4:
						if(activeChar.getLanguage() == Language.ENGLISH)
						{
							html.append("<font name=hs12>Greetings <font name=hs12 color=FF4000>" + activeChar.getName() + "</font> your current profession is <font name=hs12 color=00FF40>" + Util.getFullClassName(activeChar.getActiveClassId()) + "</font></font><br>");
							html.append("<img src=\"l2ui.squaregray\" width=\"500\" height=\"1\"><br>");
							html.append("For you are no more jobs available, or the master class is not currently available.<br>");
							if(level >= 76)
							{
								html.append("You have reached the <font color=F2C202>level 75</font> activation of the subclass is now available<br>");
								if(!activeChar.isNoble() && activeChar.getSubLevel() < 75)
									html.append("You can get the nobility only after your sub-class reaches the 76 level.<br>");
								else if(!activeChar.isNoble() && activeChar.getSubLevel() > 75)
									html.append("You can get the nobility. Your sub-class has reached the 76th level.<br>");
								else if(activeChar.isNoble())
									html.append("You have a gentleman. Getting the nobility no longer available.<br>");
							}					
						}
						else
						{
							html.append("Приветствую <font color=F2C202>" + activeChar.getName() + "</font> ваша текущая профессия <font color=F2C202>" + activeChar.getClassId().name() + "</font><br>");
							html.append("Для вас больше нет доступных профессий, либо Класс мастер в данный момент недоступен.<br>");
							if(level >= 76)
							{
								html.append("Вы достигли <font color=F2C202>75-го уровня</font> активация сабклассов теперь доступна<br>");
								if(!activeChar.isNoble() && activeChar.getSubLevel() < 75)
									html.append("Вы можете получить дворянство только после того как ваш саб-класс достигнет 76-го уровня.<br>");
								else if(!activeChar.isNoble() && activeChar.getSubLevel() > 75)
									html.append("Вы можете получить дворянство. Ваш саб-класс достиг 76-го уровня.<br>");
								else if(activeChar.isNoble())
									html.append("Вы уже дворянин. Получение дворянства более не доступно.<br>");
							}
						}
						
						html.append(getSubClassesHtml(activeChar, true));
						break;
				}
				html.append("</center>");
			}
			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/career.htm", activeChar);
			content = content.replace("%career%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
		}
		if(command.startsWith("_bbscareer;sub;"))
		{
			if (!activeChar.isGM())
			{
				if (activeChar.isInDuel())
					return;
				
				if(activeChar.getPet() != null)
				{
					activeChar.sendPacket(SystemMsg.A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED);
					return;
				}

				// Саб класс нельзя получить или поменять, пока используется скилл или персонаж находится в режиме трансформации
				if(activeChar.isActionsDisabled() || activeChar.getTransformation() != 0)
				{
					activeChar.sendPacket(SystemMsg.SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
					return;
				}

				if(activeChar.getWeightPenalty() >= 3)
				{
					activeChar.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
					return;
				}

				if(activeChar.getUsedInventoryPercents() >= 80)
				{
					activeChar.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
					return;
				}
			}
			
			StringBuilder html = new StringBuilder();

			Map<Integer, SubClass> playerClassList = activeChar.getSubClasses();
			Set<PlayerClass> subsAvailable;

			if(activeChar.getLevel() < 40)
			{
				if (activeChar.isLangRus())
					html.append("Вы должны быть уровня 40 или больше, чтобы работать с суб-классы.");
				else
					html.append("You must be level 40 or more to operate with your sub-classes.");
				
				String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/career.htm", activeChar);
				content = content.replace("%career%", html.toString());
				ShowBoard.separateAndSend(content, activeChar);
				return;
			}

			int classId = 0;
			int newClassId = 0;
			int intVal = 0;

			try
			{
				for(String id : command.substring(15, command.length()).split(" "))
				{
					if(intVal == 0)
					{
						intVal = Integer.parseInt(id);
						continue;
					}
					if(classId > 0)
					{
						newClassId = Short.parseShort(id);
						continue;
					}
					classId = Short.parseShort(id);
				}
			}
			catch(Exception NumberFormatException)
			{}
			
			switch(intVal)
			{
				case 1: // Возвращает список сабов, которые можно взять (см case 4)
					subsAvailable = getAvailableSubClasses(activeChar, true);

					if(subsAvailable != null && !subsAvailable.isEmpty())
					{
						if (activeChar.isLangRus())
							html.append("<br><br><font name=hs9 color=FE2E64>Вам доступны следующие саб-классы:</font><br>");
						else
							html.append("<br><br><font name=hs9 color=FE2E64>You have the following sub-classes:</font><br>");

						html.append("<img src=\"l2ui.squaregray\" width=\"300\" height=\"1\"><br>");
						
						for(PlayerClass subClass : subsAvailable)
							html.append("<a action=\"bypass _bbscareer;sub;4 " + subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br1>");
					}
					else
					{
						if (activeChar.isLangRus())
							html.append("<br>Вам доступны следующие саб-классы:<br>");
						else
							html.append("<br>You have the following sub-classes:<br>");
					}
					
					html.append("<br>");
					html.append("<button value=\"\" action=\"bypass _bbscareer;\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Input\" fore=\"L2UI_CT1.Button_DF_Input\">");
					html.append("<br>");
					break;
				case 2: // Установка уже взятого саба (см case 5)
					if (activeChar.isLangRus())
						html.append("<br><font name=hs9 color=81DAF5>Переключить саб-класс:</font><br>");
					else
						html.append("<br><font name=hs9 color=81DAF5>Switch sub-class:</font><br>");
					

					final int baseClassId = activeChar.getBaseClassId();

					if(playerClassList.size() < 2)
						if (activeChar.isLangRus())
							html.append("У вас нет саб-классов для переключения, но вы можете добавить его прямо сейчас<br><a action=\"bypass _bbscareer;sub;1\">Добавить саб.</a>");
						else
							html.append("You dont have any subclass to switch, but you can add it right now<br><a action=\"bypass _bbscareer;sub;1\">Add Subclass</a>");
						
					else
					{
						if (activeChar.isLangRus())
							html.append("Какой саб-класс вы желаете использовать?");
						else
							html.append("What sub-class you want to use?");

						html.append("<img src=\"l2ui.squaregray\" width=\"300\" height=\"1\"><br>");
						
						if(baseClassId == activeChar.getActiveClassId())
							if(activeChar.isLangRus())
								html.append(HtmlUtils.htmlClassName(baseClassId) + " <font color=\"LEVEL\">(Базовый)</font><br><br>");
							else
								html.append(HtmlUtils.htmlClassName(baseClassId) + " <font color=\"LEVEL\">(Base)</font><br><br>");
						else
							if(activeChar.isLangRus())
								html.append("<a action=\"bypass _bbscareer;sub;5 " + baseClassId + "\">" + HtmlUtils.htmlClassName(baseClassId) + "</a> " + "<font color=\"LEVEL\">(Базовый)</font><br>");
							else
								html.append("<a action=\"bypass _bbscareer;sub;5 " + baseClassId + "\">" + HtmlUtils.htmlClassName(baseClassId) + "</a> " + "<font color=\"LEVEL\">(Base)</font><br>");
								
						for(SubClass subClass : playerClassList.values())
						{
							if(subClass.isBase())
								continue;
							int subClassId = subClass.getClassId();

							if(subClassId == activeChar.getActiveClassId())
								html.append("<font name=hs9 color=\"2EFE2E\">" + HtmlUtils.htmlClassName(subClassId) + "</font><br1>(Current) <br>");
							else
								html.append("<a action=\"bypass _bbscareer;sub;5 " + subClassId + "\">" + HtmlUtils.htmlClassName(subClassId) + "</a><br>");
						}
						
						html.append("<br>");
						html.append("<button value=\"\" action=\"bypass _bbscareer;\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Input\" fore=\"L2UI_CT1.Button_DF_Input\">");
						html.append("<br>");
					}
					break;
				case 3: // Отмена сабкласса - список имеющихся (см case 6)
					if (activeChar.isLangRus())
						html.append("<br><br><font name=hs9 color=FE642E>Отмена саб-класса:</font><br>Какой из имеющихся сабов вы хотете заменить?<br>");
					else
						html.append("<br><br><font name=hs9 color=FE642E>Remove sub-class:</font><br>Which of the existing subs you want to replace?<br>");

					html.append("<img src=\"l2ui.squaregray\" width=\"300\" height=\"1\"><br>");

					for(SubClass sub : playerClassList.values())
					{
						html.append("<br>");
						if(!sub.isBase())
							html.append("<a action=\"bypass _bbscareer;sub;6 " + sub.getClassId() + "\">" + HtmlUtils.htmlClassName(sub.getClassId()) + "</a><br>");
					}

					html.append("<br>");
					html.append("<button value=\"\" action=\"bypass _bbscareer;\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Input\" fore=\"L2UI_CT1.Button_DF_Input\">");
					html.append("<br>");
					break;
				case 4: // Добавление сабкласса - обработка выбора из case 1
					boolean allowAddition = true;

					// Проверка хватает ли уровня
					if(activeChar.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", activeChar).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
						allowAddition = false;
					}

					if(!playerClassList.isEmpty())
					{
						for(SubClass subClass : playerClassList.values())
						{
							if(subClass.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS)
							{
								activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", activeChar).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
								allowAddition = false;
								break;
							}
						}
					}
					else
					{
						html.append("Error! Your Class List is Empty. Call to GM!");
					}

					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(activeChar))
					{
						activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(activeChar));
						return;
					}
					
					if(allowAddition)
					{
						String className = HtmlUtils.htmlClassName(activeChar.getActiveClassId());

						if(!activeChar.addSubClass(classId, true, 0))
						{
							if(activeChar.isLangRus())
								html.append("Саб-класс не добавлен!");
							else
								html.append("Sub-class is not added!");
							return;
						}

						// Possible fix for animation bug while holding dual daggers and change sub.
						ItemInstance item = activeChar.getActiveWeaponInstance();
						if (item != null && item.getName().contains("Dual Dagger"))
						{
							activeChar.broadcastCharInfo();
							activeChar.decayMe();
							activeChar.spawnMe(activeChar.getLoc());
						}
						
						if (activeChar.isLangRus())
							html.append("<br><br><font name=hs9 color=86B404>Саб-класс <font name=hs9>" + className + "</font> успешно добавлен!</font><br>");
						else
							html.append("<br><br><font name=hs9 color=86B404>Sub-Class <font name=hs9>" + className + "</font> successfully added!</font><br>");
						
						html.append("<br>");
						html.append("<button value=\"\" action=\"bypass _bbscareer;\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Input\" fore=\"L2UI_CT1.Button_DF_Input\">");
						html.append("<br>");
						activeChar.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER); // Transfer to new class.
					}
					else
					{
						if(activeChar.isLangRus())
							html.append("<br><br>Вы не можете добавить подкласс в данный момент.<br>Для активации сабклассов вы должны достичь <font color=F2C202>75-го уровня</font><br>");
						else
							html.append("<br><br>You cannot add sub-class at this moment. <br>To activate the subclass you must reach <font color=F2C202>level 75.</font><br>");
						
						html.append("<br>");
						html.append("<button value=\"\" action=\"bypass _bbscareer;\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Input\" fore=\"L2UI_CT1.Button_DF_Input\">");
						html.append("<br>");
					}
					
					break;
				case 5: // Смена саба на другой из уже взятых - обработка выбора из case 2
					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(activeChar))
					{
						activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(activeChar));
						return;
					}
					
					if (activeChar.isCastingNow())
					{
						activeChar.sendActionFailed();
						return;
					}
					
					activeChar.setActiveSubClass(classId, true);

					// Possible fix for animation bug while holding dual daggers and change sub.
					ItemInstance item = activeChar.getActiveWeaponInstance();
					if (item != null && item.getName().contains("Dual Dagger"))
					{
						activeChar.broadcastCharInfo();
						activeChar.decayMe();
						activeChar.spawnMe(activeChar.getLoc());
					}
					
					if(activeChar.isLangRus())
						html.append("<br><br><br><font name=hs12>Ваш активный саб-класс теперь: <font color=\"LEVEL\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()).toUpperCase() + "</font></font>");
					else
						html.append("<br><br><br><font name=hs12>Your active sub-class is now: <font name=hs12 color=\"LEVEL\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()).toUpperCase() + "</font></font>");

					html.append("<br>");
					html.append("<button value=\"\" action=\"bypass _bbscareer;\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Input\" fore=\"L2UI_CT1.Button_DF_Input\">");
					html.append("<br>");
					
					activeChar.sendPacket(SystemMsg.YOU_HAVE_SUCCESSFULLY_SWITCHED_TO_YOUR_SUBCLASS); // Transfer
					// completed.
					break;
				case 6: // Отмена сабкласса - обработка выбора из case 3
					if (activeChar.isLangRus())
						html.append("<br><br><font name=hs9 color=A4AEA8>Выберите саб-класс для смены.</font><br><font color=\"LEVEL\">Внимание!</font> Все профессии и скилы для этого саба будут удалены.<br>");
					else
						html.append("<br><br><font name=hs9 color=A4AEA8>Select a sub-class to change.</font><br><font name=hs9 color=\"DF0101\">Attention!</font> All professions and skills for this subclass will be deleted.<br>");

					html.append("<img src=\"l2ui.squaregray\" width=\"300\" height=\"1\"><br>");

					subsAvailable = getAvailableSubClasses(activeChar, false);

					if(!subsAvailable.isEmpty())
						for(PlayerClass subClass : subsAvailable)
							html.append("<a action=\"bypass _bbscareer;sub;7 " + classId + " " + subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
					else
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", activeChar));
						return;
					}
					
					html.append("<br>");
					html.append("<button value=\"\" action=\"bypass _bbscareer;\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Input\" fore=\"L2UI_CT1.Button_DF_Input\">");
					html.append("<br>");
					
					break;
				case 7: // Отмена сабкласса - обработка выбора из case 6
					// activeChar.sendPacket(Msg.YOUR_PREVIOUS_SUB_CLASS_WILL_BE_DELETED_AND_YOUR_NEW_SUB_CLASS_WILL_START_AT_LEVEL_40__DO_YOU_WISH_TO_PROCEED); // Change confirmation.

					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(activeChar))
					{
						activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(activeChar));
						return;
					}

					// Удаляем скиллы трансфера
					int item_id = 0;
					switch(ClassId.values()[classId])
					{
						case cardinal:
							item_id = 15307;
							break;
						case evaSaint:
							item_id = 15308;
							break;
						case shillienSaint:
							item_id = 15309;
							break;
					}
					if(item_id > 0)
						activeChar.unsetVar("TransferSkills" + item_id);

					if(activeChar.modifySubClass(classId, newClassId))
					{
						if(activeChar.isLangRus())
							html.append("<br><br><br><font name=hs12>Ваш саб-класс изменен на: <font color=\"LEVEL\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()).toUpperCase() + "</font></font>");
						else
							html.append("<br><br><br><font name=hs12>Your sub-class is changed to: <font name=hs12 color=\"LEVEL\">" + HtmlUtils.htmlClassName(activeChar.getActiveClassId()).toUpperCase() + "</font></font>");

						html.append("<br>");
						html.append("<button value=\"\" action=\"bypass _bbscareer;\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Input\" fore=\"L2UI_CT1.Button_DF_Input\">");
						html.append("<br>");
						
						activeChar.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED); // Subclass added.
					}
					else
					{
						activeChar.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", activeChar));
						return;
					}
					break;
			}

			String content = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/career.htm", activeChar);
			content = content.replace("%career%", html.toString());
			ShowBoard.separateAndSend(content, activeChar);
		}
		if(command.equalsIgnoreCase("_bbscareer;certification;"))
		{
			chooseCertificatePage(activeChar);
			return;
		}
		if(command.startsWith("_bbscareer;classmaster;change_class;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			st.nextToken();
			short val = Short.parseShort(st.nextToken());
			int itemId = Integer.parseInt(st.nextToken());
			long price = Long.parseLong(st.nextToken());
			
			ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
			ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			
			if(pay != null && pay.getCount() >= price)
			{
				activeChar.getInventory().destroyItem(pay, price);
				changeClass(activeChar, val);
				onBypassCommand(activeChar, "_bbscareer;");
			}
			else if(itemId == 57)
				activeChar.sendPacket(new SystemMessage2(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
			else
				activeChar.sendPacket(new SystemMessage2(SystemMsg.INCORRECT_ITEM_COUNT));
		}
	}

	private StringBuilder getSubClassesHtml(Player activeChar, boolean condition)
	{
		StringBuilder html = new StringBuilder();
		
		if(!Config.BBS_PVP_SUB_MANAGER_ALLOW)
		{
			activeChar.sendMessage(new CustomMessage("scripts.services.community.careermanager.service_disabled", activeChar));
			return html;
		}
		
		Set<PlayerClass> subsAvailable = getAvailableSubClasses(activeChar, true);

		if(condition)
		{
			if(!activeChar.isInZone(Zone.ZoneType.peace_zone) && !activeChar.isInZone(Zone.ZoneType.RESIDENCE) && Config.BBS_PVP_SUB_MANAGER_PEACE_ZONE)
			{
				if (activeChar.isLangRus())
					html.append("<br><font color=F2C202>" + activeChar.getName() + "</font> вам доступны следующие операции над саб-классами:<br><br>Вернитесь в город. Операции над сабом доступны только в городе");	
				else
					html.append("<br><font color=F2C202>" + activeChar.getName() + "</font> you dont have access to career managment, <br><br> Please return to town to use it.");
			}
			else
			{
				if (activeChar.isLangRus())
				{
					html.append("<br><font color=F2C202>" + activeChar.getName() + "</font> вам доступны следующие операции над саб-классами:<br>");
					html.append("<center><table width=600><tr>");
					if (subsAvailable != null && !subsAvailable.isEmpty())
						html.append("<td><center><button value=\"Добавить\" action=\"bypass _bbscareer;sub;1\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
					html.append("<td><center><button value=\"Изменить\" action=\"bypass _bbscareer;sub;2\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
					html.append("<td><center><button value=\"Отменить\" action=\"bypass _bbscareer;sub;3\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
					html.append("</tr>");
					html.append("<tr>");
					html.append("<td><center><button value=\"Certifications\" action=\"bypass _bbscareer;certification;\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
					html.append("</tr>");
					html.append("</table></center>");
				}
				else
				{
					html.append("<br><font name=hs9 color=81DAF5>You can access the following options for sub-class:<br>");
					html.append("<center><table width=600><tr>");
					if (subsAvailable != null && !subsAvailable.isEmpty())
						html.append("<td><center><button value=\"Add\" action=\"bypass _bbscareer;sub;1\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
					html.append("<td><center><button value=\"Change\" action=\"bypass _bbscareer;sub;2\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
					html.append("<td><center><button value=\"Remove\" action=\"bypass _bbscareer;sub;3\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
					html.append("</tr>");
					html.append("<tr>");
					html.append("<td>&nbsp;</td>");
					html.append("<td><center><button value=\"Certifications\" action=\"bypass _bbscareer;certification;\" width=150 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
					html.append("</tr>");
					html.append("</table></center>");
				}
			}
		}
		else
			html.append("<br>");
		
		return html;
	}
	
	private Set<PlayerClass> getAvailableSubClasses(Player player, boolean isNew)
	{
		final int charClassId = player.getBaseClassId();
		final Race pRace = player.getRace();
		final ClassType pTeachType = getTeachType(player);

		PlayerClass currClass = PlayerClass.values()[charClassId];// .valueOf(charClassName);

		Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
		if(availSubs == null)
			return null;

		// Из списка сабов удаляем мейн класс игрока
		availSubs.remove(currClass);

		for(PlayerClass availSub : availSubs)
		{
			if (Config.ALL_SUBCLASSES_AVAILABLE)
			{
				// Удаляем из списка возможных сабов, уже взятые сабы и их предков
				for(SubClass subClass : player.getSubClasses().values())
				{
					if(availSub.ordinal() == subClass.getClassId())
					{
						availSubs.remove(availSub);
						continue;
					}

					// Удаляем из возможных сабов их родителей, если таковые есть у чара
					ClassId parent = ClassId.values()[availSub.ordinal()].getParent(player.getSex());
					if(parent != null && parent.getId() == subClass.getClassId())
					{
						availSubs.remove(availSub);
						continue;
					}

					// Удаляем из возможных сабов родителей текущих сабклассов, иначе если взять саб berserker
					// и довести до 3ей профы - doombringer, игроку будет предложен berserker вновь (дежавю)
					ClassId subParent = ClassId.values()[subClass.getClassId()].getParent(player.getSex());
					if(subParent != null && subParent.getId() == availSub.ordinal())
						availSubs.remove(availSub);
				}

				// Особенности саб классов камаэль
				if(availSub.isOfRace(Race.kamael))
				{
					// Для Soulbreaker-а и SoulHound не предлагаем Soulbreaker-а другого пола
					if((currClass == PlayerClass.MaleSoulHound || currClass == PlayerClass.FemaleSoulHound || currClass == PlayerClass.FemaleSoulbreaker || currClass == PlayerClass.MaleSoulbreaker) && (availSub == PlayerClass.FemaleSoulbreaker || availSub == PlayerClass.MaleSoulbreaker))
						availSubs.remove(availSub);

					// Для Berserker(doombringer) и Arbalester(trickster) предлагаем Soulbreaker-а только своего пола
					if(currClass == PlayerClass.Berserker || currClass == PlayerClass.Doombringer || currClass == PlayerClass.Arbalester || currClass == PlayerClass.Trickster)
						if(player.getSex() == 1 && availSub == PlayerClass.MaleSoulbreaker || player.getSex() == 0 && availSub == PlayerClass.FemaleSoulbreaker)
							availSubs.remove(availSub);

					// Inspector доступен, только когда вкачаны 2 возможных первых саба камаэль(+ мейн класс)
					if(availSub == PlayerClass.Inspector && player.getSubClasses().size() < (isNew ? 3 : 4))
						availSubs.remove(availSub);
				}
			}
			else
			{
				// Удаляем из списка возможных сабов, уже взятые сабы и их предков
				for(SubClass subClass : player.getSubClasses().values())
				{
					if(availSub.ordinal() == subClass.getClassId())
					{
						availSubs.remove(availSub);
						continue;
					}

					// Удаляем из возможных сабов их родителей, если таковые есть у чара
					ClassId parent = ClassId.values()[availSub.ordinal()].getParent(player.getSex());
					if(parent != null && parent.getId() == subClass.getClassId())
					{
						availSubs.remove(availSub);
						continue;
					}

					// Удаляем из возможных сабов родителей текущих сабклассов, иначе если взять саб berserker
					// и довести до 3ей профы - doombringer, игроку будет предложен berserker вновь (дежавю)
					ClassId subParent = ClassId.values()[subClass.getClassId()].getParent(player.getSex());
					if(subParent != null && subParent.getId() == availSub.ordinal())
						availSubs.remove(availSub);
				}

				if(!availSub.isOfRace(Race.human) && !availSub.isOfRace(Race.elf))
				{
					if(!availSub.isOfRace(pRace))
						availSubs.remove(availSub);
				}
				else if(!availSub.isOfType(pTeachType))
					availSubs.remove(availSub);

				// Особенности саб классов камаэль
				if(availSub.isOfRace(Race.kamael))
				{
					// Для Soulbreaker-а и SoulHound не предлагаем Soulbreaker-а другого пола
					if((currClass == PlayerClass.MaleSoulHound || currClass == PlayerClass.FemaleSoulHound || currClass == PlayerClass.FemaleSoulbreaker || currClass == PlayerClass.MaleSoulbreaker) && (availSub == PlayerClass.FemaleSoulbreaker || availSub == PlayerClass.MaleSoulbreaker))
						availSubs.remove(availSub);

					// Для Berserker(doombringer) и Arbalester(trickster) предлагаем Soulbreaker-а только своего пола
					if(currClass == PlayerClass.Berserker || currClass == PlayerClass.Doombringer || currClass == PlayerClass.Arbalester || currClass == PlayerClass.Trickster)
						if(player.getSex() == 1 && availSub == PlayerClass.MaleSoulbreaker || player.getSex() == 0 && availSub == PlayerClass.FemaleSoulbreaker)
							availSubs.remove(availSub);

					// Inspector доступен, только когда вкачаны 2 возможных первых саба камаэль(+ мейн класс)
					if(availSub == PlayerClass.Inspector && player.getSubClasses().size() < (isNew ? 3 : 4))
						availSubs.remove(availSub);
				}
			}	
		}
		return availSubs;
	}

	private String formatClassForDisplay(PlayerClass className)
	{
		String classNameStr = className.toString();
		char[] charArray = classNameStr.toCharArray();

		for(int i = 1; i < charArray.length; i++)
			if(Character.isUpperCase(charArray[i]))
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);

		return classNameStr;
	}
	
	private ClassType getTeachType(Player player)
	{
		if(!PlayerClass.values()[player.getBaseClassId()].isOfType(ClassType.Priest))
			return ClassType.Priest;

		if(!PlayerClass.values()[player.getBaseClassId()].isOfType(ClassType.Mystic))
			return ClassType.Mystic;

		return ClassType.Fighter;
	}

	private void changeClass(Player player, int val)
	{
		if(player.getClassId().getLevel() == 3)
			player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_YOUR_THIRDCLASS_TRANSFER_QUEST); // для 3 профы
		else
			player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER); // для 1 и 2 профы

		player.setClassId(val, false, false);
		player.broadcastUserInfo(true);
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}

	private static boolean CheckCondition(Player player)
	{
		if(player == null)
            return false;

		if (player.isGM())
			return true;
		
		if (player.isInJail())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.careermanager.cannot_use_in_jail", player));
			return false;
		}
		
		if (PremiumAccountsTable.getCareerOutsidePeace(player))
		{
			if (player.getReflectionId() != ReflectionManager.DEFAULT_ID || NexusEvents.isRegistered(player) ||  NexusEvents.isInEvent(player) || player.isInOlympiadMode())
			{
				player.sendMessage("You cannot use this option while in an instance, olympiad or event.");
				return false;
			}
		}
		else
		{		
			if (!player.isInZone(ZoneType.peace_zone) && !player.isInZone(ZoneType.RESIDENCE) || NexusEvents.isRegistered(player) || NexusEvents.isInEvent(player) || player.isInOlympiadMode())
			{
				player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", (player.isLangRus() ? "Вы должны быть в зону мира, чтобы использовать эту функцию." : "You must be inside peace zone to use this function."));
				return false;
			}
		}
		
		if(!Config.USE_BBS_PROF_IS_COMBAT && (player.getPvpFlag() != 0 || player.isInDuel() || player.isInCombat() || player.isAttackingNow()))
		{
			player.sendMessage(new CustomMessage("scripts.services.community.careermanager.cannot_use_during_combat", player));
			return false;
		}

		return true;
	}
	
	private void chooseCertificatePage(Player player)
	{
		if(player.getBaseClassId() == player.getClassId().getId())
		{
			player.sendChatMessage(0, ChatType.TELL.ordinal(), "Career Manager", "Switch to subclass to use this function.");
			return;
		}

		String[][] certifications = {
				{ "Level 65 Emergent", "CommunityCert65" },
				{ "Level 70 Emergent", "CommunityCert70" },
				{ "Level 75 Class Specific", "CommunityCert75Class" },
				{ "Level 75 Master", "CommunityCert75Master" },
				{ "Level 80 Divine", "CommunityCert80" } };

		String[] replacements = new String[6 * 2];
		for (int i = 0; i < 6; i++)
		{
			replacements[i * 2] = "%sub" + i + '%';
			if (certifications.length <= i)
				replacements[i * 2 + 1] = "<br>";
			else
			{
				String[] button = certifications[i];
				replacements[i * 2 + 1] = "<button value=\"Add " + button[0] + "\" action=\"bypass -h scripts_Util:" + button[1] + "\" width=200 height=30 back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\" fore=\"L2UI_ct1.OlympiadWnd_DF_Fight1None\">";
			}
		}
		sendFileToPlayer(player, "pages/certifications.htm", replacements);
	}
	
	private void sendFileToPlayer(Player player, String path, String... replacements)
	{
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + path, player);

		for(int i = 0; i < replacements.length; i += 2)
		{
			String toReplace = replacements[i + 1];
			if(toReplace == null)
				toReplace = "<br>";
			html = html.replace(replacements[i], toReplace);
		}

		ShowBoard.separateAndSend(html, player);
	}
	
	public static final CareerManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CareerManager _instance = new CareerManager();
	}
}