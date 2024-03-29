package ch.mobile_dev.autopowersave;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;

/**
 * Created by florian on 19/10/15.
 */
public class PowerUtils {
	public static boolean disablePowersave(Context context) {
		try {
			return Settings.Global.putInt(context.getContentResolver(), "low_power", 0);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean enablePowersave(Context context) {
		try {
			return Settings.Global.putInt(context.getContentResolver(), "low_power", 1);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean grantSuperuserPermission(Context context) {
		try {
			Runtime.getRuntime().exec("su -c pm grant ch.mobile_dev.autopowersave android.permission.WRITE_SECURE_SETTINGS");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isBatterySaver(Context context) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && powerManager.isPowerSaveMode());
	}

	public static boolean isCharging(Context context) {
		Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
	}
}
