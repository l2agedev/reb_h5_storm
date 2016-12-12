package l2r.gameserver.utils;

import l2r.gameserver.Config;

public enum Language
{
	ENGLISH(1, "en"),
	RUSSIAN(8, "ru");

	public static final Language[] VALUES = Language.values();

	private int _clientIndex;
	private String _shortName;

	Language(int clientIndex, String shortName)
	{
		_clientIndex = clientIndex;
		_shortName = shortName;
	}

	public int getClientIndex()
	{
		return _clientIndex;
	}
	
	public String getShortName()
	{
		return _shortName;
	}
	
	public static Language getLanguage(int langId)
	{
		for (Language lang : VALUES)
		{
			if (lang.getClientIndex() == langId)
				return lang;
		}
		return Config.DEFAULT_LANG;
	}
	
	public static Language getLanguage(String shortName)
	{
		if (shortName != null)
		{
			for (Language lang : VALUES)
			{
				if (lang.getShortName().equalsIgnoreCase(shortName))
					return lang;
			}
		}
		return Config.DEFAULT_LANG;
	}
}
