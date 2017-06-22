package velocimetro.proyecto.app.morales.nuria.velocimetro;

import android.Manifest;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.location.LocationProvider;
import android.net.sip.SipAudioCall;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static velocimetro.proyecto.app.morales.nuria.velocimetro.Helper.isAppRunning;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    AutoResizeTextView indicadorVelocidad;
    LinearLayout linearLayout;
    LocationManager locationManager;
    MiLocationListener locationListener = null;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    Integer battery_on = 0; //Values: 0 - Initial value; 1 - Battery On; 2 - Battery Off.
    PlugInControlReceiver mReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Navigation Drawer ----------------------------------------------------------------------------->
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Navigation Drawer -------------------------------------------------------------------------------------------->

        indicadorVelocidad = (AutoResizeTextView) findViewById(R.id.indicadorVelocidad);
        linearLayout = (LinearLayout) findViewById(R.id.content_main);

        //Battery Receiver --------------------------------------------------------------------------------------------->
        mReceiver = new PlugInControlReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        Toast.makeText(this, "registerReceiver", Toast.LENGTH_LONG).show();
        registerReceiver(mReceiver,filter);
        //Battery Receiver Fin ----------------------------------------------------------------------------------------->

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras(); //Creación del Bundle

        if (bundle != null) {
            String texto = (String) bundle.get("DATOS");
            Toast.makeText(this, "texto: "+texto, Toast.LENGTH_LONG).show();
            if (texto != null) {
                if (texto.equals("conectada")) {
                    Toast.makeText(this, "conectada", Toast.LENGTH_LONG).show();
                    battery_on = 1;
                } else if (texto.equals("desconectada")) {
                    battery_on = 2;
                    Toast.makeText(this, "desconectada", Toast.LENGTH_LONG).show();
                }
            }
        }

        // Check the User Preferences ---------------------------------------------------------------------------------->
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                String auxString = sharedPreferences.toString();
                Toast.makeText(getApplicationContext(), "auxString: "+auxString, Toast.LENGTH_LONG).show();
                Boolean in_out = true;
                if ((s.equals("switch_keep_screen_on")) && in_out) {
                    in_out = false;
                    Toast.makeText(getApplicationContext(), "Preferences Changed", Toast.LENGTH_LONG).show();
                    Boolean screen_on2 = sharedPreferences.getBoolean("switch_keep_screen_on", true);
                    Toast.makeText(getApplicationContext(), "isScreenOn 2", Toast.LENGTH_LONG).show();
                    isScreenOn(screen_on2);
                }
            }
        };
        final Boolean screen_on = sharedPreferences.getBoolean("switch_keep_screen_on", true);
        Toast.makeText(this,"screen_on: "+screen_on.toString(),Toast.LENGTH_LONG).show();
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        // User Preferences -------------------------------------------------------------------------------------------->
        Toast.makeText(getApplicationContext(), "isScreenOn", Toast.LENGTH_LONG).show();
        isScreenOn(screen_on);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        }
        else {
            locationStart();
        }
    }

    // User Preferences screen-On is the main option over isPlugged. isPlugged has used only if screen-on is false ------->
    public void isScreenOn (Boolean screen_on){
        if (screen_on) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Toast.makeText(this,"La pantalla permanecerá encendida.",Toast.LENGTH_LONG).show();
        }
        else {
            switch (battery_on){
                case 0:
                case 2:
                    Toast.makeText(this, "Case: "+battery_on.toString(), Toast.LENGTH_LONG).show();
                    if (isPlugged(this)) {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        Toast.makeText(this, "La pantalla permanecerá encendida. Cargador Puesto.", Toast.LENGTH_LONG).show();
                    } else {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        Toast.makeText(this, "La pantalla se apagará.", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 1:
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    Toast.makeText(this, "La pantalla permanecerá encendida. Batería cargando.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(this, "Hay un error.", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    // Check if battery is Plugged --------------------------------------------------------------------------------------->
    public static boolean isPlugged(Context context) {
        boolean isPlugged = false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        int plugged = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) : 0;
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        return isPlugged;
    }

    private class PlugInControlReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //if (isAppRunning(context,"velocimetro.proyecto.app.morales.nuria.velocimetro"))
            Toast.makeText(context, "PlugInControlReceiver", Toast.LENGTH_LONG).show();
            String action = intent.getAction();
            Intent intentPan = new Intent(context, MainActivity.class);
            intentPan.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //intentPan.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            String datos = "";

            if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                Toast.makeText(context, "Bateria conectada", Toast.LENGTH_LONG).show();
                datos = "conectada";
                intentPan.putExtra("DATOS", datos);
                context.startActivity(intentPan);
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                Toast.makeText(context, "Bateria desconectada", Toast.LENGTH_LONG).show();
                datos = "desconectada";
                intentPan.putExtra("DATOS", datos);
                context.startActivity(intentPan);
            }
        }
    }

    private void locationStart() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MiLocationListener();
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Toast.makeText(MainActivity.this, R.string.gps_turn_off+"...", Toast.LENGTH_SHORT).show();
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        //mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
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

    //Navigation Drawer ---------------------------------------------------------->
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // New Screen Settings
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    //Navigation Drawer ---------------------------------------------------------->

    private class MiLocationListener implements LocationListener {
        // Este método se llama cuando la posición GPS cambia
        @Override
        public void onLocationChanged(Location location) {
            float vel = location.getSpeed();
            double vel2 = (((double) vel * 60) * 60) / 1000;
            vel2 = (Math.round(vel2*100d))/100d;
            indicadorVelocidad.setText(String.valueOf(vel2));
            ComprobarVelocidad (vel2);
        }

        //Este método se llama cuando el estado del manejador ha cambiado.
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            switch (i) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        //Este método se llama cuando el usuario ha activado el GPS.
        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(MainActivity.this, R.string.gps_turn_on, Toast.LENGTH_SHORT).show();
        }

        //Este método se llama cuando el usuario desactive el GPS.
        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(MainActivity.this, R.string.gps_turn_off_please, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        Toast.makeText(MainActivity.this, "onStart", Toast.LENGTH_SHORT).show();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mReceiver,filter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        Toast.makeText(MainActivity.this, "onStop", Toast.LENGTH_SHORT).show();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(MainActivity.this, "onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        // Le indicamos que ya no tiene que estar la pantalla permanentemente encendida.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Hay que liberar el buffer que se ha llenado con el Listener.
        //Toast.makeText(MainActivity.this, "Finalizando.....", Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(locationListener);
        //Free the SharedPreferenes listener. If we can't do it then repeat the same many times.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
