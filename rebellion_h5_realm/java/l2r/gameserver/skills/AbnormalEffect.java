package l2r.gameserver.skills;

import java.util.NoSuchElementException;

public enum AbnormalEffect
{
	// ave_none
	NULL("null", 0x0, AbnormalEffectType.FIRST),
	// ave_dot_bleeding
	BLEEDING("bleeding", 0x00000001, AbnormalEffectType.FIRST),
	// ave_dot_poison
	POISON("poison", 0x00000002, AbnormalEffectType.FIRST),
	REDCIRCLE("redcircle", 0x00000004, AbnormalEffectType.FIRST),
	ICE("ice", 0x00000008, AbnormalEffectType.FIRST),

	AFFRAID("affraid", 0x00000010, AbnormalEffectType.FIRST),
	CONFUSED("confused", 0x00000020, AbnormalEffectType.FIRST),
	// ave_stun
	STUN("stun", 0x00000040, AbnormalEffectType.FIRST),
	// ave_sleep
	SLEEP("sleep", 0x00000080, AbnormalEffectType.FIRST),

	MUTED("muted", 0x00000100, AbnormalEffectType.FIRST),
	// ave_root
	ROOT("root", 0x00000200, AbnormalEffectType.FIRST),
	HOLD_1("hold1", 0x00000400, AbnormalEffectType.FIRST),
	// ave_flesh_stone
	HOLD_2("hold2", 0x00000800, AbnormalEffectType.FIRST), // эффект от Dance of Medusa

	UNKNOWN_13("unk13", 0x00001000, AbnormalEffectType.FIRST),
	// ave_big_head
	BIG_HEAD("bighead", 0x00002000, AbnormalEffectType.FIRST),
	FLAME("flame", 0x00004000, AbnormalEffectType.FIRST),
	UNKNOWN_16("unk16", 0x00008000, AbnormalEffectType.FIRST), // труп с таким абнормалом становится белым

	GROW("grow", 0x00010000, AbnormalEffectType.FIRST),
	FLOATING_ROOT("floatroot", 0x00020000, AbnormalEffectType.FIRST),
	DANCE_STUNNED("dancestun", 0x00040000, AbnormalEffectType.FIRST), // танцует со звездочками над головой
	FIREROOT_STUN("firerootstun", 0x00080000, AbnormalEffectType.FIRST), // красная аура у ног со звездочками над головой

	STEALTH("shadow", 0x00100000, AbnormalEffectType.FIRST),
	IMPRISIONING_1("imprison1", 0x00200000, AbnormalEffectType.FIRST), // синяя аура на уровне пояса
	IMPRISIONING_2("imprison2", 0x00400000, AbnormalEffectType.FIRST), // синяя аура на уровне пояса
	MAGIC_CIRCLE("magiccircle", 0x00800000, AbnormalEffectType.FIRST), // большой синий круг вокруг чара

	ICE2("ice2", 0x01000000, AbnormalEffectType.FIRST), // небольшая ледяная аура, скорее всего DOT
	EARTHQUAKE("earthquake", 0x02000000, AbnormalEffectType.FIRST), // землетрясение
	UNKNOWN_27("unk27", 0x04000000, AbnormalEffectType.FIRST),
	// ave_ultimate_defence
	INVULNERABLE("invul1", 0x08000000, AbnormalEffectType.FIRST), // Ultimate Defence
	// ave_vp_up
	VITALITY("vitality", 0x10000000, AbnormalEffectType.FIRST), // Vitality херб, красное пламя
	// ave_real_target
	REAL_TARGET("realtarget", 0x20000000, AbnormalEffectType.FIRST), // дебафф Real Target (знак над головой)
	// ave_death_mark
	DEATH_MARK("deathmark", 0x40000000, AbnormalEffectType.FIRST), // голубая морда над головой
	// ave_turn_flee
	SOUL_SHOCK("soulshock", 0x80000000, AbnormalEffectType.FIRST), // голубой череп над головой

	// special effects
	// ave_invincibility
	S_INVULNERABLE("invul2", 0x00000001, AbnormalEffectType.SECOND), // целестиал
	S_AIR_STUN("redglow", 0x00000002, AbnormalEffectType.SECOND), // непонятное красное облако
	S_AIR_ROOT("redglow2", 0x00000004, AbnormalEffectType.SECOND), // непонятное красное облако
	S_BAGUETTE_SWORD("baguettesword", 0x00000008, AbnormalEffectType.SECOND), // пусто

