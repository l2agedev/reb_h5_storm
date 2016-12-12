package l2r.gameserver.model;

import l2r.gameserver.Config;
import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.model.base.AcquireType;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.ClassType2;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;

import java.util.Collection;

public class SubClass
{
	public static final String PATH = "villagemaster/certification/";
	
	public static final int CERTIFICATION_65 = 1 << 0;
	public static final int CERTIFICATION_70 = 1 << 1;
	public static final int CERTIFICATION_75 = 1 << 2;
	public static final int CERTIFICATION_80 = 1 << 3;

	private int _class = 0;
	private long _exp = Experience.LEVEL[Config.ALT_GAME_START_LEVEL_TO_SUBCLASS], minExp = Experience.LEVEL[Config.ALT_GAME_START_LEVEL_TO_SUBCLASS], maxExp = Experience.LEVEL[Experience.LEVEL.length - 1];
	private int _sp = 0;
	private int _level = Config.ALT_GAME_START_LEVEL_TO_SUBCLASS, _certification;
	private double _Hp = 1, _Mp = 1, _Cp = 1;
	private boolean _active = false, _isBase = false;
	private DeathPenalty _dp;

	public SubClass()
	{}

	public int getClassId()
	{
		return _class;
	}

	public long getExp()
	{
		return _exp;
	}

	public long getMaxExp()
	{
		return maxExp;
	}

	public void addExp(long val)
	{
		setExp(_exp + val);
	}

	public long getSp()
	{
		return Math.min(_sp, Integer.MAX_VALUE);
	}

	public void addSp(long val)
	{
		setSp(_sp + val);
	}

	public int getLevel()
	{
		return _level;
	}

	public void setClassId(int classId)
	{
		_class = classId;
	}

	public void setExp(long val)
	{
		val = Math.max(val, minExp);
		val = Math.min(val, maxExp);

		_exp = val;
		_level = Experience.getLevel(_exp);
	}

	public void setSp(long spValue)
	{
		spValue = Math.max(spValue, 0);
		spValue = Math.min(spValue, Integer.MAX_VALUE);

		_sp = (int) spValue;
	}

	public void setHp(double hpValue)
	{
		_Hp = hpValue;
	}

	public double getHp()
	{
		return _Hp;
	}

	public void setMp(final double mpValue)
	{
		_Mp = mpValue;
	}

	public double getMp()
	{
		return _Mp;
	}

	public void setCp(final double cpValue)
	{
		_Cp = cpValue;
	}

	public double getCp()
	{
		return _Cp;
	}

	public void setActive(final boolean active)
	{
		_active = active;
	}

	public boolean isActive()
	{
		return _active;
	}

	public void setBase(final boolean base)
	{
		_isBase = base;
		minExp = Experience.LEVEL[_isBase ? 1 : Config.ALT_GAME_START_LEVEL_TO_SUBCLASS];
		maxExp = Experience.LEVEL[(_isBase ? Experience.getMaxLevel() : Experience.getMaxSubLevel()) + 1] - 1;
	}

	public boolean isBase()
	{
		return _isBase;
	}

	public DeathPenalty getDeathPenalty(Player player)
	{
		if(_dp == null)
			_dp = new DeathPenalty(player, 0);
		return _dp;
	}

	public void setDeathPenalty(DeathPenalty dp)
	{
		_dp = dp;
	}

	public int getCertification()
	{
		return _certification;
	}

	public void setCertification(int certification)
	{
		_certification = certification;
	}

	public void addCertification(int c)
	{
		_certification |= c;
	}

	public boolean isCertificationGet(int v)
	{
		return (_certification & v) == v;
	}
	
	public static void showCertificationList(NpcInstance npc, Player player)
	{
		if (!checkConditions(65, npc, player, true))
		{
			return;
		}

		Functions.show(PATH + "certificatelist.htm", player, npc);
	}

