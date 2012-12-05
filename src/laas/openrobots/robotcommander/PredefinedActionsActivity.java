package laas.openrobots.robotcommander;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PredefinedActionsActivity extends Activity implements OnClickListener {
	
	private Intent resultIntent;
	private SharedPreferences sharedPref;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sharedPref = RobotCommanderActivity.prefs;
		
	    setContentView(R.layout.predefinedactions);
	
	    Button predef1Button = (Button) findViewById(R.id.btn_predef_action_1);
	    predef1Button.setText(sharedPref.getString("predef1", "(not defined)"));
        predef1Button.setOnClickListener(this);
        
        Button predef2Button = (Button) findViewById(R.id.btn_predef_action_2);
        predef2Button.setText(sharedPref.getString("predef2", "(not defined)"));
        predef2Button.setOnClickListener(this);
        
        Button predef3Button = (Button) findViewById(R.id.btn_predef_action_3);
        predef3Button.setText(sharedPref.getString("predef3", "(not defined)"));
        predef3Button.setOnClickListener(this);
        
        Button predef4Button = (Button) findViewById(R.id.btn_predef_action_4);
        predef4Button.setText(sharedPref.getString("predef4", "(not defined)"));
        predef4Button.setOnClickListener(this);
        
        Button predef5Button = (Button) findViewById(R.id.btn_predef_action_5);
        predef5Button.setText(sharedPref.getString("predef5", "(not defined)"));
        predef5Button.setOnClickListener(this);
        
        Button predef6Button = (Button) findViewById(R.id.btn_predef_action_6);
        predef6Button.setText(sharedPref.getString("predef6", "(not defined)"));
        predef6Button.setOnClickListener(this);
}

	@Override
	public void onClick(View v) {
		String text = "";
        if (v.getId() == R.id.btn_predef_action_1) {
        	text = sharedPref.getString("predef1", "");
        }
        if (v.getId() == R.id.btn_predef_action_2) {
        	text = sharedPref.getString("predef2", "");
        }
        if (v.getId() == R.id.btn_predef_action_3) {
        	text = sharedPref.getString("predef3", "");
        }
        if (v.getId() == R.id.btn_predef_action_4) {
        	text = sharedPref.getString("predef4", "");
        }
        if (v.getId() == R.id.btn_predef_action_5) {
        	text = sharedPref.getString("predef5", "");
        }
        if (v.getId() == R.id.btn_predef_action_6) {
        	text = sharedPref.getString("predef6", "");
        }		
        resultIntent = new Intent();
        resultIntent.putExtra("text", text);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
	}
}
