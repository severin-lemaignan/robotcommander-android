/**
 * XMPP Client code borrowed from 
 * http://florentgarin.developpez.com/tutoriel/android/client-xmpp/
 * 
 */
package laas.openrobots.robotcommander;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;

public class XmppSender {

	private XMPPConnection m_connection;
	private Chat chat;
	
	public String recipient = "";
	private boolean isConnected = false;
	
	private ConnectionConfiguration config;

	private String login;
	private String pwd;
	private MessageListener msgListener;
	private FileTransferManager fileTransferManager;
	private FileTransferListener fileListener;
	
	public XmppSender() {
		super();
	}
	
	public void configure(String server_host, 
					  Integer server_port, 
					  String service_name, 
					  String login, 
					  String pwd,
					  boolean use_dnssrv, 
					  boolean use_sasl, 
					  boolean require_tls,
					  boolean use_compression,
					  MessageListener msgListener,
					  FileTransferListener fileListener) throws XMPPException {

		
		this.msgListener = msgListener;
		this.fileListener = fileListener;
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
			XMPPConnection connection = initConnection();
			
			fileTransferManager = new FileTransferManager(connection);
			fileTransferManager.addFileTransferListener(fileListener);
			
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

	public void setRecipient(String recipient) {
			this.recipient = recipient;
			
	        chat = m_connection.getChatManager().createChat(recipient, msgListener);
	}
	
	public String getRecipient() {
		return recipient;
	}
	
	public boolean send(String text) throws XMPPException {
		if (recipient == null) return false;
		if (!isConnected) return false;
		
		chat.sendMessage(text);
		
		return true;
	}

	private XMPPConnection initConnection() throws XMPPException {

		
		m_connection = new XMPPConnection(config);
		
        m_connection.connect();
        m_connection.login(login, pwd);
        //Presence presence = new Presence(Presence.Type.available);
        //m_connection.sendPacket(presence);
        
        return m_connection;
        
        
	}



}
