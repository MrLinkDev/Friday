package ru.linkstuff.friday.Executors.Apps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.linkstuff.friday.FirstStart;
import ru.linkstuff.friday.Friday;
import ru.linkstuff.friday.R;

/**
 * Created by alexander on 29.08.17.
 */

public class Screenshot {

    public static void takeScreenshot(final Activity activity){
        if (Friday.fridayMainLayout != null){
            Friday.fridayMainLayout.setAlpha(0f);
        }
        take(activity, activity.getApplicationContext());

    }

    private static void take(Activity activity, Context context){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_hhmmss");
        Date date = new Date();
        String time = simpleDateFormat.format(date);
        String path = Environment.getExternalStorageDirectory().toString() + "/" + time + "_FShot.png";
        File screenshot = new File(path);

        try {
            Process sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream outputStream = sh.getOutputStream();
            outputStream.write(("/system/bin/screencap -p " + "/sdcard/" + time + "_FShot.png").getBytes("ASCII"));
            outputStream.flush();
            outputStream.close();
            sh.waitFor();
        } catch (IOException io){
            Intent intent = new Intent(context, FirstStart.class);
            activity.startActivity(intent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        openScreenshot(context, activity, screenshot);
    }

    private static void openScreenshot(Context context, Activity activity, File screenshot){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme));
        Uri uri = Uri.fromFile(screenshot);

        ImageView imageView = new ImageView(context);
        imageView.setImageURI(uri);
        builder.setView(imageView);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 1500);
    }

}
