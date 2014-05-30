package at.wodni.android.webmote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.renderscript.Script.KernelID;
import android.util.Log;
import android.widget.Toast;

public class WebService extends Service {
	
	public static final String LOG_PREFIX = "WebMote";
	public static final String ACTION_STOP = "STOP";
	
	private Thread mWebServerThread;
	private NotificationManager mNotificationManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(WebService.LOG_PREFIX, "WebService.Create" );
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String action = intent.getAction();
		if( action == null )
			action = "";
		
		/* stop service */
		if( action.equals(ACTION_STOP) ) {
			Log.d(WebService.LOG_PREFIX, "WebService.onStartCommand - Stop" );
			stopSelf();
			return START_NOT_STICKY;
		}
		/* start service */
		else {
			mWebServerThread = new Thread( new WebServer(this) );
			mWebServerThread.start();
			Toast.makeText(this, "WebMote Started...", Toast.LENGTH_SHORT).show();;
			Log.d(WebService.LOG_PREFIX, "WebService.OnStartCommand - Start" );
			return START_STICKY;
		}
	}
	
	private void stop() {
		Log.d(WebService.LOG_PREFIX, "WebService - stop" );
		mNotificationManager.cancelAll();
		if(mWebServerThread != null)
		{
			mWebServerThread.interrupt();
			try {
				mWebServerThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			mWebServerThread = null;
		}
	}
	
	@Override
	public void onDestroy() {
		Log.d(WebService.LOG_PREFIX, "WebService - destroy" );
		stop();		
		super.onDestroy();
	}
	
	private void showNotification() {
		
		/* set intent to stop activity */
		Intent intent = new Intent(this, WebService.class);
		intent.setAction(ACTION_STOP);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
		
		Notification notification = new Notification.Builder(this)
		.setContentTitle("WebMote")
		.setContentText("Listening on Port " + String.valueOf(WebServer.PORT) + "\nTouch to stop")
		.setContentIntent(pendingIntent)
		.setSmallIcon(R.drawable.ic_launcher)
		.build();
		
		mNotificationManager.notify( R.string.app_name, notification );
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
