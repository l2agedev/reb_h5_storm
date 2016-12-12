package l2r.gameserver.network;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.net.nio.impl.MMOClient;
import l2r.commons.net.nio.impl.MMOConnection;
import l2r.gameserver.Config;
import l2r.gameserver.SecondaryPasswordAuth;
import l2r.gameserver.dao.AccountPointsDAO;
import l2r.gameserver.dao.AccountsDAO;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.OlympiadHistoryDAO;
import l2r.gameserver.dao.OlympiadNobleDAO;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.model.CharSelectInfoPackage;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.PcCondOverride;
import l2r.gameserver.model.entity.L2Event;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.loginservercon.SessionKey;
import l2r.gameserver.network.loginservercon.gspackets.PlayerLogout;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.utils.Language;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GameClient extends MMOClient<MMOConnection<GameClient>>
{
	private static final Logger _log = LoggerFactory.getLogger(GameClient.class);
	private static final String NO_IP = "?.?.?.?";
	private SecondaryPasswordAuth _secondaryAuth;
	public static boolean SESSION_OK = MMOClient.SESSION_OK;
	
	public GameCrypt _crypt = null;
	
	public GameClientState _state;
	
	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		ENTER_GAME,
		IN_GAME,
		DISCONNECTED
	}
	
	/** Данные аккаунта */
	private String _login;
	private AccountData _accountData;
	private double _bonus = 1.0;
	private int _bonusExpire;
	
	private Player _activeChar;
	private SessionKey _sessionKey;
	private String _ip = NO_IP;
	private int revision = 0;
	private boolean _gameGuardOk = false;
	// private SecondaryPasswordAuth _secondaryAuth;
	
	private Language _language = Language.ENGLISH;
	
	private List<Integer> _charSlotMapping = new ArrayList<Integer>();
	
	public GameClient(MMOConnection<GameClient> con)
	{
		super(con);
		
		_state = GameClientState.CONNECTED;
		_crypt = new GameCrypt();
		if (con != null)
			_ip = con.getSocket().getInetAddress().getHostAddress();
	}
	
	@Override
	protected void onDisconnection()
	{
		final Player player;
		
		try
		{
			setState(GameClientState.DISCONNECTED);
			player = getActiveChar();
			
			if (player != null)
				player.logout(true);
			
			setActiveChar(null);
			
			// we store all data from players who are disconnected while in an event in order to restore it in the next login
			if (L2Event.isParticipant(player))
				L2Event.savePlayerEventStatus(player);
			if (getSessionKey() != null)
			{
				if (isAuthed())
				{
					AuthServerCommunication.getInstance().removeAuthedClient(getLogin());
					AuthServerCommunication.getInstance().sendPacket(new PlayerLogout(getLogin()));
				}
				else
				{
					AuthServerCommunication.getInstance().removeWaitingClient(getLogin());
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception at GameClient that would break SelectorThread. ", e);
		}
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		// TODO Auto-generated method stub
		
	}
	
	public void markRestoredChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return;
		
		if (_activeChar != null && _activeChar.getObjectId() == objid)
			_activeChar.setDeleteTimer(0);
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void markToDeleteChar(int charslot) throws Exception
	{
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return;
		
		if (_activeChar != null && _activeChar.getObjectId() == objid)
			_activeChar.setDeleteTimer((int) (System.currentTimeMillis() / 1000));
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
			statement.setLong(1, (int) (System.currentTimeMillis() / 1000L));
			statement.setInt(2, objid);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.error("data error on update deletime char:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void deleteChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		if (_activeChar != null)
			return;
		
		int objid = getObjectIdForSlot(charslot);
		if (objid == -1)
			return;
		
		CharacterDAO.getInstance().deleteCharByObjId(objid);
		
		OlympiadNobleDAO.getInstance().delete(objid);

	    OlympiadHistoryDAO.getInstance().clearHistory(objid);
	}
	
	public Player loadCharFromDisk(int charslot)
	{
		int objectId = getObjectIdForSlot(charslot);
		if (objectId == -1)
			return null;
		
		Player character = null;
		Player oldPlayer = GameObjectsStorage.getPlayer(objectId);
		
		if (oldPlayer != null)
			if (oldPlayer.isInOfflineMode() || oldPlayer.isLogoutStarted())
			{
				// оффтрейдового чара проще выбить чем восстанавливать
				oldPlayer.kick();
				return null;
			}
			else
			{
				oldPlayer.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
				
				GameClient oldClient = oldPlayer.getClient();
				if (oldClient != null)
				{
					oldClient.setActiveChar(null);
					oldClient.closeNow(false);
				}
				oldPlayer.setClient(this);
				character = oldPlayer;
			}
		
		if (character == null)
			character = Player.restore(objectId);
		
		if (character != null)
			setActiveChar(character);
		else
			_log.warn("could not restore obj_id: " + objectId + " in slot:" + charslot);
		
		return character;
	}
	
	public int getObjectIdForSlot(int charslot)
	{
		if (charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.warn(getLogin() + " tried to modify Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return _charSlotMapping.get(charslot);
	}
	
	public Player getActiveChar()
	{
		return _activeChar;
	}
	
	/**
	 * @return Returns the sessionId.
	 */
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}
	
	public String getLogin()
	{
		return _login;
	}
	
	public AccountData getAccountData()
	{
		return _accountData;
	}
	
	public void updateAccountData()
	{
		AccountsDAO.deleteAccountData(_login);
		
		 // Maybe better config it with timeout, but it shouldnt delay more than 10ms.
		_accountData = AccountsDAO.getAccountData(_login, 1000);
	}
	
	public void setLoginName(String loginName)
	{
		_login = loginName;
		if (Config.SECOND_AUTH_ENABLED)
			_secondaryAuth = new SecondaryPasswordAuth(this);
	}
	
	public void setActiveChar(Player player)
	{
		_activeChar = player;
		if (player != null)
			player.setClient(this);
	}
	
	public void setSessionId(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}
	
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();
		
		for (CharSelectInfoPackage element : chars)
		{
			int objectId = element.getObjectId();
			_charSlotMapping.add(objectId);
		}
	}
	
	public void setCharSelection(int c)
	{
		_charSlotMapping.clear();
		_charSlotMapping.add(c);
	}
	
	public int getRevision()
	{
		return revision;
	}
	
	public void setRevision(int revision)
	{
		this.revision = revision;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret = _crypt.decrypt(buf.array(), buf.position(), size);
		return ret;
	}
	
	public void sendPacket(final L2GameServerPacket gsp)
	{
		if (gsp.isInvisible() && (getActiveChar() != null) && !getActiveChar().canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS))
			return;
		
		if (getActiveChar() != null && getActiveChar().getSupersnoop().isActive())
			getActiveChar().getSupersnoop().sendPacket(gsp);
		
		if (isConnected())
			getConnection().sendPacket(gsp);
	}
	
	public void sendPacket(final L2GameServerPacket... gsp)
	{
		for (int i = 0; i < gsp.length; i++)
		{
			if (gsp[i].isInvisible() && (getActiveChar() != null) && !getActiveChar().canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS))
				gsp[i] = null;
		}
		
		if (getActiveChar() != null && getActiveChar().getSupersnoop().isActive())
			getActiveChar().getSupersnoop().sendPacket(gsp);
		
		if (isConnected())
			getConnection().sendPacket(gsp);
	}
	
	public void sendPackets(final List<L2GameServerPacket> gsp)
	{
		for (int i = 0; i < gsp.size(); i++)
		{
			if (gsp.get(i) != null && gsp.get(i).isInvisible() && (getActiveChar() != null) && !getActiveChar().canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS))
				gsp.set(i, null);
		}
		
		if (getActiveChar() != null && getActiveChar().getSupersnoop().isActive())
			getActiveChar().getSupersnoop().sendPacket(gsp.toArray(new L2GameServerPacket[gsp.size()]));
		
		if (isConnected())
			getConnection().sendPackets(gsp);
	}
	
	public void close(L2GameServerPacket gsp)
	{
		if (isConnected())
			getConnection().close(gsp);
	}
	
	public String getIpAddr()
	{
		return _ip;
	}
	
	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}
	
	public double getBonus()
	{
		return _bonus;
	}
	
	public int getBonusExpire()
	{
		return _bonusExpire;
	}
	
	public void setBonus(double bonus)
	{
		_bonus = bonus;
	}
	
	public void setBonusExpire(int bonusExpire)
	{
		_bonusExpire = bonusExpire;
	}
	
	public int getPointG()
	{
		return AccountPointsDAO.getInstance().getPoint(getLogin());
	}
	
	public void setPointG(int point)
	{
		AccountPointsDAO.getInstance().setPoint(getLogin(), point);
	}
	
	public GameClientState getState()
	{
		return _state;
	}
	
	public void setState(GameClientState state)
	{
		_state = state;
	}
	
	public Language getLanguage()
	{
		return _language;
	}
	
	public void setLanguage(Language language)
	{
		_language = language;
	}
	  
	private int _failedPackets = 0;
	private int _unknownPackets = 0;
	
	public void onPacketReadFail()
	{
		if (_failedPackets++ >= 10)
		{
			_log.warn("Too many client packet fails, connection closed : " + this);
			closeNow(true);
		}
	}
	
	public void onUnknownPacket()
	{
		if (_unknownPackets++ >= 10)
		{
			_log.warn("Too many client unknown packets, connection closed : " + this);
			closeNow(true);
		}
	}
	
	public void checkHwid(String allowedHwid)
	{
		if (getHWID() != null && !getHWID().equalsIgnoreCase(allowedHwid) && !allowedHwid.equalsIgnoreCase(""))
			closeNow(false);
	}
	
	@Override
	public String toString()
	{
		return _state + " IP: " + getIpAddr() + (_login == null ? "" : " Account: " + _login) + (_activeChar == null ? "" : " Player : " + _activeChar);
	}
	
	private String _hwid;
	private int _instancesCount;
	
	public String getHWID()
	{
		return _hwid;
	}
	
	public void setHWID(String hwid)
	{
		_hwid = hwid;
	}
	
	public SecondaryPasswordAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}
	
	public void setGameGuardOk(boolean gameGuardOk)
	{
		_gameGuardOk = gameGuardOk;
	}
	
	public boolean isGameGuardOk()
	{
		return _gameGuardOk;
	}
	
	private static byte[] _keyClientEn = new byte[8];
	
	public static void setKeyClientEn(byte[] key)
	{
		_keyClientEn = key;
	}
	
	public static byte[] getKeyClientEn()
	{
		return _keyClientEn;
	}
	
	public int getInstanceCount()
	{
		return _instancesCount;
	}
	
	public void setInstanceCount(int count)
	{
		_instancesCount = count;	
	}
}