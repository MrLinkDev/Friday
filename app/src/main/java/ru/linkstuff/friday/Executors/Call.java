package ru.linkstuff.friday.Executors;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.ActivityCompat;


import ru.linkstuff.friday.Splitter.Query;

import static android.Manifest.permission.CALL_PHONE;

/**
 * Created by alexander on 20.08.17.
 */

public class Call {
    public static void call(Activity activity, Query query) {

        if (query.getArg() != null) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("tel:" + query.getArg()));
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    activity.requestPermissions(new String[]{CALL_PHONE}, 1);
                }
            }
            activity.getApplicationContext().startActivity(intent);
        }

    }

}
