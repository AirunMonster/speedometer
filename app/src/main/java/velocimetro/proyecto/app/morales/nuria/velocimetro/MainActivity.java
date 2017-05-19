package velocimetro.proyecto.app.morales.nuria.velocimetro;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.location.LocationProvider;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    AutoResizeTextView indicadorVelocidad;
    LinearLayout linearLayout;
    LocationManager locationManager;
    MiLocationListener locationListener = null;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        indicadorVelocidad = (AutoResizeTextView) findViewById(R.id.indicadorVelocidad);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        }
        else {
            //Toast.makeText(MainActivity.this, "Iniciando.....", Toast.LENGTH_SHORT).show();
            locationStart();
        }

//        Intent intent = getIntent();
//        Bundle bundle = intent.getExtras();
//        Toast.makeText(this, "Va a entrar en bundle", Toast.LENGTH_SHORT).show();
//        if (bundle != null) {
//            Toast.makeText(this, "Entra en bundle", Toast.LENGTH_SHORT).show();
//            String cargaOnOFF = (String) bundle.get("CARGA");
//            Toast.makeText(this, "cargaOnOFF: "+cargaOnOFF, Toast.LENGTH_SHORT).show();
//            if (cargaOnOFF =="charging"){
//                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                Toast.makeText(this, "La pantalla no se apagará - MainActivity", Toast.LENGTH_SHORT).show();
//            }
//            else if (cargaOnOFF =="not charging"){
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                Toast.makeText(this, "La pantalla se apagará - MainActivity", Toast.LENGTH_SHORT).show();
//            }
//        }


//        PowerConnectionReceiver power = new PowerConnectionReceiver();
//        power.onReceive(this,intent);
//        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//        Toast.makeText(this, "MainActivity - Status "+status, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "MainActivity - Battery Status "+BatteryManager.EXTRA_STATUS, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "MainActivity - Battery Status "+BatteryManager.BATTERY_STATUS_CHARGING, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "MainActivity - Battery Status "+BatteryManager.BATTERY_STATUS_FULL, Toast.LENGTH_SHORT).show();
//        if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            Toast.makeText(this, "La pantalla no se apagará - MainActivity", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            Toast.makeText(this, "La pantalla se apagará - MainActivity", Toast.LENGTH_SHORT).show();
//        }
        if (isPlugged(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Toast.makeText(this, "La pantalla se apagará - MainActivity", Toast.LENGTH_SHORT).show();
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Toast.makeText(this, "La pantalla no se apagará - MainActivity", Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean isPlugged(Context context) {
        boolean isPlugged= false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        return isPlugged;
    }

    private void locationStart() {
        //Toast.makeText(MainActivity.this, "locationStart()....", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MiLocationListener();
        //locationListener.setMainActivity(this);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Toast.makeText(MainActivity.this, "Gps no está activado...", Toast.LENGTH_SHORT).show();
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(MainActivity.this, "No hay permisos. Se piden de nuevo...", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        //mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) locationListener);
        //Toast.makeText(MainActivity.this, "Llamamos a requestLocationUpdates...", Toast.LENGTH_LONG).show();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //Toast.makeText(MainActivity.this, "Llamamos a onRequestPermissionsResult...", Toast.LENGTH_SHORT).show();
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(MainActivity.this, "onRequestPermissionsResult - locationStart()...", Toast.LENGTH_SHORT).show();
                locationStart();
                //return;
            }
        }
    }

    public void ComprobarVelocidad (double velocidad) {
        // Si la velocidad pasa de los 150 Km/h el texto cambia a blanco y el background se vuelve rojo
        if (velocidad >= 150){
            indicadorVelocidad.setTextColor(Color.WHITE);
            linearLayout.setBackgroundColor(Color.RED);
        } else {
            linearLayout.setBackgroundColor(Color.WHITE);
            // Si la velocidad supera los 120 KM/h el texto cambia a rojo
            if (velocidad >= 120) {
                indicadorVelocidad.setTextColor(Color.RED);
            } else {
                // Si la velocidad supera los 80 Km/h el texto cambia a verde oscuro
                if (velocidad >= 80) {
                    indicadorVelocidad.setTextColor(Color.GREEN);
                } else {
                    // Si la velocidad supera los 30 Km/h el texto cambia a azul oscuro
                    if (velocidad >= 30) {
                        indicadorVelocidad.setTextColor(Color.BLUE);
                    } else {
                        indicadorVelocidad.setTextColor(Color.BLACK);
                    }
                }
            }
        }
    }

    private class MiLocationListener implements LocationListener {
        // Este método se llama cuando la posición GPS cambia
        @Override
        public void onLocationChanged(Location location) {
            //Toast.makeText(MainActivity.this, "onLocationChanged.....", Toast.LENGTH_SHORT).show();
            float vel = location.getSpeed();
            double vel2 = (((double) vel * 60) * 60) / 1000;
            vel2 = (Math.round(vel2*100d))/100d;
            //Toast.makeText(MainActivity.this, "Velocidad: "+String.valueOf(vel2), Toast.LENGTH_SHORT).show();
            indicadorVelocidad.setText(String.valueOf(vel2));
            ComprobarVelocidad (vel2);
        }

        //Este método se llama cuando el estado del manejador ha cambiado.
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            switch (i) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    //Toast.makeText(MainActivity.this, "LocationProvider.AVAILABLE", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    //Toast.makeText(MainActivity.this, "LocationProvider.OUT_OF_SERVICE", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    //Toast.makeText(MainActivity.this, "LocationProvider.TEMPORARILY_UNAVAILABLE", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        //Este método se llama cuando el usuario ha activado el GPS.
        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(MainActivity.this, "GPS activado.", Toast.LENGTH_SHORT).show();
        }

        //Este método se llama cuando el usuario desactive el GPS.
        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(MainActivity.this, "GPS desactivado. Por favor activa el GPS.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Le indicamos que ya no tiene que estar la pantalla permanentemente encendida.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Hay que liberar el buffer que se ha llenado con el Listener.
        //Toast.makeText(MainActivity.this, "Finalizando.....", Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(locationListener);
    }
}
