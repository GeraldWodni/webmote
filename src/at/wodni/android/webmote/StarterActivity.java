package at.wodni.android.webmote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/* only purpose of this activity is to start the WebService */
public class StarterActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(WebService.LOG_PREFIX, "Starting Service " + getIntent().getAction());
		
		Intent intent = new Intent(this, WebService.class);
		startService(intent);
		
		Log.i(WebService.LOG_PREFIX, "Finishing Starter");
		
		finish();
	}
}
