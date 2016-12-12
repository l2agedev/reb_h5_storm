package l2r.gameserver.network.serverpackets;

import l2r.gameserver.network.serverpackets.components.NpcString;

public class ExShowScreenMessage extends NpcStringContainer
{
	public static enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT,
	}

	public static final int SYSMSG_TYPE = 0;
	public static final int STRING_TYPE = 1;

	private int _type, _sysMessageId;
	private boolean _big_font, _effect;
	private ScreenMessageAlign _text_align;
	private int _time;
	private int _unk1;
	private int _unk2;
	private int _unk3;
	private boolean _fade;
	
	public ExShowScreenMessage(int type, int messageId, ScreenMessageAlign text_align, int unk1, int unk2, int unk3, boolean big_font, boolean showEffect, int time, boolean fade, String text)
	{
		super(NpcString.NONE, text);
		_type = type;
		_sysMessageId = messageId;
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_fade = fade;
		_text_align = text_align;
		_time = time;
		_big_font = big_font;
		_effect = showEffect;
	}
	
	public ExShowScreenMessage(String text, int time)
	{
		this(text, time, ScreenMessageAlign.TOP_CENTER, false, 1, -1, false);
	}
	
	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font)
	{
		this(text, time, text_align, big_font, 1, -1, false);
	}

	public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font, int type, int messageId, boolean showEffect)
	{
		super(NpcString.NONE, text);
		_type = type;
		_sysMessageId = messageId;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
	}

	public ExShowScreenMessage(NpcString t, int time, ScreenMessageAlign text_align, String... params)
	{
		this(t, time, text_align, true, STRING_TYPE, -1, false, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, String... params)
	{
		this(npcString, time, text_align, big_font, STRING_TYPE, -1, false, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, boolean showEffect, String... params)
	{
		this(npcString, time, text_align, big_font, STRING_TYPE, -1, showEffect, params);
	}

	public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, int type, int systemMsg, boolean showEffect, String... params)
	{
		super(npcString, params);
		_type = type;
		_sysMessageId = systemMsg;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x39);
		writeD(_type); // 0 - system messages, 1 - your defined text, 2 - npcstring
		writeD(_sysMessageId); // system message id (_type must be 0 otherwise no effect)
		writeD(_text_align.ordinal() + 1); // размещение текста
		writeD(_unk1); // ?
		writeD(_big_font ? 0 : 1); // размер текста
		writeD(_unk2); // ?
		writeD(_unk3); // ?
		writeD(_effect ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		writeD(_time); // время отображения сообщения в милисекундах
		writeD(_fade == true ? 1 : 0); // fade effect (0 - disabled, 1 enabled)
		writeElements();
	}
}