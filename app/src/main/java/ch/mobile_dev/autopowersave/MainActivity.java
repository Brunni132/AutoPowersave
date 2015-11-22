package ch.mobile_dev.autopowersave;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity {
	private static String TAG = "AutoPowersave";
	private BackgroundService m_service;
	private ServiceConnection m_serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_service = ((BackgroundService.MyBinder)service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			m_service = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.btn_enable_ps).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!PowerUtils.enablePowersave(MainActivity.this)) {
					alertPermission();
				} else {
					checkNotCharging();
				}
			}
		});
		findViewById(R.id.btn_disable_ps).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!PowerUtils.disablePowersave(MainActivity.this)) {
					alertPermission();
				} else {
					checkNotCharging();
				}
			}
		});
		((CheckBox) findViewById(R.id.chk_enable_service)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					startService();
				} else {
					stopService();
				}
			}
		});

		startService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void alertPermission() {
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
		dlgAlert.setMessage("This app does not have the permission to modify the low power system setting. There are two workarounds:\n\n1) Without rooting: plug your device to your PC in debug mode and run the following from the command line:\nadb shell pm grant ch.mobile_dev.autopowersave android.permission.WRITE_SECURE_SETTINGS\n\n2) Automatically grant the permission to this application (requires root privileges) by clicking the button below.");
		dlgAlert.setTitle("AutoPowersave");
		dlgAlert.setNegativeButton("Cancel", null);
		dlgAlert.setPositiveButton("Grant permission (root only)", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				PowerUtils.grantSuperuserPermission(MainActivity.this);
			}
		});
		dlgAlert.setCancelable(false);
		dlgAlert.create().show();
	}

	private void checkNotCharging() {
		if (PowerUtils.isCharging(this)) {
			AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
			dlgAlert.setMessage("Battery saver mode can not be enabled while your device is charging. Please plug the phone out before trying again.");
			dlgAlert.setTitle("AutoPowersave");
			dlgAlert.setPositiveButton("OK", null);
			dlgAlert.setCancelable(true);
			dlgAlert.create().show();
		}
	}

	private void startService() {
		Log.d(TAG, "Attempting to start service");
		Intent intent = new Intent(this, BackgroundService.class);
		startService(intent);
		bindService(intent, m_serviceConnection, BIND_AUTO_CREATE);
	}

	private void stopService() {
		Log.d(TAG, "Stopping service");
		stopService(new Intent(this, BackgroundService.class));
		unbindService(m_serviceConnection);
	}
}
