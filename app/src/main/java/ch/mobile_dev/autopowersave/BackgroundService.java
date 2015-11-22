package ch.mobile_dev.autopowersave;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by florian on 19/10/15.
 */
public class BackgroundService extends Service {
	public class MyBroadCastReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "OnReceive");
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i(TAG, "Screen went OFF: " + PowerUtils.enablePowersave(context));
			} else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				Log.i(TAG, "Screen went ON: " + PowerUtils.disablePowersave(context));
			}
		}
	}
	private MyBroadCastReciever m_screenStateReceiver = new MyBroadCastReciever();

	public class MyBinder extends Binder {
		public BackgroundService getService() {
			return BackgroundService.this;
		}
	}

	private static String TAG = "AutoPowersave";
	private MyBinder m_binder = new MyBinder();
	private static int NOTIFICATION_ID = 58163;
	NotificationManager m_notificationManager;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO for communication return IBinder implementation
		Log.d(TAG, "Bound service");
		m_notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		IntentFilter screenStateFilter = new IntentFilter();
		screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenStateFilter.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(m_screenStateReceiver, screenStateFilter);
		addNotification();
		return m_binder;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(m_screenStateReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void addNotification() {
		// create the notification
		Notification.Builder m_notificationBuilder = new Notification.Builder(this)
			.setContentTitle(getText(R.string.service_name))
			.setContentText(getResources().getText(R.string.service_text))
			.setSmallIcon(android.R.drawable.sym_def_app_icon)
			.setPriority(Notification.PRIORITY_MIN);

		// create the pending intent and add to the notification
		Intent intent = new Intent(this, BackgroundService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		m_notificationBuilder.setContentIntent(pendingIntent);

		// send the notification
//		m_notificationManager.notify(NOTIFICATION_ID, m_notificationBuilder.build());
		startForeground(NOTIFICATION_ID, m_notificationBuilder.build());
	}
}
