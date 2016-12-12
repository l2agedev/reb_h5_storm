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

package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.instances.FenceInstance;

/**
 * Format: (ch)ddddddd
 * d: object id
 * d: type (00 - no fence, 01 - only 4 columns, 02 - columns with fences)
 * d: x coord
 * d: y coord
 * d: z coord
 * d: width
 * d: height
 */
public class ExColosseumFenceInfoPacket extends L2GameServerPacket
{
	private FenceInstance _fence;
	
	public ExColosseumFenceInfoPacket(FenceInstance fence)
	{
		_fence = fence;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x03);
		
		writeD(_fence.getObjectId());
		writeD(_fence.getType());
		writeD(_fence.getX());
		writeD(_fence.getY());
		writeD(_fence.getZ());
		writeD(_fence.getWidth());
		writeD(_fence.getLength());
	}
}
