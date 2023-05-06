package ru.linkstuff.friday.Executors;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import javax.crypto.spec.DESedeKeySpec;

import ru.linkstuff.friday.Accomplices.Albert;
import ru.linkstuff.friday.Executors.Apps.Changelog;
import ru.linkstuff.friday.Executors.Apps.Screenshot;
import ru.linkstuff.friday.HelperClasses.DictionarySchema;
import ru.linkstuff.friday.HelperClasses.FridayDatabase;
import ru.linkstuff.friday.R;
import ru.linkstuff.friday.Splitter.Query;

/**
 * Created by alexander on 20.08.17.
 */

public class AppRouter {
    private static final String DEFAULT_TAG = "this_";

    public static void routeApp(Activity activity, Query query){
        if (query.getArg().contains(DEFAULT_TAG)){
            switch (query.getArg()){
                case DEFAULT_TAG + "браузер":

                    break;
                case DEFAULT_TAG + "изменений":
                    Intent changelog = new Intent(activity, Changelog.class);
                    changelog.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                    activity.startActivity(changelog);
                    break;
                case DEFAULT_TAG + "скриншот":
                    Screenshot.takeScreenshot(activity);
                    break;
                case DEFAULT_TAG + "приложение":
                    Intent albert = new Intent(activity, Albert.class);
                    albert.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                    activity.startActivity(albert);
                    break;

            }
        } else {
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage(query.getArg());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } else Toast.makeText(activity, activity.getResources().getString(R.string.app_router_null_message), Toast.LENGTH_SHORT).show();
        }
    }

}
