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
package l2r.gameserver.network.consolecon.receive;

import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.consolecon.Console;
import l2r.gameserver.network.consolecon.ReceivableConsolePacket;
import l2r.gameserver.network.consolecon.send.AnswereRequestPacketForge;

import org.inc.incolution.util.IncByteReader;
import org.inc.incolution.util.IncByteWriter;
import org.inc.incolution.util.list.IncArrayList;

/**
 * @author Forsaiken
 */
public final class RequestPacketForge extends ReceivableConsolePacket
{
	private int _requestId;
	private String _playerName;
	@SuppressWarnings("unused")
	private boolean _broadcast;
	private byte[] _packetData;
	
	private Player _player;
	
	@Override
	protected final void readImpl()
	{
		_requestId = super.readD();
		_playerName = super.readS();
		_broadcast = super.readC() == 1;
		
		final int size = super.readH();
		_packetData = new byte[size];
		super.readB(_packetData);
	}
	
	@Override
	protected final void runImpl()
	{
		final Console console = super.getClient();
		
		_player = World.getPlayer(_playerName);
		if (_player == null)
		{
			console.sendPacket(new AnswereRequestPacketForge(_requestId, AnswereRequestPacketForge.RESPONSE_FAILED, null));
			return;
		}
		
		final IncByteReader ibr = new IncByteReader(_packetData);
		final IncArrayList<PacketForgeElement> stack = new IncArrayList<>();
		
		while (ibr.remaining() > 0)
		{
			switch (ibr.readC())
			{
				case 0x00:
					stack.add(new PacketForgeElementC(ibr));
					break;
					
				case 0x01:
					stack.add(new PacketForgeElementH(ibr));
					break;
					
				case 0x02:
					stack.add(new PacketForgeElementD(ibr));
					break;
					
				case 0x03:
					stack.add(new PacketForgeElementQ(ibr));
					break;
					
				case 0x04:
					stack.add(new PacketForgeElementF(ibr));
					break;
					
				case 0x05:
					stack.add(new PacketForgeElementS(ibr));
					break;
					
				case 0x06:
					stack.add(new PacketForgeElementB(ibr));
					break;
			}
		}
		
		//System.out.println("Preparing Packet Forge");
		final IncByteWriter ibw = new IncByteWriter();
		for (final PacketForgeElement element : stack)
		{
			//System.out.println("Putting: " + element.getClass().getSimpleName() + ", value: " + element.getStringValue());
			element.writeToByteBuffer(ibw);
		}
		
		/*final byte[] data = ibw.toArray();
		//System.out.println("Sending packet forge: " + data.length + " bytes");
		final ConsolePacketForge packet = new ConsolePacketForge(data); TODO FIX
		final String[] playerNames;
		if (_broadcast)
		{
			final IncArrayList<Player> players = new IncArrayList<>();
			players.addAll(World.getAroundPlayers(_player));
			playerNames = new String[players.size() + 1];
			for (int i = players.size(); i-- > 0;)
			{
				final Player player = players.getUnsafe(i);
				player.sendPacket(packet);
				playerNames[i] = player.getName();
			}
			_player.broadcastPacket(packet);
			playerNames[players.size()] = _player.getName();
		}
		else
		{
			_player.sendPacket(packet);
			playerNames = new String[]{_player.getName()};
		}
		
		console.sendPacket(new AnswereRequestPacketForge(_requestId, AnswereRequestPacketForge.RESPONSE_SUCCESS, playerNames));*/
	}
	
	protected final Player getPlayer()
	{
		return _player;
	}
	
	private abstract class PacketForgeElement
	{
		protected final String _svalue;
		
		protected PacketForgeElement(final IncByteReader ibr)
		{
			_svalue = ibr.readS();
		}
		
		protected final boolean isSpecialField()
		{
			return _svalue.startsWith("%");
		}
		
		@SuppressWarnings("unused")
		public final String getStringValue()
		{
			return _svalue;
		}
		
