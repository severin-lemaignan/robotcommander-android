/**
 * XMPP Client code borrowed from 
 * http://florentgarin.developpez.com/tutoriel/android/client-xmpp/
 * 
 */
package laas.openrobots.robotcommander;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

public class XmppSender {
	

	private final static String SERVER_HOST = "talk.google.com";
	private final static int SERVER_PORT = 5222;
	private final static String SERVICE_NAME = "gmail.com";	
	private final static String LOGIN = "jidowanki@gmail.com";
	private final static String PASSWORD = "jidojido";


	private XMPPConnection m_connection;
	
	public String recipient;
	private boolean isConnected = false;

	
	public XmppSender(String recipient) {
		super();
		
		this.recipient = recipient;
		
		try {
			initConnection();
			isConnected = true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	public boolean send(String text) {
		if (!isConnected) return false;
		
		Message msg = new Message(recipient, Message.Type.chat);
		msg.setBody(text);
		m_connection.sendPacket(msg);
		
		return true;
	}

	private void initConnection() throws XMPPException {

        ConnectionConfiguration config =
                new ConnectionConfiguration(SERVER_HOST, SERVER_PORT, SERVICE_NAME);
        m_connection = new XMPPConnection(config);
        m_connection.connect();
        m_connection.login(LOGIN, PASSWORD);
        Presence presence = new Presence(Presence.Type.available);
        m_connection.sendPacket(presence);
     
	}

}