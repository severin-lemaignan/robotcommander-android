/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package laas.openrobots.robotcommander;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Example that shows finding a preference from the hierarchy and a custom preference type.
 */
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    public static final String KEY_DISTANT_ACCOUNT = "custom_account";
    public static final String KEY_SELECTED_ROBOT = "robot_list";

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the XML preferences file
        addPreferencesFromResource(R.xml.robotcommander_preferences);

    }
    
    @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something when my counter preference value changes
    	if (key.equals(KEY_SELECTED_ROBOT)) {
    		String robot = sharedPreferences.getString(key, "");
            if (!robot.equals("custom")) {
            	finish();
            }
        }
        if (key.equals(KEY_DISTANT_ACCOUNT)) {
        	//If the user modify the custom account, automatically set the
        	// 'robot' type to custom.
        	SharedPreferences.Editor editor = sharedPreferences.edit();
        	editor.putString(KEY_SELECTED_ROBOT, "custom");
        	editor.commit();
        	
        	finish();
        }
    }
    
}
