package l2r.gameserver.model.petition;

import l2r.gameserver.handler.petition.IPetitionHandler;
import l2r.gameserver.scripts.Scripts;

public class PetitionSubGroup extends PetitionGroup
{
	private final IPetitionHandler _handler;

	public PetitionSubGroup(int id, String handler)
	{
		super(id);

		Class<?> clazz = Scripts.getInstance().getClasses().get("handler.petition." + handler);

		try
		{
			_handler = (IPetitionHandler)clazz.newInstance();
		}
		catch(Exception e)
		{
			throw new Error(e);
		}
	}

	public IPetitionHandler getHandler()
	{
		return _handler;
	}
}
