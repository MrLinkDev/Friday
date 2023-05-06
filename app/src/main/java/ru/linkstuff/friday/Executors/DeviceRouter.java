package ru.linkstuff.friday.Executors;

import android.app.Activity;

import ru.linkstuff.friday.Executors.Devices.WiFi;
import ru.linkstuff.friday.Splitter.Query;

/**
 * Created by alexander on 20.08.17.
 */

public class DeviceRouter {
    public static void routeDevice(Activity activity, Query query){

        switch (query.getArg()){

            case "0":
                WiFi.switchState(activity, query);
                break;

        }

    }

}
