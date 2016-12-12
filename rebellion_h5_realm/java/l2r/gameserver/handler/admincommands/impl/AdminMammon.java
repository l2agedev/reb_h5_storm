package l2r.gameserver.handler.admincommands.impl;

import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Spawner;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unused")
public class AdminMammon implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_find_mammon,
		admin_show_mammon,
		admin_hide_mammon,
		admin_list_spawns
	}

	List<Integer> npcIds = new ArrayList<Integer>();

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		npcIds.clear();

		if(fullString.startsWith("admin_find_mammon"))
		{
			npcIds.add(31113);
			npcIds.add(31126);
			npcIds.add(31092); // Add the Marketeer of Mammon also
			int teleportIndex = -1;

			try
			{
				if(fullString.length() > 16)
					teleportIndex = Integer.parseInt(fullString.substring(18));
			}
			catch(Exception NumberFormatException)
			{
				// activeChar.sendPacket(SystemMessage.sendString("Command format is
				// //find_mammon <teleportIndex>"));
			}

			findAdminNPCs(activeChar, npcIds, teleportIndex, -1);
		}

		else if(fullString.equals("admin_show_mammon"))
		{
			npcIds.add(31113);
			npcIds.add(31126);

			findAdminNPCs(activeChar, npcIds, -1, 1);
		}

		else if(fullString.equals("admin_hide_mammon"))
		{
			npcIds.add(31113);
			npcIds.add(31126);

			findAdminNPCs(activeChar, npcIds, -1, 0);
		}

		else if(fullString.startsWith("admin_list_spawns"))
		{
			int npcId = 0;

			try
			{
				npcId = Integer.parseInt(fullString.substring(18).trim());
			}
			catch(Exception NumberFormatException)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminmammon.message1", activeChar));
			}

			if (npcId == 0)
			{
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.handler.admincommands.impl.adminmammon.message2", activeChar));
				return false;
			}
			
			npcIds.add(npcId);
			showListSpawns(activeChar, npcId);
			//findAdminNPCs(activeChar, npcIds, -1, -1);
		}

		// Used for testing SystemMessage IDs - Use //msg <ID>
		else if(fullString.startsWith("admin_msg"))
			activeChar.sendPacket(new SystemMessage2().addInteger(Integer.parseInt(fullString.substring(10).trim())));

		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void showListSpawns(Player player, int npcId)
	{
		NpcTemplate tmpl = NpcHolder.getInstance().getTemplate(npcId);
		if (tmpl == null)
			return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("<html><title>");
		sb.append(tmpl.getName() + "(" + npcId + ")");
		sb.append("</title><body>");
		
		for (NpcInstance npcInst : GameObjectsStorage.getAllNpcsForIterate())
		{
			int index = 0;
			if(npcInst.getNpcId() == npcId)
			{
				if (index >= 50)
				{
					sb.append("There are more spawns...");
					break;
				}
				
				if (npcInst.getNpcId() == npcId)
				{
					index++;
					int x = 0;
					int y = 0;
					int z = 0;
					String color = "FFFFFF";
					Spawner npc = npcInst.getSpawn();
					if (npc == null)
					{
						color = "FF0000"; // Red;
					}
					else
					{
						color = "00FF00"; // Green
						x = npc.getLastSpawn().getX();
						y = npc.getLastSpawn().getY();
						z = npc.getLastSpawn().getZ();
					}
					
					sb.append(" <a action=\"bypass -h admin_move_to " + x + " " + y + " " + z + "\"> Tele </a><font color=" + color + "> X=" + x + " Y="+ y + " Z=" + z+ "</font><br1>");
				}
			}
		}
		sb.append("</body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public void findAdminNPCs(Player activeChar, List<Integer> npcIdList, int teleportIndex, int makeVisible)
	{
		int index = 0;

		for(NpcInstance npcInst : GameObjectsStorage.getAllNpcsForIterate())
		{
			int npcId = npcInst.getNpcId();
			if(npcIdList.contains(npcId))
			{
				if(makeVisible == 1)
					npcInst.spawnMe();
				else if(makeVisible == 0)
					npcInst.decayMe();

				if(npcInst.isVisible())
				{
					index++;

					if(teleportIndex > -1)
					{
						if(teleportIndex == index)
							activeChar.teleToLocation(npcInst.getLoc());
					}
					else
						activeChar.sendMessage(index + " - " + npcInst.getName() + " (" + npcInst.getObjectId() + "): " + npcInst.getX() + " " + npcInst.getY() + " " + npcInst.getZ());
				}
			}
		}
	}
}