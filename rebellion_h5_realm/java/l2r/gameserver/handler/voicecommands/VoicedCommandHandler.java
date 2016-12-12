package l2r.gameserver.handler.voicecommands;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.impl.AlternativeVoteReward;
import l2r.gameserver.handler.voicecommands.impl.CWHPrivileges;
import l2r.gameserver.handler.voicecommands.impl.Calculator;
import l2r.gameserver.handler.voicecommands.impl.Captcha;
import l2r.gameserver.handler.voicecommands.impl.CastleInfo;
import l2r.gameserver.handler.voicecommands.impl.Cfg;
import l2r.gameserver.handler.voicecommands.impl.CustomSecurity;
import l2r.gameserver.handler.voicecommands.impl.Debug;
import l2r.gameserver.handler.voicecommands.impl.Donate;
import l2r.gameserver.handler.voicecommands.impl.Hellbound;
import l2r.gameserver.handler.voicecommands.impl.Offline;
import l2r.gameserver.handler.voicecommands.impl.Offlinebuff;
import l2r.gameserver.handler.voicecommands.impl.Online;
import l2r.gameserver.handler.voicecommands.impl.Password;
import l2r.gameserver.handler.voicecommands.impl.Ping;
import l2r.gameserver.handler.voicecommands.impl.RandomCommands;
import l2r.gameserver.handler.voicecommands.impl.RecoverPasswordOnEmail;
import l2r.gameserver.handler.voicecommands.impl.Relocate;
import l2r.gameserver.handler.voicecommands.impl.Repair;
import l2r.gameserver.handler.voicecommands.impl.Security;
import l2r.gameserver.handler.voicecommands.impl.Time;
import l2r.gameserver.handler.voicecommands.impl.Wedding;
import l2r.gameserver.handler.voicecommands.impl.WhoAmI;

import java.util.HashMap;
import java.util.Map;

public class VoicedCommandHandler extends AbstractHolder
{
	private static final VoicedCommandHandler _instance = new VoicedCommandHandler();

	public static VoicedCommandHandler getInstance()
	{
		return _instance;
	}

	private Map<String, IVoicedCommandHandler> _datatable = new HashMap<String, IVoicedCommandHandler>();

	private VoicedCommandHandler()
	{
		registerVoicedCommandHandler(new Online());
		registerVoicedCommandHandler(new Hellbound());
		registerVoicedCommandHandler(new Cfg());
		registerVoicedCommandHandler(new CWHPrivileges());
		registerVoicedCommandHandler(new Offline());
		registerVoicedCommandHandler(new Password());
		registerVoicedCommandHandler(new Relocate());
		registerVoicedCommandHandler(new Repair());
		registerVoicedCommandHandler(new Wedding());
		registerVoicedCommandHandler(new WhoAmI());
		registerVoicedCommandHandler(new Debug());
		registerVoicedCommandHandler(new Security());
		registerVoicedCommandHandler(new RandomCommands());
		registerVoicedCommandHandler(new CastleInfo());
		registerVoicedCommandHandler(new Time());
		registerVoicedCommandHandler(new Captcha());
		registerVoicedCommandHandler(new RecoverPasswordOnEmail());
		registerVoicedCommandHandler(new CustomSecurity());
		registerVoicedCommandHandler(new Ping());
		registerVoicedCommandHandler(new Offlinebuff());
		registerVoicedCommandHandler(new Calculator());
		registerVoicedCommandHandler(new Donate());
		if (Config.ENABLE_ALT_VOTE_REWARD)
			registerVoicedCommandHandler(new AlternativeVoteReward());
	}

	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for(String element : ids)
			_datatable.put(element, handler);
	}

	public void removeVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for(String element : ids)
			_datatable.remove(element);
	}
	
	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if(voicedCommand.indexOf(" ") != -1)
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));

		return _datatable.get(command);
	}

	@Override
	public int size()
	{
		return _datatable.size();
	}

	@Override
	public void clear()
	{
		_datatable.clear();
	}
}
