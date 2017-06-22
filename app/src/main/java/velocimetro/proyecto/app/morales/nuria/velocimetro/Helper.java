package velocimetro.proyecto.app.morales.nuria.velocimetro;

import android.app.ActivityManager;
import android.content.Context;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Android on 21/06/2017.
 */

public class Helper {
    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    Toast.makeText(context, "Helper app: "+processInfo.processName, Toast.LENGTH_LONG).show();
                    return true;
                }
            }
        }
        return false;
    }
}
