package l2r.gameserver.listener.reflection;

import l2r.commons.listener.Listener;
import l2r.gameserver.model.entity.Reflection;

public interface OnReflectionCollapseListener extends Listener<Reflection>
{
	public void onReflectionCollapse(Reflection reflection);
}
