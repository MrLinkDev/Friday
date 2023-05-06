package ru.linkstuff.friday.Executors;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

import ru.linkstuff.friday.Splitter.Query;

public class Brightness{

    public static void setBrightness(Activity activity, Query query){

        if (query.getArgOfChange() != null){
            ContentResolver resolver = activity.getContentResolver();
            float temp = 0;
            if (query.getArg() != null){
                temp = 255f / 100 * Integer.parseInt(query.getArg());
                Log.w("2", String.valueOf(temp));
            } else {
                temp = 255f / 100 * 10;
                Log.w("1", String.valueOf(temp));
            }
            int set = Math.round(temp);
            int current = 255;

            if (query.getExtraArg() != null && query.getExtraArg().equals("до")){
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, set);
            } else {
                try {
                    current = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
                    Log.w("3", String.valueOf(current));
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }

                switch (query.getArgOfChange()){
                    case "постав":
                        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, set);
                        break;
                    case "убав":
                        if (current - set < 0) Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 0);
                        else Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, current - set);
                        break;
                    case "прибав":
                        if (current + set > 255) Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 255);
                        else Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, current + set);
                        break;
                    case "добав":
                        if (current + set > 255) Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 255);
                        else Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, current + set);
                        break;
                }
            }
        }

    }

}
