package laas.openrobots.robotcommander;

import com.example.android.imagedownloader.ImageDownloader;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class ImageDialog extends Activity {

    private ImageView mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagedialoglayout);

        ImageDownloader imagedownloader = new ImageDownloader();

        mDialog = (ImageView)findViewById(R.id.your_image);
        imagedownloader.download("http://pr2c2.laas.fr/data/toto2.jpg", mDialog);
        mDialog.setClickable(true);


        //finish the activity (dismiss the image dialog) if the user clicks 
        //anywhere on the image
        mDialog.setOnTouchListener(new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
            finish();
			return true;
		}
        });

    }
}