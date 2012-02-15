package laas.openrobots.robotcommander;

import android.content.Context;
import android.os.Handler;
import android.widget.ArrayAdapter;


public class TranscriptAdapter extends ArrayAdapter<String> {
	
	public TranscriptAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	final Handler mHandler = new Handler();

	void asyncAdd(final String item) {
        // Enqueue work on mHandler to change the data on
        // the main thread.
        mHandler.post(new Runnable() {
                @Override
                public void run() {
                    add(item);
                }
            });
    }
    

}