	public static void getCertification65(NpcInstance npc, Player player)
	{
		if (!checkConditions(65, npc, player, Config.ALT_GAME_SUB_BOOK))
			return;

		SubClass clzz = player.getActiveClass();
		if (clzz.isCertificationGet(SubClass.CERTIFICATION_65))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		Functions.addItem(player, 10280, 1);
		clzz.addCertification(SubClass.CERTIFICATION_65);
		player.store(true);
	}

	public static void getCertification70(NpcInstance npc, Player player)
	{
		if (!checkConditions(70, npc, player, Config.ALT_GAME_SUB_BOOK))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		Functions.addItem(player, 10280, 1);
		clzz.addCertification(SubClass.CERTIFICATION_70);
		player.store(true);
	}

	public static void getCertification75List(NpcInstance npc, Player player)
	{
		if (!checkConditions(75, npc, player, Config.ALT_GAME_SUB_BOOK))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		Functions.show(PATH + "certificate-choose.htm", player, npc);
	}

	public static void getCertification75(NpcInstance npc, Player player, boolean classCertifi)
	{
		if (!checkConditions(75, npc, player, Config.ALT_GAME_SUB_BOOK))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		if (classCertifi)
		{
			ClassId cl = ClassId.VALUES[clzz.getClassId()];
			if(cl.getType2() == null)
				return;


			Functions.addItem(player, cl.getType2().getCertificateId(), 1);
		}
		else
		{
			Functions.addItem(player, 10612, 1); // master ability
		}

		clzz.addCertification(SubClass.CERTIFICATION_75);
		player.store(true);
	}

	public static void getCertification80(NpcInstance npc, Player player)
	{
		if (!checkConditions(80, npc, player, Config.ALT_GAME_SUB_BOOK))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят(ы) преведущий сертификат(ы)
		if (!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70) || !clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if (clzz.isCertificationGet(SubClass.CERTIFICATION_80))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		ClassId cl = ClassId.VALUES[clzz.getClassId()];
		if(cl.getType2() == null)
			return;

		Functions.addItem(player, cl.getType2().getTransformationId(), 1);
		clzz.addCertification(SubClass.CERTIFICATION_80);
		player.store(true);
	}

	public static void cancelCertification(NpcInstance npc, Player player)
	{
		if(player.getInventory().getAdena() < 10000000)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(!player.getActiveClass().isBase())
			return;

		player.getInventory().reduceAdena(10000000);

		for (ClassType2 classType2 : ClassType2.VALUES)
		{
			player.getInventory().destroyItemByItemId(classType2.getCertificateId(), player.getInventory().getCountOf(classType2.getCertificateId()));
			player.getInventory().destroyItemByItemId(classType2.getTransformationId(), player.getInventory().getCountOf(classType2.getTransformationId()));
		}

		Collection<SkillLearn> skillLearnList = SkillAcquireHolder.getInstance().getAvailableSkills(null, AcquireType.CERTIFICATION);
		for(SkillLearn learn : skillLearnList)
		{
			Skill skill = player.getKnownSkill(learn.getId());
			if(skill != null)
				player.removeSkill(skill, true);
		}

		for(SubClass subClass : player.getSubClasses().values())
		{
			if(!subClass.isBase())
				subClass.setCertification(0);
		}

		player.sendPacket(new SkillList(player));
		Functions.show(new CustomMessage("scripts.services.SubclassSkills.SkillsDeleted", player), player);
	}

	public static boolean checkConditions(int level, NpcInstance npc, Player player, boolean first)
	{
		if (player.getLevel() < level)
		{
			Functions.show(PATH + "certificate-nolevel.htm", player, npc, "%level%", level);
			return false;
		}

		if (player.getActiveClass().isBase())
		{
			Functions.show(PATH + "certificate-nosub.htm", player, npc);
			return false;
		}

		if (first)
			return true;

		for (ClassType2 type : ClassType2.VALUES)
		{
			if (player.getInventory().getCountOf(type.getCertificateId()) > 0 || player.getInventory().getCountOf(type.getTransformationId()) > 0)
			{
				Functions.show(PATH + "certificate-already.htm", player, npc);
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		return ClassId.VALUES[_class].toString() + " " + _level;
	}
}