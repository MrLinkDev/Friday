package ru.linkstuff.friday.Executors;

import java.io.IOException;

/**
 * Created by alexander on 20.08.17.
 */

public class Power {
    public static void reboot(){
        try {
            Runtime.getRuntime().exec(new String[] { "su", "-c", "reboot" });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void shutdown(){
        try {
            Runtime.getRuntime().exec(new String[]{ "su", "-c", "reboot -p" });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
