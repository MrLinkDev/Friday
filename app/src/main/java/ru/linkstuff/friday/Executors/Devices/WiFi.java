package ru.linkstuff.friday.Executors.Devices;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;

import ru.linkstuff.friday.Splitter.Query;

/**
 * Created by alexander on 20.08.17.
 */

public class WiFi {
    public static void switchState(Activity activity, Query query){
        WifiManager manager = (WifiManager)activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean state = false;

        if (query.getCommand().equals("включ")) state = true;

        manager.setWifiEnabled(state);
    }

}
