package me.neversleep.plusplus;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.N)
public class ADBService extends TileService {
    public static final String TAG = "QurkStartService";
    private SharedPreferences xConf;

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        try {
            this.xConf = getSharedPreferences("x_conf", 1);
            Log.e(TAG, "onCreate: xConf" + this.xConf);
        } catch (SecurityException e) {
            Toast.makeText(this, "error: " + e.getMessage(), 1).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean power = intent.getIntExtra("power", 1) == 1;
        boolean sleep = intent.getIntExtra("sleep", 0) == 1;
        Log.e(TAG, "power:" + sleep + ", screen" + power);

        if (this.xConf == null) {
            return START_STICKY;
        }

        this.xConf.edit().putBoolean("power", power).apply();
        this.xConf.edit().putBoolean("disable_sleep", sleep).apply();
        return START_STICKY;
    }

}
