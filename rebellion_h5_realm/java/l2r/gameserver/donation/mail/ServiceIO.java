package l2r.gameserver.donation.mail;

import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import l2r.gameserver.Config;

public class ServiceIO
{
	private final Session _session;
	private final Store _store;

	public ServiceIO() throws Exception
	{
		final Properties properties = new Properties();
		properties.put("mail.imap.host", "imap.gmail.com");
		properties.put("mail.imap.socketFactory.fallback", "false");
		properties.put("mail.imap.socketFactory.port", 993);
		properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.imap.auth", "true");
		properties.put("mail.imap.port", 993);
		/*
		properties.setProperty("mail.store.protocol", "imaps");
		properties.put("mail.imap.ssl.enable", "true");
		*/
		
		
		_session = Session.getDefaultInstance(properties, new javax.mail.Authenticator()
		{
			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(Config.MAIL_USER, Config.MAIL_PASS);
			}
		});
		
		_store = _session.getStore("imaps");
		_store.connect("imap.gmail.com", Config.MAIL_USER, Config.MAIL_PASS);
	}

	public Session getSession()
	{
		return _session;
	}

	public Store getStore()
	{
		return _store;
	}
}
