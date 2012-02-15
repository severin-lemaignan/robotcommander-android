package laas.openrobots.robotcommander;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConnectionManager implements OnCheckedChangeListener, MessageListener {
	

	private XmppSender xmppSender;
	private RobotCommanderActivity activity;
	private boolean xmpp_ok = false;
	
	public ConnectionManager(RobotCommanderActivity activity) {
		super();
		
		this.activity = activity;
		    	
	    xmppSender = new XmppSender();
		
	}

	public boolean send(String msg) {
		
		if (!xmppSender.connected()) {
			Toast.makeText(activity, "Not connected!", Toast.LENGTH_SHORT).show();
			return false;
		}
	
		boolean ok;
		try {
			 ok = xmppSender.send(msg);
		} catch (XMPPException e) {
			Toast.makeText(activity, "XMPP error while sending the message: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
		return ok;
	}
	
	public void connect() {
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
    	
    	String server_host = sharedPref.getString("host", ""); 
	    //Integer server_port = sharedPref.getInt("port", 0);
    	Integer server_port = Integer.parseInt(sharedPref.getString("port", "0"));
	    String service_name = sharedPref.getString("servicename", "");
	    String login = sharedPref.getString("username", "");
	    String pwd = sharedPref.getString("password", "");
	    boolean use_dnssrv = !sharedPref.getBoolean("usecustomhost", true);
	    boolean use_sasl = sharedPref.getBoolean("sasl", true);
	    boolean require_tls = sharedPref.getBoolean("ssltls", true);
	    boolean use_compression = sharedPref.getBoolean("compression", false);
	    
		try {
			xmppSender.configure( 
					server_host, server_port, 
					service_name, 
					login, pwd, 
					use_dnssrv, 
					use_sasl, require_tls, use_compression,
					this);
			
			xmpp_ok = true;
		} catch (XMPPException e) {
			Toast.makeText(activity, "XMPP configuration error!", Toast.LENGTH_SHORT).show();
		}
		
		if (xmpp_ok) {
			String msg = xmppSender.connect();
			
			Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
			
	        if (xmppSender.connected()) {
	        	activity.setConnected();
	        }
	        else {
	        	activity.setNotConnected();
	        }
		}

	}
	
	public void disconnect() {
		xmppSender.disconnect();
        activity.setNotConnected();
     }

	

    void setRecipient(){
    	
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
    	String mode = sharedPref.getString(Preferences.KEY_SELECTED_ROBOT, "");
    	
    	String recipient = ""; 
    	if (mode.equals("pr2")) {
    		recipient = "max@laas.fr";
    	}
    	else if (mode.equals("jido")) {
    		recipient = "jido@laas.fr";
    	}
    	else {
    		recipient = sharedPref.getString(Preferences.KEY_DISTANT_ACCOUNT, "");
    	}
    	
    	if (!xmppSender.getRecipient().equals(recipient)) {
	    	xmppSender.setRecipient(recipient);
	    	Toast.makeText(activity, "Messages will be sent to " + recipient, 
	    					Toast.LENGTH_SHORT).show();
    	}
    }

    public void processMessage(Chat chat, Message message) {
    	activity.asyncAddToTranscript("            Robot says: " + message.getBody());
    	activity.say(message.getBody());
    }
    
	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		if (v.getId() == R.id.cb_connection_status) {
			if (isChecked && !xmppSender.connected()) {
				Toast.makeText(activity, "Trying to connect...", Toast.LENGTH_SHORT).show();
				connect();
			} else if (!isChecked && xmppSender.connected()) {
				Toast.makeText(activity, "Disconnecting...", Toast.LENGTH_SHORT).show();
				disconnect();
			}
	    }
		
	}

}
