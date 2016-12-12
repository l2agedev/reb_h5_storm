package l2r.gameserver.data.xml.holder;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.templates.CubicTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

public final class CubicHolder extends AbstractHolder
{
	private static CubicHolder _instance = new CubicHolder();
	private final TIntObjectHashMap<CubicTemplate> _cubics = new TIntObjectHashMap<CubicTemplate>(10);

	public static CubicHolder getInstance()
	{
		return _instance;
	}

	private CubicHolder()
	{
	}

	public void addCubicTemplate(CubicTemplate template)
	{
		_cubics.put(hash(template.getId(), template.getLevel()), template);
	}

	public CubicTemplate getTemplate(int id, int level)
	{
		return _cubics.get(hash(id, level));
	}

	public int hash(int id, int level)
	{
		return id * 10000 + level;
	}

	@Override
	public int size()
	{
		return _cubics.size();
	}

	@Override
	public void clear()
	{
		_cubics.clear();
	}
}
