package laas.openrobots.robotcommander;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//import android.widget.Toast;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class RobotCommanderActivity extends Activity implements OnClickListener, OnKeyListener {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private static final int DIALOG_CHOOSE_SPEECH_RECO_RESULT = 0;
	private static final int DIALOG_NO_XMPP_CONNECTION = 1;

    private XmppSender xmppSender;
    
    private ArrayList<String> speech_matches;
    
    private ListView transcript;
    private EditText edittext;

    private ArrayAdapter<String> transcriptAdapter;
    
    private ArrayList<String> transcriptContent = new ArrayList<String>();
    
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        xmppSender = new XmppSender("skadge@gmail.com");
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.main);

        // Get display items for later interaction
        Button speakButton = (Button) findViewById(R.id.btn_speak);
        
        edittext = (EditText) findViewById(R.id.command_text_input);
        
        edittext.setOnKeyListener(this);

        transcript = (ListView) findViewById(R.id.transcript);
        
        transcriptAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, transcriptContent);
        transcript.setAdapter(transcriptAdapter);

        // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
        } else {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }
    }
    
    protected Dialog onCreateDialog(int id) {

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	switch(id) {
        case DIALOG_CHOOSE_SPEECH_RECO_RESULT:
        	CharSequence[] items = speech_matches.toArray(new CharSequence[speech_matches.size()]);

        	builder.setTitle("Did you mean...");
        	builder.setItems(items, new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int item) {
                	if (!xmppSender.send(speech_matches.get(item).toString())){
                		showDialog(DIALOG_NO_XMPP_CONNECTION);
                	}
                	
                    transcriptAdapter.add(speech_matches.get(item).toString());
                    removeDialog(DIALOG_CHOOSE_SPEECH_RECO_RESULT);
        	    }
        	});
        	builder.setCancelable(true);        	
        	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	removeDialog(DIALOG_CHOOSE_SPEECH_RECO_RESULT);
                }
        	});
        	builder.setNeutralButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	removeDialog(DIALOG_CHOOSE_SPEECH_RECO_RESULT);
                    startVoiceRecognitionActivity();                    
                }
        	});
        	return builder.create();
        
        case DIALOG_NO_XMPP_CONNECTION:

        	builder.setMessage("No connection to the XMPP server! Your message won't be send to the robot.")
        	       .setCancelable(true)
        		   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	dismissDialog(DIALOG_NO_XMPP_CONNECTION);
                }
        	});
        	return builder.create();
        }
        return null;
    }

    /**
     * Handle the click on the start recognition button.
     */
    public void onClick(View v) {
        if (v.getId() == R.id.btn_speak) {
            startVoiceRecognitionActivity();
        }
    }
    
    /**
     * Handle the text in the text input area.
     */
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // If the event is a key-down event
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                	String msg = edittext.getText().toString();
                	
                	if (!xmppSender.send(msg)){
                		showDialog(DIALOG_NO_XMPP_CONNECTION);
                	}
                	
                    transcriptAdapter.add(msg);
                    edittext.setText(null);
                    return true;
            }
        }

        return false;
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell the robot what to do");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            speech_matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            showDialog(DIALOG_CHOOSE_SPEECH_RECO_RESULT);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
