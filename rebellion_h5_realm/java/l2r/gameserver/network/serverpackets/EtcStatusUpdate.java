package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private int IncreasedForce, WeightPenalty, MessageRefusal, DangerArea;
	private int armorExpertisePenalty, weaponExpertisePenalty, CharmOfCourage, DeathPenaltyLevel, ConsumedSouls;

	public EtcStatusUpdate(Player player)
	{
		IncreasedForce = player.getIncreasedForce();
		WeightPenalty = player.getWeightPenalty();
		MessageRefusal = player.getMessageRefusal() || player.getNoChannel() != 0 || player.isBlockAll() ? 1 : 0;
		DangerArea = player.isInDangerArea() ? 1 : 0;
		armorExpertisePenalty = player.getArmorsExpertisePenalty();
		weaponExpertisePenalty = player.getWeaponsExpertisePenalty();
		CharmOfCourage = player.isCharmOfCourage() ? 1 : 0;
		DeathPenaltyLevel = player.getDeathPenalty() == null ? 0 : player.getDeathPenalty().getLevel();
		ConsumedSouls = player.getConsumedSouls();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xf9); //Packet type
		writeD(IncreasedForce); // skill id 4271, 7 lvl
		writeD(WeightPenalty); // skill id 4270, 4 lvl
		writeD(MessageRefusal); //skill id 4269, 1 lvl
		writeD(DangerArea); // skill id 4268, 1 lvl
		writeD(weaponExpertisePenalty); // weapon grade penalty, skill 6209 in epilogue
		writeD(armorExpertisePenalty); // armor grade penalty, skill 6213 in epilogue
		writeD(CharmOfCourage); //Charm of Courage, "Prevents experience value decreasing if killed during a siege war".
		writeD(DeathPenaltyLevel); //Death Penalty max lvl 15, "Combat ability is decreased due to death."
		writeD(ConsumedSouls);
	}
}