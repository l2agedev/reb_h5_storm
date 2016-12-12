package l2r.gameserver.data.xml.holder;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.templates.PlayerTemplate;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.templates.item.CreateItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ro0TT
 * @date 18.4.2012
 **/

@SuppressWarnings( { "nls", "unqualified-field-access", "boxing" })
public class CharTemplateHolder extends AbstractHolder {

	private static CharTemplateHolder _instance;

	private Map<Integer, PlayerTemplate> _templates;

	public static CharTemplateHolder getInstance()
	{
		if(_instance == null)
			_instance = new CharTemplateHolder();
		return _instance;
	}

	private CharTemplateHolder()
	{
		_templates = new HashMap<Integer, PlayerTemplate>(206);
	}

	public void addTemplate(int classId, StatsSet set, List<CreateItem> items)
	{
		set.set("collision_radius", set.get("male_collision_radius"));
		set.set("collision_height", set.get("male_collision_height"));
		_templates.put(classId, new PlayerTemplate(classId, set, true, items));

		set.set("collision_radius", set.get("female_collision_radius"));
		set.set("collision_height", set.get("female_collision_height"));
		_templates.put(classId | 0x100, new PlayerTemplate(classId, set, false, items));
	}

	public PlayerTemplate getTemplate(ClassId classId, boolean female)
	{
		return getTemplate(classId.getId(), female);
	}

	public PlayerTemplate getTemplate(int classId, boolean female)
	{
		int key = classId;
		if(female)
			key |= 0x100;
		return _templates.get(key);
	}

	@Override
	public int size() {
		return _templates.size();
	}

	@Override
	public void clear() {
		_templates.clear();
	}
}
