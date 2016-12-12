package l2r.gameserver.tables;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakePcsTable
{
	private static final Logger _log = LoggerFactory.getLogger(FakePcsTable.class);
	
	private FastMap<Integer, FakePc> _fakePcs = new FastMap<Integer, FakePc>();

	public void init()
	{
		_log.info("FakePcsTable: Initializing data...");
		loadData();
	}
	
	private void loadData()
	{
		_fakePcs.clear();

		FakePc fpc = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `fake_pcs`");
			rset = statement.executeQuery();

			while(rset.next())
			{
				fpc = new FakePc();
				int npcId = rset.getInt("npc_id");
				fpc.race = rset.getInt("race");
				fpc.sex = rset.getInt("sex");
				fpc.clazz = rset.getInt("class");
				fpc.title = rset.getString("title");
				fpc.titleColor = Integer.decode("0x" + rset.getString("title_color"));
				fpc.name = rset.getString("name");
				fpc.nameColor = Integer.decode("0x" + rset.getString("name_color"));
				fpc.hairStyle = rset.getInt("hair_style");
				fpc.hairColor = rset.getInt("hair_color");
				fpc.face = rset.getInt("face");
				fpc.mount = rset.getByte("mount");
				fpc.team = rset.getByte("team");
				fpc.hero = rset.getByte("hero");
				fpc.pdUnder = rset.getInt("pd_under");
				fpc.pdUnderAug = rset.getInt("pd_under_aug");
				fpc.pdHead = rset.getInt("pd_head");
				fpc.pdHeadAug = rset.getInt("pd_head_aug");
				fpc.pdRHand = rset.getInt("pd_rhand");
				fpc.pdRHandAug = rset.getInt("pd_rhand_aug");
				fpc.pdLHand = rset.getInt("pd_lhand");
				fpc.pdLHandAug = rset.getInt("pd_lhand_aug");
				fpc.pdGloves = rset.getInt("pd_gloves");
				fpc.pdGlovesAug = rset.getInt("pd_gloves_aug");
				fpc.pdChest = rset.getInt("pd_chest");
				fpc.pdChestAug = rset.getInt("pd_chest_aug");
				fpc.pdLegs = rset.getInt("pd_legs");
				fpc.pdLegsAug = rset.getInt("pd_legs_aug");
				fpc.pdFeet = rset.getInt("pd_feet");
				fpc.pdFeetAug = rset.getInt("pd_feet_aug");
				fpc.pdBack = rset.getInt("pd_back");
				fpc.pdBackAug = rset.getInt("pd_back_aug");
				fpc.pdLRHand = rset.getInt("pd_lrhand");
				fpc.pdLRHandAug = rset.getInt("pd_lrhand_aug");
				fpc.pdHair = rset.getInt("pd_hair");
				fpc.pdHairAug = rset.getInt("pd_hair_aug");
				fpc.pdHair2 = rset.getInt("pd_hair2");
				fpc.pdHair2Aug = rset.getInt("pd_hair2_aug");
				fpc.pdRBracelet = rset.getInt("pd_rbracelet");
				fpc.pdRBraceletAug = rset.getInt("pd_rbracelet_aug");
				fpc.pdLBracelet = rset.getInt("pd_lbracelet");
				fpc.pdLBraceletAug = rset.getInt("pd_lbracelet_aug");
				fpc.pdDeco1 = rset.getInt("pd_deco1");
				fpc.pdDeco1Aug = rset.getInt("pd_deco1_aug");
				fpc.pdDeco2 = rset.getInt("pd_deco2");
				fpc.pdDeco2Aug = rset.getInt("pd_deco2_aug");
				fpc.pdDeco3 = rset.getInt("pd_deco3");
				fpc.pdDeco3Aug = rset.getInt("pd_deco3_aug");
				fpc.pdDeco4 = rset.getInt("pd_deco4");
				fpc.pdDeco4Aug = rset.getInt("pd_deco4_aug");
				fpc.pdDeco5 = rset.getInt("pd_deco5");
				fpc.pdDeco5Aug = rset.getInt("pd_deco5_aug");
				fpc.pdDeco6 = rset.getInt("pd_deco6");
				fpc.pdDeco6Aug = rset.getInt("pd_deco6_aug");
				fpc.enchantEffect = rset.getInt("enchant_effect");
				fpc.pvpFlag = rset.getInt("pvp_flag");
				fpc.karma = rset.getInt("karma");
				fpc.fishing = rset.getByte("fishing");
				fpc.fishingX = rset.getInt("fishing_x");
				fpc.fishingY = rset.getInt("fishing_y");
				fpc.fishingZ = rset.getInt("fishing_z");
				fpc.invisible = rset.getByte("invisible");
				_fakePcs.put(npcId, fpc);
			}
		}
		catch(Exception e)
		{
			_log.warn("FakePcsTable: LoadData couldnt be initialized:" + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
			_log.info("FakePcsTable: Fake NPC to PC system loaded. Cached " + _fakePcs.size() + " objects.");
		}
	}

	public void reloadData()
	{
		loadData();
		_log.info("Fake NPC to PC - RELOADED !!");
	}

	public FakePc getFakePc(int npcId)
	{
		return _fakePcs.get(npcId);
	}

	public static FakePcsTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final FakePcsTable _instance = new FakePcsTable();
	}
	
	public class FakePc
	{
		public int race;
		public int sex;
		public int clazz;
		public String title;
		public int titleColor;
		public String name;
		public int nameColor;
		public int hairStyle;
		public int hairColor;
		public int face;
		public byte mount;
		public byte team;
		public byte hero;
		public int pdUnder;
		public int pdUnderAug;
		public int pdHead;
		public int pdHeadAug;
		public int pdRHand;
		public int pdRHandAug;
		public int pdLHand;
		public int pdLHandAug;
		public int pdGloves;
		public int pdGlovesAug;
		public int pdChest;
		public int pdChestAug;
		public int pdLegs;
		public int pdLegsAug;
		public int pdFeet;
		public int pdFeetAug;
		public int pdBack;
		public int pdBackAug;
		public int pdLRHand;
		public int pdLRHandAug;
		public int pdHair;
		public int pdHairAug;
		public int pdHair2;
		public int pdHair2Aug;
		public int pdRBracelet;
		public int pdRBraceletAug;
		public int pdLBracelet;
		public int pdLBraceletAug;
		public int pdDeco1;
		public int pdDeco1Aug;
		public int pdDeco2;
		public int pdDeco2Aug;
		public int pdDeco3;
		public int pdDeco3Aug;
		public int pdDeco4;
		public int pdDeco4Aug;
		public int pdDeco5;
		public int pdDeco5Aug;
		public int pdDeco6;
		public int pdDeco6Aug;
		public int enchantEffect;
		public int pvpFlag;
		public int karma;
		public byte fishing;
		public int fishingX;
		public int fishingY;
		public int fishingZ;
		public byte invisible;
	}
}
