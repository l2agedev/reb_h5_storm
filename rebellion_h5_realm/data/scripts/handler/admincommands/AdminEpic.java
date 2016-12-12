package handler.admincommands;

import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.templates.npc.NpcTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import bosses.BaiumManager;
import bosses.EpicBossState;

public class AdminEpic implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_epic,
		admin_epic_edit
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player player)
	{
		Commands command = (Commands) comm;
		StringTokenizer st = new StringTokenizer(fullString);
		switch (command)
		{
			case admin_epic:
				st.nextToken();
				if (st.hasMoreTokens())
					showEpicEditPage(player, Integer.parseInt(st.nextToken()));
				else
					showEpicIndexPage(player);
				break;
			case admin_epic_edit:
				st.nextToken();
				int boss = Integer.parseInt(st.nextToken());
				EpicBossState state = EpicBossState.getState(boss);
				if (state == null)
				{
					player.sendMessage("Error: AdminEpic.edit -> Can't find state for boss id " + boss);
					return false;
				}
				
				Calendar calendar = (Calendar) Calendar.getInstance().clone();
				for (int i = 2; i < wordList.length; i++)
				{
					int val = Integer.parseInt(wordList[i]);
					int type = 0;
					switch (i)
					{
						case 2:
							type = 11;
							break;
						case 3:
							type = 12;
							break;
						case 4:
							type = 5;
							break;
						case 5:
							type = 2;
							val--;
							break;
						case 6:
							type = 1;
							break;
					}
					calendar.set(type, val);
				}
				calendar.set(Calendar.SECOND, 0);
				if (calendar.getTimeInMillis() <= System.currentTimeMillis())
				{
					state.setState(EpicBossState.State.NOTSPAWN);
					state.setRespawnDateFull(System.currentTimeMillis());
					if (state.getBossId() == 29020)
					{
						NpcInstance baiumNpc = GameObjectsStorage.getByNpcId(29025);
						if (baiumNpc == null)
							BaiumManager.spawnBaium();
					}
				}
				else
				{
					state.setRespawnDateFull(calendar.getTimeInMillis());
					state.setState(EpicBossState.State.INTERVAL);
				}
				
				state.update();
				useAdminCommand(Commands.admin_epic, null, "admin_epic " + boss, player);
		}
		return true;
	}
	
	private void showEpicIndexPage(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		
		String index = HtmCache.getInstance().getNotNull("admin/epic/index.htm", player);
		
		int i = 1;
		for (EpicBossState epic : EpicBossState.getEpicsList())
		{
			int id = epic.getBossId();
			NpcTemplate template = NpcHolder.getInstance().getTemplate(id);
			
			index = index.replace("<?id_" + i + "?>", String.valueOf(id));
			index = index.replace("<?name_" + i + "?>", template.getName());
			index = index.replace("<?state_" + i + "?>", getStatusNote(epic.getState()));
			
			i++;
		}
		
		html.setHtml(index);
		player.sendPacket(html);
	}
	
	private void showEpicEditPage(Player player, int _id)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		
		String epic = HtmCache.getInstance().getNotNull("admin/epic/edit.htm", player);
		EpicBossState boss = EpicBossState.getState(_id);
		
		int id = boss.getBossId();
		NpcTemplate template = NpcHolder.getInstance().getTemplate(id);
		
		epic = epic.replace("<?id?>", String.valueOf(id));
		
		epic = epic.replace("<?name?>", template.getName());
		epic = epic.replace("<?state?>", getStatusNote(boss.getState()));
		long time = boss.getRespawnDate();
		if (time > 0L)
			epic = epic.replace("<?resp?>", new SimpleDateFormat("HH:mm:ss dd/MM/yyyy ").format(new Date(time)));
		else
			epic = epic.replace("<?resp?>", "<font color=\"LEVEL\">...</font>");
		html.setHtml(epic);
		player.sendPacket(html);
	}
	
	private String getStatusNote(EpicBossState.State state)
	{
		switch (state)
		{
			case NOTSPAWN:
				return "<font color=\"CC3333\">Under Attack</font>";
			case ALIVE:
				return "<font color=\"99CC33\">Alive</font>";
			case DEAD:
			case INTERVAL:
				return "<font color=\"FF3333\">Death</font>";
		}
		return null;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	@Override
	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}
}
