package services.community;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.lang.ArrayUtils;
import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Playable;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.HtmlUtils;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class CommunityBuffer implements ScriptFile, ICommunityBoardHandler
{
    private static final Logger _log = LoggerFactory.getLogger(CommunityBuffer.class);

    public static class BuffSet
    {
        public final CopyOnWriteArrayList<Buff> skills = new CopyOnWriteArrayList<Buff>();

        public void addSkill(int skillid)
        {
            if(buffs.containsKey(skillid))
                skills.add(buffs.get(skillid));
        }
    }

    public static class Buff
    {
        public final Skill skill;
        public final String icon;
        public final int skill_id;

        public Buff(int id)
        {
            int lvl = SkillTable.getInstance().getBaseLevel(id);
            skill_id = id;
            skill = SkillTable.getInstance().getInfo(id, lvl);
            icon = skill.getIcon();
        }
    }

    private static final HashMap<Integer, HashMap<String, BuffSet>> playerBuffSets = new HashMap<Integer, HashMap<String, BuffSet>>();
    private static final HashMap<Integer, Buff> buffs = new HashMap<Integer, Buff>();
    private static final BuffSet all = new BuffSet();

    @Override
	public void onLoad()
	{
		if (Config.COMMUNITYBOARD_ENABLED && Config.BBS_BUFFER_ENABLED)
		{
			playerBuffSets.clear();
			buffs.clear();
			all.skills.clear();
			cleanUP();
			for (int id : buff_ids)
			{
				buffs.put(id, new Buff(id));
				all.addSkill(id);
			}
			CommunityBoardManager.getInstance().registerHandler(this);
			_log.info("CommunityBuffer: Loaded " + buffs.size() + " buffs count.]");
		}
	}

    @Override
	public void onReload()
	{
		if (Config.COMMUNITYBOARD_ENABLED && Config.BBS_BUFFER_ENABLED)
		{
			playerBuffSets.clear();
			buffs.clear();
			all.skills.clear();
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}

    @Override
    public void onShutdown()
    {}

    @Override
    public String[] getBypassCommands()
    {
        return new String[] { "_cbbsbuffer" };
    }

    @Override
    public void onBypassCommand(Player player, String bypass)
    {
        if (!checkPlayer(player))
            return;

        String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/buffer/buff2.htm", player);
        String content = "";
        if(bypass.startsWith("_cbbsbuffer"))
        {
            StringTokenizer bf = new StringTokenizer(bypass, " ");
            bf.nextToken();
            String[] arg = new String[0];
            while(bf.hasMoreTokens())
                arg = ArrayUtils.add(arg, bf.nextToken());

            content = BuffList(arg, player);
        }
        html = html.replace("%content%", content);
        ShowBoard.separateAndSend(html, player);
    }


	public static boolean checkPlayer(Player player)
	{
		if (!Config.BBS_BUFF_DEATH && (player.isDead() || player.isAlikeDead() || player.isFakeDeath()))
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_while_dead", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_ACTION && (player.isCastingNow() || player.isInCombat() || player.isAttackingNow()) && (!player.isGM() && player.isInvisible()))
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_while_casting_combat_attacking", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_OLY && player.isInOlympiadMode())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_inolymode", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_FLY && (player.isFlying() || player.isInFlyingTransform()))
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_during_flight_transform", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_VEICHLE && player.isInBoat())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_on_boat", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_MOUNTED && player.isMounted())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_mounted", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_CANT_MOVE && (player.isMovementDisabled() || player.isParalyzed() || player.isStunned() || player.isSleeping() || player.isRooted() || player.isImmobilized()))
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_mdisab_paralyz_stun_sleep_rooted_immobil", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_STORE_MODE && (player.isInStoreMode() || player.isInTrade() || player.isInOfflineMode()))
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_trade_store_offline", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_FISHING && player.isFishing())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_fisshing", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_TEMP_ACTION && (player.isLogoutStarted() || player.isTeleporting()))
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_logout_teleport", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_DUEL && player.isInDuel())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_induel", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_CURSED && player.isCursedWeaponEquipped())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_withcursed", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_PK && player.getKarma() > 0)
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_withkarma", player));
			return false;
		}
		
		if (Config.BBS_BUFF_LEADER && !player.isClanLeader())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.only_clan_leader", player));
			return false;
		}
		
		if (Config.BBS_BUFF_NOBLE && !player.isNoble())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.only_noble", player));
			return false;
		}
		
		if (!Config.BBS_BUFF_TERITORY && player.isOnSiegeField())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.cannot_use_duringsiege", player));
			return false;
		}
		
		if (Config.BBS_BUFF_PEACEZONE_ONLY && !player.isInZonePeace())
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.OnlyOnPeaceZone", player));
			return false;
		}
		
		if (player.getVarInt("jailed") > 0)
		{
			player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.NotInJail", player));
			return false;
		}
		
		return true;
	}


    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
    {}

    public static String htmlButton(String value, int width, int height, Object... args)
    {
        String action = "bypass _cbbsbuffer";
        for(Object arg : args)
            action += " " + arg.toString();
        return HtmlUtils.htmlButton(value, action, width, height);
    }

    private static boolean takeItemsAndBuff(Playable player, List<Buff> buffs, boolean toPet)
    {
        int needCount = Config.BBS_BUFF_ITEM_COUNT * buffs.size();

        if(player.getLevel() > Config.BBS_BUFF_FREE_LVL && Functions.getItemCount(player, Config.BBS_BUFF_ITEM_ID) < needCount)
            return false;

        Playable target = toPet ? player.getPet() : player;
        if(target != null)
        {
            try
            {
                if(player.getLevel() > Config.BBS_BUFF_FREE_LVL)
                    Functions.removeItem(player, Config.BBS_BUFF_ITEM_ID, needCount);
            }
            catch(Exception e)
            {
                return false;
            }
            for(Buff nextbuff : buffs)
            {
                if(nextbuff.skill.isMusic())
                    //songs and dances
                    nextbuff.skill.getEffects(target, target, false, false, Config.BBS_BUFF_TIME * 1000, false);
                    //for special skill that last less than 20min
                else if(nextbuff.skill.getId() == 1355 || nextbuff.skill.getId() == 1356 || nextbuff.skill.getId() == 1357 || nextbuff.skill.getId() == 1363 || nextbuff.skill.getId() == 1413 || nextbuff.skill.getId() == 1414)
                    nextbuff.skill.getEffects(target, target, false, false, Config.BBS_BUFF_TIME_SPECIAL * 1000, false);
                    //normal buff
                else
                    nextbuff.skill.getEffects(target, target, false, false, Config.BBS_BUFF_TIME * 1000, false);
            }
        }
        return true;
    }

    private static int getSkillIdx(BuffSet set, int skill_id)
    {
        for(int i = 0; i < set.skills.size(); i++)
            if(set.skills.get(i).skill_id == skill_id)
                return i;
        return -1;
    }

    private static String pageGet(Player player, String[] var)
    {
        boolean buffallset = var[1].equalsIgnoreCase("0") || var[1].equalsIgnoreCase("2");
        String[] var2 = new String[var.length - (buffallset ? 1 : 2)];
        System.arraycopy(var, var.length - var2.length, var2, 0, var2.length);
        List<Buff> buffs_to_buff = new ArrayList<Buff>();

        if(buffallset)
        {
            String[] a = var[2].split("_");
            int listid = a[0].equalsIgnoreCase("2") ? player.getObjectId() : 0;
            String name = Strings.joinStrings(" ", var, 3);
            String localized_name = name;
            HashMap<String, BuffSet> sets = getBuffSets(listid);
            if(listid == 0)
            {
                String[] langs = name.split(";");
                if(langs.length == 2)
                    localized_name = langs[player.isLangRus() ? 1 : 0];
            }
            if(!sets.containsKey(name))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Набор '" + localized_name + "' не найден</font></center>";
                else
                    return "<center><font color=FF3355>'" + localized_name + "' set not found</font></center>";
            buffs_to_buff.addAll(sets.get(name).skills);
        }
        else
            buffs_to_buff.add(buffs.get(Integer.parseInt(var[2])));

        if(!takeItemsAndBuff(player, buffs_to_buff, var[1].equalsIgnoreCase("2")))
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);

        return pageList(player, var2);
    }

    private static final int pageRows = Config.BUFF_PAGE_ROWS;
    private static final int pageCols = Config.MAX_BUFF_PER_SET;
    private static final int pageMax = pageRows * pageCols;

    private static String pageList(Player player, String[] var)
    {
        String[] a = var[1].split("_");
        int pageIdx = Integer.parseInt(a[1]);
        boolean _all = a[0].equalsIgnoreCase("0");
        int listid = a[0].equalsIgnoreCase("2") ? player.getObjectId() : 0;
        String name = "Все баффы";
        if(!player.isLangRus())
            name = "All buffs";
        String param1 = Strings.joinStrings(" ", var, 1);
        BuffSet set = all;

        String localized_name = name;
        if(!_all)
        {
            HashMap<String, BuffSet> sets = getBuffSets(listid);
            name = Strings.joinStrings(" ", var, 2);
            localized_name = name;
            if(listid == 0)
            {
                String[] langs = name.split(";");
                if(langs.length == 2)
                    localized_name = langs[player.isLangRus() ? 1 : 0];
            }
            if(!sets.containsKey(name))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Набор '" + localized_name + "' не найден</font></center>";
                else
                    return "<center><font color=FF3355>'" + localized_name + "' set not found</font></center>";
            set = sets.get(name);
        }

        String pagePrev = pageIdx == 0 ? "" : htmlButton("&$543;", 80, 22, "list", param1.replaceFirst(var[1], a[0] + "_" + (pageIdx - 1)));
        String pageNext = "";
        List<String> tds = new ArrayList<String>();

        for(int i = pageIdx * pageMax; i < set.skills.size(); i++)
        {
            if(tds.size() == pageMax)
            {
                pageNext = htmlButton("&$544;", 80, 22, "list", param1.replaceFirst(var[1], a[0] + "_" + (pageIdx + 1)));
                break;
            }
            Buff _buff = set.skills.get(i);
            String buff_str = "<td width=32 valign=top><img src=\"" + _buff.icon + "\" width=32 height=32></td>";
            buff_str += "<td>" + htmlButton("$", 22, 32, "get", 1, _buff.skill_id, param1) + "</td>";
            if(player.isLangRus())
                buff_str += "<td fixwidth=140><font color=3399FF>" + _buff.skill.getName() + "</font><br1><font color=LEVEL> Уровень " + _buff.skill.getLevel() + "</font></td>";
            else
                buff_str += "<td fixwidth=140><font color=3399FF>" + _buff.skill.getName() + "</font><br1><font color=LEVEL> Level " + _buff.skill.getLevel() + "</font></td>";
            tds.add(buff_str);
        }

        String result = "";

        int cost;
        if(player.getLevel() > Config.BBS_BUFF_FREE_LVL)
            cost = set.skills.size() * Config.BBS_BUFF_ITEM_COUNT;
        else
            cost = 0;
        result += "<table><tr>";
        String all = "All";
        if(player.isLangRus())
            all = "Все";
        result += "<td width=200 align=center><font color=33FF55>" + localized_name + (!_all && pageNext.isEmpty() && pagePrev.isEmpty() ? " [" + all +": " + cost + " " + HtmlUtils.htmlItemName(Config.BBS_BUFF_ITEM_ID) + "]" : "") + "</font></td>";
        if(!_all && pageNext.isEmpty() && pagePrev.isEmpty())
        {
            if(player.isLangRus())
            {
                result += "<td width=70>Себе: ";
                if(player.getPet() != null)
                    result += "<br>Питомцу: ";
            }
            else
            {
                result += "<td width=70>For me: ";
                if(player.getPet() != null)
                    result += "<br>For pet: ";
            }
            result += "</td>";

            result += "<td>";
            if(player.isLangRus())
            {
                result += htmlButton("Все", 50, 22, "get", 0, param1);
                if(player.getPet() != null)
                    result += "<br>" + htmlButton("Все", 50, 22, "get", 2, param1);
            }
            else
            {
                result += htmlButton("All", 50, 22, "get", 0, param1);
                if(player.getPet() != null)
                    result += "<br>" + htmlButton("All", 50, 22, "get", 2, param1);
            }
            result += "</td>";
        }
        if(listid != 0)
            if(player.isLangRus())
                result += "<td width=140 align=center>" + htmlButton("Редактировать", 125, 22, "editset", "edit", name) + "</td>";
            else
                result += "<td width=140 align=center>" + htmlButton("Edit", 125, 22, "editset", "edit", name) + "</td>";
        if(!pagePrev.isEmpty() || !pageNext.isEmpty())
        {
            result += "<td width=90 align=center>" + pagePrev + "</td>";
            result += "<td width=60 align=center>Page: " + (pageIdx + 1) + "</td>";
            result += "<td width=90 align=center>" + pageNext + "</td>";
        }
        result += "</tr></table>";

        if(tds.size() > 0)
        {
            result += "<br><img src=\"L2UI.SquareWhite\" width=600 height=1><br><table>";
            result += formatTable(tds, pageCols, false);
            result += "</table>";
        }

        return result;
    }

    private static String pageEdit(Player player, String[] var)
    {
        int charId = player.getObjectId();
        HashMap<String, BuffSet> sets = getBuffSets(charId);
        String name = "";

        if(var[1].equalsIgnoreCase("del"))
        {
            Log.addGame("BUFF\tУдален набор: " + name, "service_buff", player);
            name = Strings.joinStrings(" ", var, 2);
            deleteBuffSet(charId, name);
            sets.remove(name);
            return pageMain(player);
        }

        String result = "";
        List<String> tds = new ArrayList<String>();

        if(var[1].equalsIgnoreCase("delconf"))
        {
            name = Strings.joinStrings(" ", var, 2);
            if(player.isLangRus())
            {
                tds.add(htmlButton("ДА", 50, 22, "editset", "del", name));
                tds.add(htmlButton("НЕТ", 50, 22, "editset", "edit", name));
            }
            else
            {
                tds.add(htmlButton("YES", 50, 22, "editset", "del", name));
                tds.add(htmlButton("NO", 50, 22, "editset", "edit", name));
            }

            if(player.isLangRus())
                result += "<center><font color=FF3355>Вы действительно желаете удалить набор: " + name + "?</font><br><table>";
            else
                result += "<center><font color=FF3355>Are you sure you want to delete a set: " + name + "?</font><br><table>";
            result += formatTable(tds, 2, true);
            result += "</table></center>";
            return result;
        }

        BuffSet set = null;

        if(var[1].equalsIgnoreCase("new"))
        {
            if(sets.size() >= Config.MAX_SETS_PER_CHAR)
                if(player.isLangRus())
                    return "<center><font color=FF3355>Вы достигли лимита наборов</font></center>";
                else
                    return "<center><font color=FF3355>You have reached the limit set</font></center>";
            name = trimHtml(Strings.joinStrings(" ", var, 2));
            if(name.length() > 16)
                name = name.substring(0, 15);
            if(name.isEmpty() || name.equalsIgnoreCase(" "))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Необходимо указать имя набора</font></center>";
                else
                    return "<center><font color=FF3355>You must specify the name of the set</font></center>";
            set = new BuffSet();
            sets.put(name, set);
            updateBuffSet(charId, name, set);
            Log.addGame("BUFF\tСоздан набор: " + name, "service_buff", player);
        }
        else if(var[1].equalsIgnoreCase("edit"))
        {
            name = Strings.joinStrings(" ", var, 2);
            if(!sets.containsKey(name))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Набор '" + name + "' не найден</font></center>";
                else
                    return "<center><font color=FF3355>'" + name + "' set not found</font></center>";
            set = sets.get(name);
        }
        else if(var[1].equalsIgnoreCase("rem"))
        {
            name = Strings.joinStrings(" ", var, 3);
            if(!sets.containsKey(name))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Набор '" + name + "' не найден</font></center>";
                else
                    return "<center><font color=FF3355>'" + name + "' set not found</font></center>";
            set = sets.get(name);
            int skill_to_remove = Integer.valueOf(var[2]);
            int idx = getSkillIdx(set, skill_to_remove);
            if(idx != -1)
                set.skills.remove(idx);
            updateBuffSet(charId, name, set);
        }
        else if(var[1].equalsIgnoreCase("add"))
        {
            name = Strings.joinStrings(" ", var, var[2].equalsIgnoreCase("x") ? 4 : 3);
            if(!sets.containsKey(name))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Набор '" + name + "' не найден</font></center>";
                else
                    return "<center><font color=FF3355>'" + name + "' set not found</font></center>";
            set = sets.get(name);
            if(var[2].equalsIgnoreCase("x"))
            {
                set.addSkill(Integer.valueOf(var[3]));
                updateBuffSet(charId, name, set);
            }
            else
            {
                int pageIdx = Integer.valueOf(var[2]);
                String pagePrev = pageIdx == 0 ? "" : htmlButton("&$543;", 80, 22, "editset", "add", pageIdx - 1, name);
                String pageNext = "";
                for(int i = pageIdx * pageMax; i < all.skills.size(); i++)
                {
                    if(tds.size() == pageMax)
                    {
                        pageNext = htmlButton("&$544;", 80, 22, "editset", "add", pageIdx + 1, name);
                        break;
                    }
                    Buff _buff = all.skills.get(i);
                    int idx = getSkillIdx(set, _buff.skill_id);
                    if(idx != -1)
                        continue;
                    String buff_str = "<td width=32 valign=top><img src=\"" + _buff.icon + "\" width=32 height=32></td>";
                    buff_str += "<td>" + htmlButton(">", 22, 32, "editset", "add", "x", _buff.skill_id, name) + "</td>";
                    if(player.isLangRus())
                        buff_str += "<td fixwidth=140><font color=3399FF>" + _buff.skill.getName() + "</font><br1><font color=LEVEL> Уровень " + _buff.skill.getLevel() + "</font></td>";
                    else
                        buff_str += "<td fixwidth=140><font color=3399FF>" + _buff.skill.getName() + "</font><br1><font color=LEVEL> Level " + _buff.skill.getLevel() + "</font></td>";
                    tds.add(buff_str);
                }

                result += "<table><tr>";
                if(player.isLangRus())
                    result += "<td width=300 align=center><font color=33FF55>Редактирование набора: " + name + "</font></td>";
                else
                    result += "<td width=300 align=center><font color=33FF55>Set editing: " + name + "</font></td>";
                if(!pagePrev.isEmpty() || !pageNext.isEmpty())
                {
                    result += "<td width=90 align=center>" + pagePrev + "</td>";
                    result += "<td width=60 align=center>Page: " + (pageIdx + 1) + "</td>";
                    result += "<td width=90 align=center>" + pageNext + "</td>";
                }
                result += "</tr></table>";

                result += "<img src=\"L2UI.SquareWhite\" width=600 height=1><br><table>";
                result += formatTable(tds, pageCols, false);
                result += "</table>";

                return result;
            }
        }
        else
            return pageMain(player);

        for(int i = 0; i < set.skills.size(); i++)
        {
            Buff _buff = set.skills.get(i);
            String buff_str = "<td width=32 valign=top><img src=\"" + _buff.icon + "\" width=32 height=32></td>";
            buff_str += "<td>" + htmlButton("<", 22, 32, "editset", "rem", _buff.skill_id, name) + "</td>";
            if(player.isLangRus())
                buff_str += "<td fixwidth=140><font color=3399FF>" + _buff.skill.getName() + "</font><br1><font color=LEVEL> Уровень " + _buff.skill.getLevel() + "</font></td>";
            else
                buff_str += "<td fixwidth=140><font color=3399FF>" + _buff.skill.getName() + "</font><br1><font color=LEVEL> Level " + _buff.skill.getLevel() + "</font></td>";
            tds.add(buff_str);
        }

        result += "<table><tr>";
        if(player.isLangRus())
        {
            result += "<td width=300 align=center><font color=33FF55>Редактирование: " + name + "</font></td>";
            if(set.skills.size() < pageMax)
                result += "<td width=150 align=center>" + htmlButton("Добавить бафф", 130, 22, "editset", "add", 0, name) + "</td>";
            result += "<td width=150 align=center>" + htmlButton("Удалить набор", 130, 22, "editset", "delconf", name) + "</td>";
            result += "<td width=90 align=center>" + htmlButton("Возврат", 80, 22, "list", "2_0", name) + "</td>";
            result += "</tr></table>";
        }
        else
        {
            result += "<td width=300 align=center><font color=33FF55>Editing: " + name + "</font></td>";
            if(set.skills.size() < pageMax)
                result += "<td width=150 align=center>" + htmlButton("Add buff", 130, 22, "editset", "add", 0, name) + "</td>";
            result += "<td width=150 align=center>" + htmlButton("Delete set", 130, 22, "editset", "delconf", name) + "</td>";
            result += "<td width=90 align=center>" + htmlButton("Return", 80, 22, "list", "2_0", name) + "</td>";
            result += "</tr></table>";
        }

        if(tds.size() > 0)
        {
            result += "<img src=\"L2UI.SquareWhite\" width=600 height=1><br><table>";
            result += formatTable(tds, pageCols, false);
            result += "</table>";
        }

        return result;
    }

    private static String pageMain(Player player)
    {
        String result = "<table width=600><tr>";

        result += "<td align=\"center\" valign=\"top\">Выберите категорию:<br>";
        result += htmlButton(player.isLangRus() ? "Все Баффы" : "All Buffs", 175, 25, "list", "0_0") + "<br1>";
        HashMap<String, BuffSet> sets = getBuffSets(0);
        for(String setname : sets.keySet())
        {
            String name = setname;
            String[] langs = setname.split(";");
            if(langs.length == 2)
                name = langs[player.isLangRus() ? 1 : 0];
            result += htmlButton(name, 175, 25, "list", "1_0", setname) + "<br1>";
        }
        if (Config.BBS_BUFF_ALLOW_CANCEL || Config.BBS_BUFF_ALLOW_HEAL) {
            result += "<br><br>Выберите действие:<br>";
            if (Config.BBS_BUFF_ALLOW_HEAL)
                result += htmlButton(player.isLangRus() ? "Восстановить статы" : "Reset stats", 175, 25, "heal") + "<br1>";
            if (Config.BBS_BUFF_ALLOW_CANCEL)
                result += htmlButton(player.isLangRus() ? "Сбросить бафы" : "Cancel", 175, 25, "cancel") + "<br1>";
        }
        result += "</td>";
            result += "<td align=\"right\"><table width=300 background=\"L2UI_CH3.refinewnd_back_Pattern\"><tr><td align=\"center\"><br><font color=\"ff8000\">";
            result += player.isLangRus() ? "Новый профиль:</font><br1>" : "New profile:</font><br1>";
            result += player.isLangRus() ? "Введите название:<br1>" : "Enter name:<br1>";
            result += "<edit var=\"name\" width=150 length=10><br>";
            result += htmlButton(player.isLangRus() ? "Сохранить" : "Save", 75, 25, "save", "$name") + "<br>";
            result += "<img src=\"L2UI.SquareWhite\" width=230 height=1><br><br><font color=\"ff8000\">";
            result += player.isLangRus() ? "Ваши наборы" : "Your sets";
            result += "</font><br1>";
            result += player.isLangRus() ? "Выберите цель:<br1>" : "Select target:<br1>";
            if (player.getPet() != null)
                result += "<combobox width=\"80\" var=\"trg\" list=\"" + (player.isLangRus() ? "Персонаж;Питомец" : "Player;Pet") + "\"><br1>";
            else
                result += "<combobox width=\"80\" var=\"trg\" list=\"" + (player.isLangRus() ? "Персонаж" : "Player") + "\"><br1>";
            result += "<img src=\"L2UI_CT1.MiniMap_DF_ICN_Center\" width=32 height=32><br1>";
            result += player.isLangRus() ? "Выберите набор:" : "Select set:";
            sets = getBuffSets(player.getObjectId());
            String sets_text = "";
            int i = 0;
            for(String setname : sets.keySet()) {
                sets_text = (i != 0 ? sets_text + ";" : "") + setname;
                i++;
            }
            result += "<combobox width=\"120\" var=\"cheme\" list=\""+sets_text+"\"><br1><img src=\"L2UI_CT1.MiniMap_DF_ICN_Center\" width=32 height=32><br1>";
            result += player.isLangRus() ? "Выберите действие:<br1>" : "Select action:<br1>";
            result += "<table><tr><td align=\"center\">";
            result += htmlButton(player.isLangRus() ? "Наложить" : "Apply", 75, 25, "apply", "$cheme", "$trg");
            result += "</td><td align=\"center\">";
            result += htmlButton(player.isLangRus() ? "Удалить" : "Delete", 75, 25, "delete", "$cheme");
            result += "</td></tr></table><br></td></tr></table></td>";
            result += "</tr></table><br>";
        return result;
    }

    public String BuffList(String[] var, Player player)
    {
        if(player.isLangRus())
        {
            if(player.isInOlympiadMode())
                return "Эта функция недоступна на олимпиаде";
            if(player.isInCombat())
                return "Эта функция недоступна во время боя";
        }
        else
        {
            if(player.isInOlympiadMode())
                return "This feature is not available at the Olympiad Game";
            if(player.isInCombat())
                return "This feature is not available during the battle";
        }

        if(var[0].equalsIgnoreCase("get"))
            return pageGet(player, var);

        if(var[0].equalsIgnoreCase("list"))
            return pageList(player, var);

        if(var[0].equalsIgnoreCase("editset") && var.length > 1)
            return pageEdit(player, var);

        if(var[0].equalsIgnoreCase("save") && var.length > 1) {
            int charId = player.getObjectId();
            HashMap<String, BuffSet> sets = getBuffSets(charId);
            String name = "";
            BuffSet set = null;
            if(sets.size() >= Config.MAX_SETS_PER_CHAR)
                if(player.isLangRus())
                    return "<center><font color=FF3355>Вы достигли лимита наборов</font></center>";
                else
                    return "<center><font color=FF3355>You have reached the limit set</font></center>";
            name = var[1].replace(" ", "");
            if(name.length() > 16)
                name = name.substring(0, 15);
            if(name.isEmpty() || name.equalsIgnoreCase(" "))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Необходимо указать имя набора</font></center>";
                else
                    return "<center><font color=FF3355>You must specify the name of the set</font></center>";
            if(sets.containsKey(name))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Набор '" + name + "' уже существует</font></center>";
                else
                    return "<center><font color=FF3355>Set '" + name + "' already exists</font></center>";
            set = new BuffSet();
            if (!Config.BUFF_MANUAL_EDIT_SETS)
                for(Effect e : player.getEffectList().getAllEffects())
                    set.addSkill(e.getSkill().getId());
            sets.put(name, set);
            updateBuffSet(charId, name, set);
            Log.addGame("BUFF\tСоздан набор: " + name, "service_buff", player);

            return pageMain(player);
        }

        if(var[0].equalsIgnoreCase("apply") && var.length > 1) {
            List<Buff> buffs_to_buff = new ArrayList<Buff>();
            String name;
            String trg;
            try {
                name = var[1].trim();
                trg = var[2].trim();
            } catch (ArrayIndexOutOfBoundsException error) {
                return "<center><font color=FF3355>Неверное имя набора</font></center";
            }
            HashMap<String, BuffSet> sets = getBuffSets(player.getObjectId());
            if(!sets.containsKey(name))
                if(player.isLangRus())
                    return "<center><font color=FF3355>Набор '" + name + "' не найден</font></center>";
                else
                    return "<center><font color=FF3355>'" + name + "' set not found</font></center>";
            buffs_to_buff.addAll(sets.get(name).skills);

            if(!takeItemsAndBuff(player, buffs_to_buff, trg.equalsIgnoreCase("питомец") || trg.equalsIgnoreCase("pet")))
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);

            return pageMain(player);
        }

        if(var[0].equalsIgnoreCase("delete") && var.length > 1) {
            int charId = player.getObjectId();
            HashMap<String, BuffSet> sets = getBuffSets(charId);
            String name = "";
            Log.addGame("BUFF\tУдален набор: " + name, "service_buff", player);
            name = Strings.joinStrings(" ", var, 1);
            deleteBuffSet(charId, name);
            sets.remove(name);
            return pageMain(player);
        }

        if(var[0].equalsIgnoreCase("heal")) {
            if (Config.BBS_BUFF_ALLOW_HEAL)
                if (player.isInZonePeace()) {
                    player.setCurrentCp(player.getMaxCp());
                    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
                    player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.stats_restored", player));
                } else
                	player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.restore_in_peacezone", player));
            else
            	player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.restore_disabled", player));
            return pageMain(player);
        }

        if(var[0].equalsIgnoreCase("cancel")) {
            if (Config.BBS_BUFF_ALLOW_CANCEL) {
                player.getEffectList().stopAllEffects();
                player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.stats_reset", player));
            } else
            	player.sendMessage(new CustomMessage("scripts.services.community.communitybuffer.func_not_available", player));
            return pageMain(player);
        }

        return pageMain(player);
    }

    private static synchronized HashMap<String, BuffSet> getBuffSets(int charId)
    {
        if(playerBuffSets.containsKey(charId))
            return playerBuffSets.get(charId);

        HashMap<String, BuffSet> _new = loadBuffSets(charId);
        playerBuffSets.put(charId, _new);
        return _new;
    }

    private static void updateBuffSet(int charId, String setname, BuffSet _set)
    {
        String skills = _set.skills.size() == 0 ? "" : String.valueOf(_set.skills.get(0).skill_id);
        for(int i = 1; i < _set.skills.size(); i++)
            skills += "," + _set.skills.get(i).skill_id;

        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO bbs_buffs (char_id,name,skills) VALUES (?,?,?)");
            statement.setInt(1, charId);
            statement.setString(2, setname);
            statement.setString(3, skills);
            statement.execute();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static void deleteBuffSet(int charId, String setname)
    {
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM bbs_buffs WHERE char_id=? AND name=?");
            statement.setInt(1, charId);
            statement.setString(2, setname);
            statement.execute();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static HashMap<String, BuffSet> loadBuffSets(int charId)
    {
        HashMap<String, BuffSet> result = new HashMap<String, BuffSet>();

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT name,skills FROM bbs_buffs WHERE char_id=?");
            statement.setInt(1, charId);
            rset = statement.executeQuery();
            while(rset.next())
            {
                BuffSet next_set = new BuffSet();
                String skills = rset.getString("skills");
                if(skills != null && !skills.isEmpty())
                    if(!skills.contains(","))
                        next_set.addSkill(Integer.parseInt(skills));
                    else
                    {
                        String[] skill_ids = skills.split(",");
                        for(String skill_id : skill_ids)
                            if(!skill_id.isEmpty())
                                next_set.addSkill(Integer.parseInt(skill_id));
                    }

                result.put(rset.getString("name"), next_set);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return result;
    }

    private static void cleanUP()
    {
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM `bbs_buffs` WHERE char_id != 0 AND char_id NOT IN(SELECT obj_id FROM characters);");
            statement.executeUpdate();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static String formatTable(Collection<String> tds, int rows, boolean appendTD)
    {
        String result = "";
        int i = 0;
        for(String td : tds)
        {
            if(i == 0)
                result += "<tr>";
            result += appendTD ? "<td>" + td + "</td>" : td;
            i++;
            if(i == rows)
            {
                result += "</tr>";
                i = 0;
            }
        }
        if(i > 0 && i < rows)
        {
            while(i < rows)
            {
                result += "<td></td>";
                i++;
            }
            result += "</tr>";
        }
        return result;
    }

    /**
     * кроме обычного trim, заменяет кавычки на нестандартные UTF-8, удяляет ВСЕ двойные пробелы, убирает символы <>
     */
    private static String trimHtml(String s)
    {
        int i;
        s = s.trim().replaceAll("\"", "?").replaceAll("'", "?").replaceAll("<", "").replaceAll(">", "");
        do
        {
            i = s.length();
            s = s.replaceAll("  ", " ");
        }
        while(i > s.length());

        return s;
    }

    private static int buff_ids[] = {

    	1499,
    	1500,
    	1501,
    	1502,
    	1503,
    	1504,
    	1519,
    	1040,
    	4345,
    	4355,
    	4356,
    	4357,
    	4358,
    	4359,
    	4360,
    	1303,
    	1388,
    	1389,
    	4349,
    	1087,
    	1243,
    	1542,
    	1364,
    	1461,
    	4346,
    	4350,
    	1032,
    	1548,
    	1033,
    	1182,
    	1189,
    	1191,
    	1352,
    	1353,
    	1354,
    	1392,
    	1393,
    	4342,
    	4347,
    	4348,
    	4351,
    	4352,
    	4354,
    	1284,
    	1397,
    	1257,
    	1044,
    	1460,
    	1073,
    	264,
    	265,
    	266,
    	267,
    	268,
    	269,
    	270,
    	304,
    	305,
    	306,
    	308,
    	349,
    	363,
    	364,
    	529,
    	271,
    	272,
    	273,
    	274,
    	275,
    	276,
    	277,
    	307,
    	309,
    	310,
    	311,
    	365,
    	366,
    	530,
    	915,
    	1355,
    	1356,
    	1357,
    	1363,
    	1413,
    	1414,
    	4699,
    	4700,
    	4702,
    	4703,
    	825,
    	826,
    	827,
    	828,
    	829,
    	830
    };
}