		protected final long getValueForSpecialField()
		{
			if (_svalue.equals("%player_objid%"))
			{
				return getPlayer().getObjectId();
			}
			else if (_svalue.equals("%target_objid%"))
			{
				final GameObject target = getPlayer().getTarget();
				if (target != null)
					return target.getObjectId();
				return 0;
			}
			else if (_svalue.equals("%player_clanid%"))
			{
				final Clan clan = getPlayer().getClan();
				if (clan != null)
					return clan.getClanId();
				return 0;
			}
			else if (_svalue.equals("%target_clanid%"))
			{
				final GameObject target = getPlayer().getTarget();
				if (target != null && target.isPlayer())
				{
					final Clan clan = ((Player) target).getClan();
					if (clan != null)
						return clan.getClanId();
				}
				return 0;
			}
			return 0;
		}
		
		protected abstract void writeToByteBuffer(final IncByteWriter ibw);
	}
	
	private final class PacketForgeElementC extends PacketForgeElement
	{
		public PacketForgeElementC(final IncByteReader ibr)
		{
			super(ibr);
		}
		
		@Override
		protected final void writeToByteBuffer(final IncByteWriter ibw)
		{
			if (super.isSpecialField())
				ibw.writeC((int)super.getValueForSpecialField());
			else
				ibw.writeC(Integer.parseInt(_svalue));
		}
	}
	
	private final class PacketForgeElementH extends PacketForgeElement
	{
		public PacketForgeElementH(final IncByteReader ibr)
		{
			super(ibr);
		}
		
		@Override
		protected final void writeToByteBuffer(final IncByteWriter ibw)
		{
			if (super.isSpecialField())
				ibw.writeH((int)super.getValueForSpecialField());
			else
				ibw.writeH(Integer.parseInt(_svalue));
		}
	}
	
	private final class PacketForgeElementD extends PacketForgeElement
	{
		public PacketForgeElementD(final IncByteReader ibr)
		{
			super(ibr);
		}
		
		@Override
		protected final void writeToByteBuffer(final IncByteWriter ibw)
		{
			if (super.isSpecialField())
				ibw.writeD((int)super.getValueForSpecialField());
			else
				ibw.writeD(Integer.parseInt(_svalue));
		}
	}
	
	private final class PacketForgeElementQ extends PacketForgeElement
	{
		public PacketForgeElementQ(final IncByteReader ibr)
		{
			super(ibr);
		}
		
		@Override
		protected final void writeToByteBuffer(final IncByteWriter ibw)
		{
			if (super.isSpecialField())
				ibw.writeQ(super.getValueForSpecialField());
			else
				ibw.writeQ(Long.parseLong(_svalue));
		}
	}
	
	private final class PacketForgeElementF extends PacketForgeElement
	{
		public PacketForgeElementF(final IncByteReader ibr)
		{
			super(ibr);
		}
		
		@Override
		protected final void writeToByteBuffer(final IncByteWriter ibw)
		{
			if (super.isSpecialField())
				ibw.writeF(Double.longBitsToDouble(super.getValueForSpecialField()));
			else
				ibw.writeF(Double.parseDouble(_svalue));
		}
	}
	
	private final class PacketForgeElementS extends PacketForgeElement
	{
		public PacketForgeElementS(final IncByteReader ibr)
		{
			super(ibr);
		}
		
		@Override
		protected final void writeToByteBuffer(final IncByteWriter ibw)
		{
			ibw.writeS(_svalue);
		}
	}
	
	private final class PacketForgeElementB extends PacketForgeElement
	{
		public PacketForgeElementB(final IncByteReader ibr)
		{
			super(ibr);
		}
		
		@Override
		protected final void writeToByteBuffer(final IncByteWriter ibw)
		{
			ibw.writeB(parse());
		}
		
		private final byte[] parse()
		{
			final String[] sbytes = _svalue.split(";");
			final byte[] bytes = new byte[sbytes.length];
			for (int i = sbytes.length; i-- > 0;)
			{
				bytes[i] = Byte.parseByte(sbytes[i]);
			}
			return bytes;
		}
	}
}