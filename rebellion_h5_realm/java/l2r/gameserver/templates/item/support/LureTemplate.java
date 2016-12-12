package l2r.gameserver.templates.item.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.util.Rnd;

public class LureTemplate
{
	private final int _itemId;
	private final int _lengthBonus;
	private final double _revisionNumber;
	private final double _rateBonus;
	private final LureType _lureType;

	private boolean _hardGrade = false;
	private boolean _easyGrade = false;
	private boolean _normalGrade = false;

	private final Map<FishGroup, Integer> _chances;

	@SuppressWarnings("unchecked")
	public LureTemplate(MultiValueSet<String> set)
	{
		_itemId = set.getInteger("item_id");
		_lengthBonus = set.getInteger("length_bonus");
		_revisionNumber = set.getDouble("revision_number");
		_rateBonus = set.getDouble("rate_bonus");
		_lureType = set.getEnum("type", LureType.class);
		_chances = (Map<FishGroup, Integer>)set.get("chances");

		for(FishGroup group : _chances.keySet())
			if (group.name().startsWith("HARD"))
				_hardGrade = true;
			else if (group.name().startsWith("EASY"))
				_easyGrade = true;
			else
				_normalGrade = true;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getLengthBonus()
	{
		return _lengthBonus;
	}

	public double getRevisionNumber()
	{
		return _revisionNumber;
	}

	public double getRateBonus()
	{
		return _rateBonus;
	}

	public LureType getLureType()
	{
		return _lureType;
	}

	public Map<FishGroup, Integer> getChances()
	{
		return _chances;
	}

	public int getChance(FishGroup group)
	{
		return _chances.containsKey(group) ? _chances.get(group) : 0;
	}

	public boolean isEasyGrade()
	{
		return _easyGrade;
	}

	public boolean isNormalGrade()
	{
		return _normalGrade;
	}

	public boolean isHardGrade()
	{
		return _hardGrade;
	}
	
	public FishGrade getGrade()
	{
		List<FishGrade> _chances = new ArrayList<FishGrade>();

		if (isEasyGrade())
			_chances.add(FishGrade.EASY);

		if (isNormalGrade())
		_chances.add(FishGrade.NORMAL);

		if (isHardGrade())
			_chances.add(FishGrade.HARD);

		if (_chances.size() == 0)
			return FishGrade.NORMAL;

		return _chances.get(_chances.size() - 1);
	}

	public FishGroup getGroup(FishGrade grade)
	{
		int chance = 0;
		for (Map.Entry<FishGroup, Integer> groups : _chances.entrySet())
			if (groups.getKey().name().startsWith(grade.name()) || grade.name().equals("NORMAL") || grade.name().equals("NIGHT"))
				chance += groups.getValue();
		
		chance = Rnd.get(chance);
		for (Map.Entry<FishGroup, Integer> groups : _chances.entrySet())
			if (groups.getKey().name().startsWith(grade.name()) || grade.name().equals("NORMAL") || grade.name().equals("NIGHT"))
			{
				if (groups.getValue() >= chance)
					return groups.getKey();
				chance -= groups.getValue();
			}
		
		return null;
	}
}
