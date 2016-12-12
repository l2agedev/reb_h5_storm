package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2r.gameserver.utils.Strings;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang3.text.StrBuilder;

public class WhoAmI implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "whoami", "whoiam" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if (!Config.ALLOW_WHOAMI_COMMAND)
			return false;
		
		Creature target = null;
		
		double hpRegen = Formulas.calcHpRegen(player);
		double cpRegen = Formulas.calcCpRegen(player);
		double mpRegen = Formulas.calcMpRegen(player);
		double hpDrain = player.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0., target, null);
		double mpDrain = player.calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0., target, null);
		double hpGain = player.calcStat(Stats.HEAL_EFFECTIVNESS, 100., target, null);
		double mpGain = player.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., target, null);
		double critPerc = 2 * player.calcStat(Stats.CRITICAL_DAMAGE, target, null);
		double critStatic = player.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, null);
		double mCritRate = player.calcStat(Stats.MCRITICAL_RATE, target, null);
		double blowRate = player.calcStat(Stats.FATALBLOW_RATE, target, null);

		ItemInstance shld = player.getSecondaryWeaponInstance();
		boolean shield = shld != null && shld.getItemType() == WeaponType.NONE;

		double shieldDef = shield ? player.calcStat(Stats.SHIELD_DEFENCE, player.getTemplate().getBaseShldDef(), target, null) : 0.;
		double shieldRate = shield ? player.calcStat(Stats.SHIELD_RATE, target, null) : 0.;

		double fireResist = player.calcStat(Element.FIRE.getDefence(), 0., target, null);
		double windResist = player.calcStat(Element.WIND.getDefence(), 0., target, null);
		double waterResist = player.calcStat(Element.WATER.getDefence(), 0., target, null);
		double earthResist = player.calcStat(Element.EARTH.getDefence(), 0., target, null);
		double holyResist = player.calcStat(Element.HOLY.getDefence(), 0., target, null);
		double unholyResist = player.calcStat(Element.UNHOLY.getDefence(), 0., target, null);

		double bleedPower = player.calcStat(Stats.BLEED_POWER, target, null);
		double bleedResist = player.calcStat(Stats.BLEED_RESIST, target, null);
		double poisonPower = player.calcStat(Stats.POISON_POWER, target, null);
		double poisonResist = player.calcStat(Stats.POISON_RESIST, target, null);
		double stunPower = player.calcStat(Stats.STUN_POWER, target, null);
		double stunResist = player.calcStat(Stats.STUN_RESIST, target, null);
		double rootPower = player.calcStat(Stats.ROOT_POWER, target, null);
		double rootResist = player.calcStat(Stats.ROOT_RESIST, target, null);
		double sleepPower = player.calcStat(Stats.SLEEP_POWER, target, null);
		double sleepResist = player.calcStat(Stats.SLEEP_RESIST, target, null);
		double paralyzePower = player.calcStat(Stats.PARALYZE_POWER, target, null);
		double paralyzeResist = player.calcStat(Stats.PARALYZE_RESIST, target, null);
		double mentalPower = player.calcStat(Stats.MENTAL_POWER, target, null);
		double mentalResist = player.calcStat(Stats.MENTAL_RESIST, target, null);
		double debuffPower = player.calcStat(Stats.DEBUFF_POWER, target, null);
		double debuffResist = player.calcStat(Stats.DEBUFF_RESIST, target, null);
		double cancelPower = player.calcStat(Stats.CANCEL_POWER, target, null);
		double cancelResist = player.calcStat(Stats.CANCEL_RESIST, target, null);

		double swordResist = 100. - player.calcStat(Stats.SWORD_WPN_VULNERABILITY, target, null);
		double dualResist = 100. - player.calcStat(Stats.DUAL_WPN_VULNERABILITY, target, null);
		double bluntResist = 100. - player.calcStat(Stats.BLUNT_WPN_VULNERABILITY, target, null);
		double daggerResist = 100. - player.calcStat(Stats.DAGGER_WPN_VULNERABILITY, target, null);
		double bowResist = 100. - player.calcStat(Stats.BOW_WPN_VULNERABILITY, target, null);
		double crossbowResist = 100. - player.calcStat(Stats.CROSSBOW_WPN_VULNERABILITY, target, null);
		double poleResist = 100. - player.calcStat(Stats.POLE_WPN_VULNERABILITY, target, null);
		double fistResist = 100. - player.calcStat(Stats.FIST_WPN_VULNERABILITY, target, null);

		double critChanceResist = 100. - player.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, target, null);
		double critDamResistStatic = player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, target, null);
		double critDamResist = 100. - 100 * (player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, 1., target, null) - critDamResistStatic);

		String dialog = HtmCache.getInstance().getNotNull("command/whoami.htm", player);

		NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
		df.setMaximumFractionDigits(1);
		df.setMinimumFractionDigits(1);

		StrBuilder sb = new StrBuilder(dialog);
		sb.replaceFirst("%hpRegen%", df.format(hpRegen));
		sb.replaceFirst("%cpRegen%", df.format(cpRegen));
		sb.replaceFirst("%mpRegen%", df.format(mpRegen));
		sb.replaceFirst("%hpDrain%", df.format(hpDrain));
		sb.replaceFirst("%mpDrain%", df.format(mpDrain));
		sb.replaceFirst("%hpGain%", df.format(hpGain));
		sb.replaceFirst("%mpGain%", df.format(mpGain));
		sb.replaceFirst("%critPerc%", df.format(critPerc));
		sb.replaceFirst("%critStatic%", df.format(critStatic));
		sb.replaceFirst("%mCritRate%", df.format(mCritRate));
		sb.replaceFirst("%blowRate%", df.format(blowRate));
		sb.replaceFirst("%shieldDef%", df.format(shieldDef));
		sb.replaceFirst("%shieldRate%", df.format(shieldRate));
		sb.replaceFirst("%fireResist%", df.format(fireResist));
		sb.replaceFirst("%windResist%", df.format(windResist));
		sb.replaceFirst("%waterResist%", df.format(waterResist));
		sb.replaceFirst("%earthResist%", df.format(earthResist));
		sb.replaceFirst("%holyResist%", df.format(holyResist));
		sb.replaceFirst("%darkResist%", df.format(unholyResist));
		sb.replaceFirst("%bleedPower%", df.format(bleedPower));
		sb.replaceFirst("%bleedResist%", df.format(bleedResist));
		sb.replaceFirst("%poisonPower%", df.format(poisonPower));
		sb.replaceFirst("%poisonResist%", df.format(poisonResist));
		sb.replaceFirst("%stunPower%", df.format(stunPower));
		sb.replaceFirst("%stunResist%", df.format(stunResist));
		sb.replaceFirst("%rootPower%", df.format(rootPower));
		sb.replaceFirst("%rootResist%", df.format(rootResist));
		sb.replaceFirst("%sleepPower%", df.format(sleepPower));
		sb.replaceFirst("%sleepResist%", df.format(sleepResist));
		sb.replaceFirst("%paralyzePower%", df.format(paralyzePower));
		sb.replaceFirst("%paralyzeResist%", df.format(paralyzeResist));
		sb.replaceFirst("%mentalPower%", df.format(mentalPower));
		sb.replaceFirst("%mentalResist%", df.format(mentalResist));
		sb.replaceFirst("%debuffPower%", df.format(debuffPower));
		sb.replaceFirst("%debuffResist%", df.format(debuffResist));
		sb.replaceFirst("%cancelPower%", df.format(cancelPower));
		sb.replaceFirst("%cancelResist%", df.format(cancelResist));
		sb.replaceFirst("%swordResist%", df.format(swordResist));
		sb.replaceFirst("%dualResist%", df.format(dualResist));
		sb.replaceFirst("%bluntResist%", df.format(bluntResist));
		sb.replaceFirst("%daggerResist%", df.format(daggerResist));
		sb.replaceFirst("%bowResist%", df.format(bowResist));
		sb.replaceFirst("%crossbowResist%", df.format(crossbowResist));
		sb.replaceFirst("%fistResist%", df.format(fistResist));
		sb.replaceFirst("%poleResist%", df.format(poleResist));
		sb.replaceFirst("%critChanceResist%", df.format(critChanceResist));
		sb.replaceFirst("%critDamResist%", df.format(critDamResist));

		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(Strings.bbParse(sb.toString()));
		player.sendPacket(msg);

		return true;
	}
}
