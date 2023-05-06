package ru.linkstuff.friday.Executors;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.PowerManager;
import android.view.WindowManager;

import ru.linkstuff.friday.HelperClasses.MyAdmin;

/**
 * Created by alexander on 20.08.17.
 */

public class Security {
    public static void lock(Context context){
        DevicePolicyManager manager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final ComponentName componentName = new ComponentName(context, MyAdmin.class);

        if (manager.isAdminActive(componentName)){
            manager.lockNow();
        }
    }

    public static void unlock (Activity activity, Context context){
        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("FridayLock");
        keyguardLock.disableKeyguard();

        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "FridayLock");
        if (!wakeLock.isHeld() && wakeLock != null){
            wakeLock.acquire();
        }
    }


}