	S_YELLOW_AFFRO("yellowafro", 0x00000010, AbnormalEffectType.SECOND), // Большая круглая желтая прическа с воткнутой в волосы расческой
	S_PINK_AFFRO("pinkafro", 0x00000020, AbnormalEffectType.SECOND), // Большая круглая розовая прическа с воткнутой в волосы расческой
	S_BLACK_AFFRO("blackafro", 0x00000040, AbnormalEffectType.SECOND), // Большая круглая черная прическа с воткнутой в волосы расческой
	// ave_vp_keep
	AVE_VP_KEEP("ave_vp_keep", 0x00000080, AbnormalEffectType.SECOND),
	// ave_stigma_of_silen
	S_STIGMA("stigma", 0x00000100, AbnormalEffectType.SECOND), // Stigma of Shillen
	S_UNKNOWN10("sunk10", 0x00000200, AbnormalEffectType.SECOND), // какой то рут
	// ave_frozen_pillar
	FROZEN_PILLAR("frozenpillar", 0x00000400, AbnormalEffectType.SECOND), // Frozen Pillar (Freya)
	S_UNKNOWN12("sunk12", 0x00000800, AbnormalEffectType.SECOND), // пусто

	S_DESTINO_SET("vesper_red", 0x00001000, AbnormalEffectType.SECOND), // Фейковый сет Веспера
	S_VESPER_SET("vesper_noble", 0x00002000, AbnormalEffectType.SECOND), // фейковый сет Веспера Белый
	S_SOA_RESP("soa_respawn", 0x00004000, AbnormalEffectType.SECOND), // Мобы на респе СОА появляются с таким абнормалом
	// ave_mp_shield
	S_ARCANE_SHIELD("ave_mp_shield", 0x00008000, AbnormalEffectType.SECOND), // Щит Арканы

	S_UNKNOWN17("sunk17", 0x00010000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN18("sunk18", 0x00020000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN19("sunk19", 0x00040000, AbnormalEffectType.SECOND), // пусто
	S_NAVIT("nevitSystem", 0x00080000, AbnormalEffectType.SECOND), //

	S_UNKNOWN21("sunk21", 0x00100000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN22("sunk22", 0x00200000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN23("sunk23", 0x00400000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN24("sunk24", 0x00800000, AbnormalEffectType.SECOND), // пусто

	S_UNKNOWN25("sunk25", 0x01000000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN26("sunk26", 0x02000000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN27("sunk27", 0x04000000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN28("sunk28", 0x08000000, AbnormalEffectType.SECOND), // пусто

	S_UNKNOWN29("sunk29", 0x10000000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN30("sunk30", 0x20000000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN31("sunk31", 0x40000000, AbnormalEffectType.SECOND), // пусто
	S_UNKNOWN32("sunk32", 0x80000000, AbnormalEffectType.SECOND), // пусто

	// event effects
	E_AFRO_1("afrobaguette1", 0x000001, AbnormalEffectType.BRANCH),
	E_AFRO_2("afrobaguette2", 0x000002, AbnormalEffectType.BRANCH),
	E_AFRO_3("afrobaguette3", 0x000004, AbnormalEffectType.BRANCH),
	E_EVASWRATH("evaswrath", 0x000008, AbnormalEffectType.BRANCH),
	E_HEADPHONE("headphone", 0x000010, AbnormalEffectType.BRANCH),
	// ave_change_ves_s
	E_VESPER_1("vesper1", 0x000020, AbnormalEffectType.BRANCH),
	// ave_change_ves_c
	E_VESPER_2("vesper2", 0x000040, AbnormalEffectType.BRANCH),
	// ave_change_ves_d
	E_VESPER_3("vesper3", 0x000080, AbnormalEffectType.BRANCH);

	private final int _mask;
	private final String _name;
	private final AbnormalEffectType _type;

	private AbnormalEffect(String name, int mask, AbnormalEffectType t)
	{
		_name = name;
		_mask = mask;
		_type = t;
	}
	public final int getMask()
	{
		return _mask;
	}

	public final String getName()
	{
		return _name;
	}

	public AbnormalEffectType getType()
	{
		return _type;
	}

	public static AbnormalEffect getByName(String name)
	{
		for(AbnormalEffect eff : AbnormalEffect.values())
			if(eff.getName().equals(name))
				return eff;

		throw new NoSuchElementException("AbnormalEffect not found for name: '" + name + "'.\n Please check " + AbnormalEffect.class.getCanonicalName());
	}
}