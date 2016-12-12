package l2r.gameserver.skills.effects;

import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Playable;
import l2r.gameserver.stats.Env;
import l2r.gameserver.utils.ItemFunctions;

public class EffectRestoration extends Effect {
    private final int itemId;
    private final long count;

    public EffectRestoration(Env env, EffectTemplate template) {
        super(env, template);
        String item = getTemplate().getParam().getString("Item");
        itemId = Integer.parseInt(item.split(":")[0]);
        count = Long.parseLong(item.split(":")[1]);

    }

    @Override
    public void onStart() {
        super.onStart();
        ItemFunctions.addItem((Playable) getEffected(), itemId, count, true);
    }

    @Override
    protected boolean onActionTime() {
        return false;
    }
}
