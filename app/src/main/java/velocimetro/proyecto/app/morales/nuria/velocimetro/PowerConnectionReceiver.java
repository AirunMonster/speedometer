package velocimetro.proyecto.app.morales.nuria.velocimetro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by Android on 18/05/2017.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        Toast.makeText(context, "lalalala", Toast.LENGTH_LONG).show();
//        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//        //boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
//        //        status == BatteryManager.BATTERY_STATUS_FULL;
//        Toast.makeText(context, "lalala - Status "+status, Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "lalala - Battery Status "+BatteryManager.EXTRA_STATUS, Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "lalala - Battery Status "+BatteryManager.BATTERY_STATUS_CHARGING, Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "lalala - Battery Status "+BatteryManager.BATTERY_STATUS_FULL, Toast.LENGTH_SHORT).show();
//        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
//            //La pantalla no se apagará mientras el layout esté activo.
//            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            Toast.makeText(context, "La pantalla no se apagará", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            Toast.makeText(context, "La pantalla se apagará", Toast.LENGTH_SHORT).show();
//        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);
        //Intent extras = new Intent(context,MainActivity.class);

        int chargeState = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String strState;

        switch (chargeState) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
            case BatteryManager.BATTERY_STATUS_FULL:
                strState = "charging";
                //intent.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
            default:
                //intent.setFlags(~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                strState = "not charging";
                break;
        }
        //Toast.makeText(context, "Cargando?: "+strState, Toast.LENGTH_SHORT).show();

        //intent.putExtra("CARGA",strState);
        //Toast.makeText(context, "Intent: "+intent, Toast.LENGTH_LONG).show();
    }
}
