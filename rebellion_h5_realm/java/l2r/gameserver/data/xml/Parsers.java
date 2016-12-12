package l2r.gameserver.data.xml;

import l2r.gameserver.Config;
import l2r.gameserver.data.StringHolder;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.BuyListHolder;
import l2r.gameserver.data.xml.holder.MultiSellHolder;
import l2r.gameserver.data.xml.holder.ProductHolder;
import l2r.gameserver.data.xml.holder.RecipeHolder;
import l2r.gameserver.data.xml.parser.AirshipDockParser;
import l2r.gameserver.data.xml.parser.ArmorSetsParser;
import l2r.gameserver.data.xml.parser.CharTemplateParser;
import l2r.gameserver.data.xml.parser.CubicParser;
import l2r.gameserver.data.xml.parser.DomainParser;
import l2r.gameserver.data.xml.parser.DoorParser;
import l2r.gameserver.data.xml.parser.EnchantItemParser;
import l2r.gameserver.data.xml.parser.EventParser;
import l2r.gameserver.data.xml.parser.FishParser;
import l2r.gameserver.data.xml.parser.HennaParser;
import l2r.gameserver.data.xml.parser.InstantZoneParser;
import l2r.gameserver.data.xml.parser.ItemParser;
import l2r.gameserver.data.xml.parser.NpcParser;
import l2r.gameserver.data.xml.parser.OptionDataParser;
import l2r.gameserver.data.xml.parser.PetitionGroupParser;
import l2r.gameserver.data.xml.parser.ResidenceParser;
import l2r.gameserver.data.xml.parser.RestartPointParser;
import l2r.gameserver.data.xml.parser.SkillAcquireParser;
import l2r.gameserver.data.xml.parser.SoulCrystalParser;
import l2r.gameserver.data.xml.parser.SpawnParser;
import l2r.gameserver.data.xml.parser.StaticObjectParser;
import l2r.gameserver.data.xml.parser.ZoneParser;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SpawnTable;

public abstract class Parsers
{
	public static void parseAll()
	{
		HtmCache.getInstance().reload();
		StringHolder.getInstance().load();
		//
		SkillTable.getInstance().load(); // - SkillParser.getInstance();
		
		if (!Config.DONTLOADOPTIONDATA)
			OptionDataParser.getInstance().load();
		ItemParser.getInstance().load();
		//
		NpcParser.getInstance().load();

		DomainParser.getInstance().load();
		RestartPointParser.getInstance().load();

		StaticObjectParser.getInstance().load();
		DoorParser.getInstance().load();
		ZoneParser.getInstance().load();
		SpawnTable.getInstance();
		SpawnParser.getInstance().load();
		InstantZoneParser.getInstance().load();

		ReflectionManager.getInstance();
		//
		AirshipDockParser.getInstance().load();
		SkillAcquireParser.getInstance().load();
		//
		CharTemplateParser.getInstance().load();
		//
		ResidenceParser.getInstance().load();
		EventParser.getInstance().load();
		// support(cubic & agathion)
		CubicParser.getInstance().load();
		//
		BuyListHolder.getInstance();
		RecipeHolder.getInstance();
		if (!Config.DONTLOADMULTISELLS)
			MultiSellHolder.getInstance();
		ProductHolder.getInstance();
		// AgathionParser.getInstance();
		// item support
		HennaParser.getInstance().load();
		EnchantItemParser.getInstance().load();
		SoulCrystalParser.getInstance().load();
		ArmorSetsParser.getInstance().load();

		// Fish
		FishParser.getInstance().load();
		
		// etc
		PetitionGroupParser.getInstance().load();
	}
}
