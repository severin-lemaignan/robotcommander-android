/**
 * XMPP Client code borrowed from 
 * http://florentgarin.developpez.com/tutoriel/android/client-xmpp/
 * 
 */
package laas.openrobots.robotcommander;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.packet.*;

public class XmppSender {

	private XMPPConnection m_connection;
	
	public String recipient;
	private boolean isConnected = false;
	
	private ConnectionConfiguration config;

	private String login;
	private String pwd;
	
	public XmppSender() {
		super();
		
		Connection connection = new XMPPConnection("laas.fr");
		try {
			connection.connect();
			connection.login("jidotab", "tx+OI3VY");
			Chat chat = connection.getChatManager().createChat("slemaign@laas.fr", new MessageListener() {

			    public void processMessage(Chat chat, Message message) {
			        System.out.println("Received message: " + message);
			    }
			});
			chat.sendMessage("Howdy!");
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void configure(String server_host, 
					  Integer server_port, 
					  String service_name, 
					  String login, 
					  String pwd,
					  boolean use_dnssrv, 
					  boolean use_sasl, 
					  boolean require_tls,
					  boolean use_compression) throws XMPPException {

		
		this.login = login;
		this.pwd = pwd;
		
		if (use_dnssrv) {
			if (service_name == "") throw new XMPPException("Undefined XMPP service name");
			config = new ConnectionConfiguration(service_name);
		}
		else {
			if (server_host == "") throw new XMPPException("Undefined XMPP server");
		
			if (server_port == 0) server_port = 5222;
			
			if (service_name != "") {
				config = new ConnectionConfiguration(server_host, 
													server_port.intValue(),
													service_name);
			} else {
				config = new ConnectionConfiguration(server_host, 
						server_port.intValue());
			}
		}
				
		config.setSASLAuthenticationEnabled(use_sasl);
		if (require_tls) config.setSecurityMode(SecurityMode.required);
		config.setCompressionEnabled(use_compression);

	}
	
	public String connect(){
		try {
			initConnection();
			isConnected = true;
			return "Connected.";
			
		} catch (XMPPException e) {
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
	}
	
	public void disconnect(){
		m_connection.disconnect();
		isConnected = false;
	}
	
	public boolean connected() {
		return this.isConnected;
	}

	public void recipient(String recipient) {
			this.recipient = recipient;
	}
	
	public boolean send(String text) {
		if (recipient == null) return false;
		if (!isConnected) return false;
		
		Message msg = new Message(recipient, Message.Type.chat);
		msg.setBody(text);
		m_connection.sendPacket(msg);
		
		return true;
	}

	private void initConnection() throws XMPPException {

		
		m_connection = new XMPPConnection(config);
		
        m_connection.connect();
        m_connection.login(login, pwd);
        Presence presence = new Presence(Presence.Type.available);
        m_connection.sendPacket(presence);
     
	}



}
