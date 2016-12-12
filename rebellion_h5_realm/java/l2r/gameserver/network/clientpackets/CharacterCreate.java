package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.PremiumAccountsTable;
import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.SkillLearn;
import l2r.gameserver.model.actor.instances.player.Bonus;
import l2r.gameserver.model.actor.instances.player.ShortCut;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.serverpackets.CharacterCreateFail;
import l2r.gameserver.network.serverpackets.CharacterCreateSuccess;
import l2r.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.PlayerTemplate;
import l2r.gameserver.templates.item.CreateItem;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Util;

public class CharacterCreate extends L2GameClientPacket
{
	// cSdddddddddddd
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	protected void readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}

	@Override
	protected void runImpl()
	{
		for(ClassId cid : ClassId.VALUES)
			if(cid.getId() == _classId && cid.getLevel() != 1)
				return;
		if(CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= 8)
		{
			sendPacket(CharacterCreateFail.REASON_TOO_MANY_CHARACTERS);
			return;
		}
		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
		{
			sendPacket(CharacterCreateFail.REASON_16_ENG_CHARS);
			return;
		}
		else if(CharacterDAO.getInstance().getObjectIdByName(_name) > 0 || Util.contains(Config.FORBIDDEN_CHAR_NAMES, _name))
		{
			sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}

		Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
			return;

		sendPacket(CharacterCreateSuccess.STATIC);

		initNewChar(getClient(), newChar);
	}

	private void initNewChar(GameClient client, Player newChar)
	{
		PlayerTemplate template = newChar.getTemplate();
		
		if (template == null)
		{
			client.closeNow(false);
			return;
		}
		
		Player.restoreCharSubClasses(newChar);

		if(Config.STARTING_ADENA > 0)
			newChar.addAdena(Config.STARTING_ADENA);
		
		if(Config.STARTING_LVL != 0)
			newChar.addExpAndSp(Experience.LEVEL[Config.STARTING_LVL] - newChar.getExp(), 0, 0, 0, false, false);

        if (Config.SPAWN_CHAR)
            newChar.teleToLocation(Config.SPAWN_X, Config.SPAWN_Y, Config.SPAWN_Z);
           else
           	newChar.setLoc(template.spawnLoc);

		if(Config.CHAR_TITLE)
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		else
			newChar.setTitle("");

		if(Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS && Config.SERVICES_RATE_CREATE_PA != 0 && newChar.getBonus() == null)
		{
			newChar.getBonus().setBonusExpire((int)(System.currentTimeMillis() / 1000L * (60 * 60 * 24 *  Config.SERVICES_RATE_CREATE_PA)));
			newChar.stopBonusTask();
			newChar.startBonusTask();
		}
		
		if (Config.PREMIUM_FOR_NEW_ACC && (PremiumAccountsTable.getPremiumAccount(client.getLogin()) != null && !PremiumAccountsTable.getPremiumAccount(client.getLogin()).isActive()))
			PremiumAccountsTable.savePremium(client.getLogin(), Config.PREMIUM_TEMPLATE_NEW_ACC, (System.currentTimeMillis() + Config.PREMIUM_TIME_FOR_NEW_ACC));
		
		for(CreateItem i : template.getItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getItemId());
			newChar.getInventory().addItem(item);

			if(i.getShortcut() - 1 > -1) // tutorial book
				newChar.registerShortCut(new ShortCut(Math.min(i.getShortcut() - 1, 11), 0, ShortCut.TYPE_ITEM, item.getObjectId(), -1, 1));

			if(i.isEquipable() && item.isEquipable() && (newChar.getActiveWeaponItem() == null || item.getTemplate().getType2() != ItemTemplate.TYPE2_WEAPON))
				newChar.getInventory().equipItem(item);
		}

		ClassId nclassId = ClassId.VALUES[_classId];
		if(Config.ALLOW_START_ITEMS)
		{
			if(nclassId.isMage())
			{
				for(int i = 0; i < Config.START_ITEMS_MAGE.length; i++)
				{
					ItemInstance item = ItemFunctions.createItem(Config.START_ITEMS_MAGE[i]);
					item.setCount(Config.START_ITEMS_MAGE_COUNT[i]);
					newChar.getInventory().addItem(item);
				}
				
				if (Config.BIND_NEWBIE_START_ITEMS_TO_CHAR)
				{
					for(int i = 0; i < Config.START_ITEMS_MAGE_BIND_TO_CHAR.length; i++)
					{
						ItemInstance item = ItemFunctions.createItem(Config.START_ITEMS_MAGE_BIND_TO_CHAR[i]);
						item.setCount(Config.START_ITEMS_MAGE_COUNT_BIND_TO_CHAR[i]);
						item.setCustomFlags(ItemInstance.FLAG_NO_CRYSTALLIZE | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_TRANSFER | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_SELL);
						newChar.getInventory().addItem(item);
					}
				}
			}
			else
			{
				for(int i = 0; i < Config.START_ITEMS_FITHER.length; i++)
				{
					ItemInstance item = ItemFunctions.createItem(Config.START_ITEMS_FITHER[i]);
					item.setCount(Config.START_ITEMS_FITHER_COUNT[i]);
					newChar.getInventory().addItem(item);
				}
				
				if (Config.BIND_NEWBIE_START_ITEMS_TO_CHAR)
				{
					for(int i = 0; i < Config.START_ITEMS_FITHER_BIND_TO_CHAR.length; i++)
					{
						ItemInstance item = ItemFunctions.createItem(Config.START_ITEMS_FITHER_BIND_TO_CHAR[i]);
						item.setCount(Config.START_ITEMS_FITHER_COUNT_BIND_TO_CHAR[i]);
						item.setCustomFlags(ItemInstance.FLAG_NO_CRYSTALLIZE | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_TRANSFER | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_SELL);
						newChar.getInventory().addItem(item);
					}
				}
			}
		}
		
		// Adventurer's Scroll of Escape
		ItemInstance item = ItemFunctions.createItem(10650);
		item.setCount(5);
		newChar.getInventory().addItem(item);

		// Scroll of Escape: Kamael Village
		item = ItemFunctions.createItem(9716);
		item.setCount(10);
		newChar.getInventory().addItem(item);

		for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL))
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);

		if(newChar.getSkillLevel(1001) > 0) // Soul Cry
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1001, 1, 1));
		if(newChar.getSkillLevel(1177) > 0) // Wind Strike
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1177, 1, 1));
		if(newChar.getSkillLevel(1216) > 0) // Self Heal
			newChar.registerShortCut(new ShortCut(2, 0, ShortCut.TYPE_SKILL, 1216, 1, 1));

		// add attack, take, sit shortcut
		newChar.registerShortCut(new ShortCut(0, 0, ShortCut.TYPE_ACTION, 2, -1, 1));
		newChar.registerShortCut(new ShortCut(3, 0, ShortCut.TYPE_ACTION, 5, -1, 1));
		newChar.registerShortCut(new ShortCut(10, 0, ShortCut.TYPE_ACTION, 0, -1, 1));
		// понял как на панельке отобразить. нц софт 10-11 панели сделали(by VISTALL)
		// fly transform
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 911, 1, 1));
		newChar.registerShortCut(new ShortCut(3, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 884, 1, 1));
		newChar.registerShortCut(new ShortCut(4, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 885, 1, 1));
		// air ship
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_AIRSHIP, ShortCut.TYPE_ACTION, 70, 0, 1));

		if (!Config.DISABLE_TUTORIAL)
			startTutorialQuest(newChar);

		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0); // retail
		newChar.setOnlineStatus(false);

		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		
		client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLogin()));
	}

	public static void startTutorialQuest(Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if(q != null)
			q.newQuestState(player, Quest.CREATED);
	}
}