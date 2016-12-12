package l2r.gameserver.network.serverpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.VisualInstance;
import l2r.gameserver.skills.AbnormalEffectType;


/**
 * 
 * @author Infern0
 * @Idea Midnex
 *
 */
public class ModelCharInfo extends L2GameServerPacket
{
	private VisualInstance _model;
	private Player _pl;

	public ModelCharInfo(VisualInstance m, Player target)
	{
		_pl = target;
		_model = m;
	}

	@Override
	protected final void writeImpl()
	{
		if(_pl != null)
		{
			writeC(0x31);
			writeD(_model.getX());
			writeD(_model.getY());
			writeD(_model.getZ() + Config.CLIENT_Z_SHIFT);
			writeD(0x00);
			writeD(_model.getObjectId());
			writeS(_model.getName()); // visible name
			writeD(_pl.getRace().ordinal());
			writeD(_pl.getSex());
			writeD(_pl.getBaseClassId());

			writeD(0x00);
			writeD(0x00);
			writeD(_model._weapons);
			writeD(_model._shields);
			writeD(_model._gloves);
			writeD(_model._chest);
			writeD(_model._legs);
			writeD(_model._boots);
			writeD(_model._cloaks);
			writeD(0x00);
			writeD(_model._accessory);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00); // belt

			writeD(0x00);
			writeD(0x00);
			writeD(_model._weapons);
			writeD(_model._shields);
			writeD(_model._gloves);
			writeD(_model._chest);
			writeD(_model._legs);
			writeD(_model._boots);
			writeD(_model._cloaks);
			writeD(0x00);
			writeD(_model._accessory);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00); // belt aug

			writeD(0x00);
			writeD(0x01);

			writeD(0x00);
			writeD(0x00);

			writeD(_model.getMAtkSpd());
			writeD(_model.getPAtkSpd());

			writeD(0x00);

			writeD(_model.getRunSpeed());
			writeD(_model.getWalkSpeed());
			writeD(_model.getSwimSpeed()); // swim run speed
			writeD(_model.getSwimSpeed()); // swim walk speed
			writeD(_model.getWalkSpeed()); // fly run speed
			writeD(_model.getWalkSpeed()); // fly walk speed
			writeD(_model.getRunSpeed());
			writeD(_model.getWalkSpeed());
			writeF(_model.getMovementSpeedMultiplier()); // _activeChar.getProperMultiplier()
			writeF(_model.getAttackSpeedMultiplier()); // _activeChar.getAttackSpeedMultiplier()

			writeF(_pl.getColRadius());
			writeF(_pl.getColHeight());

			writeD(_model._hairStyle >= 0 ? _model._hairStyle : _pl.getHairStyle());
			writeD(_model._hairColor >= 0 ? _model._hairColor : _pl.getHairColor());
			writeD(_model._face > 0 ? _model._face : _pl.getFace());

			writeS(_model.getTitle());

			writeD(0x00); // clan id
			writeD(0x00); // clan crest id
			writeD(0x00); // ally id
			writeD(0x00); // ally crest id

			writeC(0x01);
			writeC(_model.isRunning() ? 1 : 0); // running = 1   walking = 0
			writeC(_model.isInCombat() ? 1 : 0);
			writeC(_model.isAlikeDead() ? 1 : 0);

			writeC(0x00); // invisible = 1  visible =0

			writeC(0x00); // 1 on strider   2 on wyvern  3 on Great Wolf  0 no mount
			writeC(0x00); //  1 - sellshop
			writeH(0x00); // cubic count
			writeC(0x00); // find party members
			writeD(_model.getAbnormalEffect(AbnormalEffectType.FIRST)); // abnormal effect
			writeC(0x00); // isFlying() ? 2 : 0
			writeH(0x00); //getRecomHave(): Blue value for name (0 = white, 255 = pure blue)
			writeD(1000000); // getMountNpcId() + 1000000

			writeD(_pl.getClassId().getId());
			writeD(0x00);
			writeC(0x00);

			writeC(_model.getTeam().ordinal()); //team circle around feet 1= Blue, 2 = red

			writeD(0x00); // getClanCrestLargeId()
			writeC(0x00); // isNoble(): Symbol on char menu ctrl+I
			writeC(0x00); // Hero Aura
			writeC(0x00); //0x01: Fishing Mode (Cant be undone by setting back to 0)
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(_model._ncolor);
			writeD(_model.getLoc().h);
			writeD(0x00); // pledge class
			writeD(0x00); // pledge type
			writeD(_model._tcolor);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x01); // T2
			writeD(0x00);
		}
	}
}