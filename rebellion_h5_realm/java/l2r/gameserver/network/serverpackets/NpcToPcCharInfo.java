package l2r.gameserver.network.serverpackets;

import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.CharTemplateHolder;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.tables.FakePcsTable;
import l2r.gameserver.tables.FakePcsTable.FakePc;
import l2r.gameserver.templates.PlayerTemplate;

public class NpcToPcCharInfo extends L2GameServerPacket
{
	private NpcInstance _cha;
	
	FakePc _fpc = null;
	
	public NpcToPcCharInfo(NpcInstance cha)
	{
		_cha = cha;
		_fpc = FakePcsTable.getInstance().getFakePc(cha.getNpcId());
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_fpc != null)
		{
			writeC(0x31);
			writeD(_cha.getX());
			writeD(_cha.getY());
			writeD(_cha.getZ() + Config.CLIENT_Z_SHIFT);
			writeD(0x00);
			writeD(_cha.getObjectId());
			writeS(_fpc.name); // visible name
			writeD(_fpc.race);
			writeD(_fpc.sex);
			writeD(_fpc.clazz);
			
			writeD(_fpc.pdUnder);
			writeD(_fpc.pdHead);
			writeD(_fpc.pdRHand);
			writeD(_fpc.pdLHand);
			writeD(_fpc.pdGloves);
			writeD(_fpc.pdChest);
			writeD(_fpc.pdLegs);
			writeD(_fpc.pdFeet);
			writeD(_fpc.pdBack);
			writeD(_fpc.pdLRHand);
			writeD(_fpc.pdHair);
			writeD(_fpc.pdHair2);
			writeD(_fpc.pdRBracelet);
			writeD(_fpc.pdLBracelet);
			writeD(_fpc.pdDeco1);
			writeD(_fpc.pdDeco2);
			writeD(_fpc.pdDeco3);
			writeD(_fpc.pdDeco4);
			writeD(_fpc.pdDeco5);
			writeD(_fpc.pdDeco6);
			writeD(0x00); // belt
			
			writeD(_fpc.pdUnderAug);
			writeD(_fpc.pdHeadAug);
			writeD(_fpc.pdRHandAug);
			writeD(_fpc.pdLHandAug);
			writeD(_fpc.pdGlovesAug);
			writeD(_fpc.pdChestAug);
			writeD(_fpc.pdLegsAug);
			writeD(_fpc.pdFeetAug);
			writeD(_fpc.pdBackAug);
			writeD(_fpc.pdLRHandAug);
			writeD(_fpc.pdHairAug);
			writeD(_fpc.pdHair2Aug);
			writeD(_fpc.pdRBraceletAug);
			writeD(_fpc.pdLBraceletAug);
			writeD(_fpc.pdDeco1Aug);
			writeD(_fpc.pdDeco2Aug);
			writeD(_fpc.pdDeco3Aug);
			writeD(_fpc.pdDeco4Aug);
			writeD(_fpc.pdDeco5Aug);
			writeD(_fpc.pdDeco6Aug);
			writeD(0x00); // belt aug
			
			writeD(0x00);
			writeD(0x01);
			
			writeD(_fpc.pvpFlag);
			writeD(_fpc.karma);
			
			writeD(_cha.getMAtkSpd());
			writeD(_cha.getPAtkSpd());
			
			writeD(0x00);
			
			writeD(_cha.getRunSpeed());
			writeD(_cha.getWalkSpeed());
			writeD(_cha.getSwimSpeed()); // swim run speed
			writeD(_cha.getSwimSpeed()); // swim walk speed
			writeD(_cha.getWalkSpeed()); // fly run speed
			writeD(_cha.getWalkSpeed()); // fly walk speed
			writeD(_cha.getRunSpeed());
			writeD(_cha.getWalkSpeed());
			writeF(_cha.getMovementSpeedMultiplier()); // _activeChar.getProperMultiplier()
			writeF(_cha.getAttackSpeedMultiplier()); // _activeChar.getAttackSpeedMultiplier()
			
			PlayerTemplate tmp = CharTemplateHolder.getInstance().getTemplate(_fpc.clazz, _fpc.sex != 0);
			writeF(tmp.getCollisionRadius());
			writeF(tmp.getCollisionHeight());
			
			writeD(_fpc.hairStyle);
			writeD(_fpc.hairColor);
			writeD(_fpc.face);
			
			writeS(_fpc.title);
			
			writeD(0x00); // clan id
			writeD(0x00); // clan crest id
			writeD(0x00); // ally id
			writeD(0x00); // ally crest id
			
			writeC(0x01);
			writeC(_cha.isRunning() ? 1 : 0); // running = 1 walking = 0
			writeC(_cha.isInCombat() ? 1 : 0);
			writeC(_cha.isAlikeDead() ? 1 : 0);
			
			writeC(0x00); // invisible = 1 visible =0
			
			writeC(_fpc.mount); // 1 on strider 2 on wyvern 3 on Great Wolf 0 no mount
			writeC(0x00); // 1 - sellshop
			writeH(0x00); // cubic count
			writeC(0x00); // find party members
			writeD(0x00); // abnormal effect
			writeC(0x00); // isFlying() ? 2 : 0
			writeH(0x00); // getRecomHave(): Blue value for name (0 = white, 255 = pure blue)
			writeD(1000000); // getMountNpcId() + 1000000
			
			writeD(_fpc.clazz);
			writeD(0x00);
			writeC(_fpc.enchantEffect);
			
			writeC(_fpc.team); // team circle around feet 1= Blue, 2 = red
			
			writeD(0x00); // getClanCrestLargeId()
			writeC(0x00); // isNoble(): Symbol on char menu ctrl+I
			writeC(_fpc.hero); // Hero Aura
			writeC(_fpc.fishing); // 0x01: Fishing Mode (Cant be undone by setting back to 0)
			writeD(_fpc.fishingX);
			writeD(_fpc.fishingY);
			writeD(_fpc.fishingZ);
			writeD(_fpc.nameColor);
			writeD(_cha.getLoc().h);
			writeD(0x00); // pledge class
			writeD(0x00); // pledge type
			writeD(_fpc.titleColor);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x01); // T2
			writeD(0x00);
		}
	}
}