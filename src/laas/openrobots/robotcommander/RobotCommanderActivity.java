package laas.openrobots.robotcommander;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RobotCommanderActivity extends Activity implements OnClickListener, OnKeyListener, TextToSpeech.OnInitListener {

	private static final String LOG_TTS = "TextToSpeech";
	
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private static final int PREFERENCES_REQUEST_CODE = 1235;
        
	private static final int DIALOG_CHOOSE_SPEECH_RECO_RESULT = 0;
	private static final int DIALOG_NO_XMPP_CONNECTION = 1;
	
    
    private ArrayList<String> speech_matches;
    
    private ListView transcript;
    private EditText edittext;

    //private ArrayAdapter<String> transcriptAdapter;
    private TranscriptAdapter transcriptAdapter;
    
    private ArrayList<String> transcriptContent = new ArrayList<String>();
	private ConnectionManager connectionManager;
    
	private TextToSpeech mTts;
	
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.robotcommander_preferences, true);
        
        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(this,
            this  // TextToSpeech.OnInitListener
            );
        
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.main);

        // Get display items for later interaction
        ImageButton speakButton = (ImageButton) findViewById(R.id.btn_speak);
        
        ImageButton settingsButton = (ImageButton) findViewById(R.id.btn_settings);
        settingsButton.setOnClickListener(this);
        
        Button helloButton = (Button) findViewById(R.id.btn_hello_robot);
        helloButton.setOnClickListener(this);
        Button forgetButton = (Button) findViewById(R.id.btn_forget_it);
        forgetButton.setOnClickListener(this);
        
        edittext = (EditText) findViewById(R.id.command_text_input);
        
        edittext.setOnKeyListener(this);

        transcript = (ListView) findViewById(R.id.transcript);
        transcript.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		publish(((TextView) view).getText().toString());
        	}        	
        });
        
        //transcriptAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, transcriptContent);
        transcriptAdapter = new TranscriptAdapter(this, android.R.layout.simple_list_item_1);
        transcript.setAdapter(transcriptAdapter);

        // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
            speakButton.requestFocus();
        } else {
            speakButton.setEnabled(false);
            Toast.makeText(this, "Recognizer not present", Toast.LENGTH_SHORT).show();
        }

        connectionManager = new ConnectionManager(this);
        
        CheckBox connectionStatusCheckBox = (CheckBox) findViewById(R.id.cb_connection_status);
        connectionStatusCheckBox.setOnCheckedChangeListener(connectionManager);
        
        connectionManager.connect();
        connectionManager.setRecipient(); //Initializes account from the last preferences state

    }
    
    public void setConnected() {
    	CheckBox connectionStatusCheckBox = (CheckBox) findViewById(R.id.cb_connection_status);
        TextView connectionStatus = (TextView) findViewById(R.id.text_connection_status);
        
    	connectionStatus.setText(this.getString(R.string.status_connected));
    	connectionStatusCheckBox.setEnabled(true);
    	connectionStatusCheckBox.setChecked(true);
    }
    
    public void setNotConnected() {
    	CheckBox connectionStatusCheckBox = (CheckBox) findViewById(R.id.cb_connection_status);
        TextView connectionStatus = (TextView) findViewById(R.id.text_connection_status);

    	connectionStatus.setText(this.getString(R.string.status_not_connected));
    	connectionStatusCheckBox.setEnabled(true);
    	connectionStatusCheckBox.setChecked(false);
    }
    
    protected Dialog onCreateDialog(int id) {

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	switch(id) {
        case DIALOG_CHOOSE_SPEECH_RECO_RESULT:
        	CharSequence[] items = speech_matches.toArray(new CharSequence[speech_matches.size()]);

        	builder.setTitle("Did you mean...");
        	builder.setItems(items, new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog, int item) {
                	publish(speech_matches.get(item).toString());
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
    	}
        return null;
    }

    /**
     * Handle the click on the start recognition button.
     */
    public void onClick(View v) {
        if (v.getId() == R.id.btn_speak) {
            startVoiceRecognitionActivity();
            return;
        }
        
        if (v.getId() == R.id.btn_settings) {
        	// When the button is clicked, launch an activity through this intent
            Intent launchPreferencesIntent = new Intent().setClass(this, Preferences.class);
            // Make it a subactivity so we know when it returns
            startActivityForResult(launchPreferencesIntent, PREFERENCES_REQUEST_CODE);
            
        	return;
        }

        
        if (v.getId() == R.id.btn_hello_robot) {
        	publish("Hello robot!");
        }
        
        if (v.getId() == R.id.btn_forget_it) {
        	publish("forget it");
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
                	
                	publish(msg);
                    edittext.setText(null);
                    return true;
            }
        }

        return false;
    }
    
    private void publish(String msg) {
    	connectionManager.send(msg);
   	
    	transcriptAdapter.add(msg);
    }
    
    public void asyncAddToTranscript(String msg) {
    	transcriptAdapter.asyncAdd(msg);
    	//transcript.smoothScrollToPosition(transcriptAdapter.getCount());
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
        
        if (requestCode == PREFERENCES_REQUEST_CODE) {
        	connectionManager.setRecipient();
        }
        
    }

	
	@Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }

        super.onDestroy();
    }
	
	public void say(String text) {
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enabletts", true)) {
			mTts.speak(text,
	                TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
	                null);
		}
	}
	
	// Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.UK);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e(LOG_TTS, "Language is not available.");
            } else {

                // The TTS engine has been successfully initialized.
                // Greet the user.
            	say("Welcome!");
            	
            }
        } else {
            // Initialization failed.
            Log.e(LOG_TTS, "Could not initialize TextToSpeech.");
        }
    }

        
}
