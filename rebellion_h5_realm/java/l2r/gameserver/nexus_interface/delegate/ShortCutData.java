/**
 * 
 */
package l2r.gameserver.nexus_interface.delegate;

import l2r.gameserver.model.actor.instances.player.ShortCut;


/**
 * @author hNoke
 *
 */
public class ShortCutData
{
	private ShortCut _shortcut;
	
	public ShortCutData(int slotId, int pageId, int shortcutType, int shortcutId, int shortcutLevel, int characterType)
	{
		_shortcut = new ShortCut(slotId, pageId, shortcutType, shortcutId, shortcutLevel, characterType);
	}
	
	public int getId()
    {
        return _shortcut.getId();
    }

    public int getLevel()
    {
        return _shortcut.getLevel();
    }

    public int getPage()
    {
        return _shortcut.getPage();
    }

    public int getSlot()
    {
        return _shortcut.getSlot();
    }

    public int getType()
    {
        return _shortcut.getType();
    }
    
    public int getCharacterType()
    {
    	return _shortcut.getCharacterType();
    }
}
