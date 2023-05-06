package ru.linkstuff.friday.HelperClasses;

import android.app.Activity;

import ru.linkstuff.friday.Executors.AppRouter;
import ru.linkstuff.friday.Executors.Brightness;
import ru.linkstuff.friday.Executors.Call;
import ru.linkstuff.friday.Executors.DeviceRouter;
import ru.linkstuff.friday.Executors.Power;
import ru.linkstuff.friday.Executors.Security;
import ru.linkstuff.friday.Splitter.Query;

/**
 * Created by alexander on 10.09.17.
 */

public class Corridor {

    public static void findRoom(Activity activity, Query query){
        switch (query.getCommand()){
            case "APP":
                if (query.getArg() != null){
                    AppRouter.routeApp(activity, query);
                }
                break;
            case "SEARCH":

                break;
            case "яркост":
                Brightness.setBrightness(activity, query);
                break;
            case "заблокир":
                Security.lock(activity.getApplicationContext());
                break;
            case "включ":
                DeviceRouter.routeDevice(activity, query);
                break;
            case "выключ":
                if (query.getArg() != null) DeviceRouter.routeDevice(activity, query);
                else Power.shutdown();
                break;
            case "перезагруз":
                Power.reboot();
                break;
            case "позвон":
                Call.call(activity, query);
                break;

        }

    }

}
