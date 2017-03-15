package com.example.mimos.simpleweatherapp;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnTaskCompleted {
    private Typeface weatherFont;

    private TextView cityField;
    private TextView dateField;
    private TextView weatherField;
    private TextView humidityField;
    private TextView pressureField;
    private TextView currentTemperatureField;
    private TextView weatherIcon;

    private FloatingActionButton fab;

    private PermissionManager PM;
    private LocationManager locationManager;

    private GestureDetectorCompat gestureDetectorCompat;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        PM = new PermissionManager(this);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
        cityField = (TextView) findViewById(R.id.city_field);
        dateField = (TextView) findViewById(R.id.date_field);
        weatherField = (TextView) findViewById(R.id.weather_field);
        humidityField = (TextView) findViewById(R.id.humidity_field);
        pressureField = (TextView) findViewById(R.id.pressure_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView)findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        //runtime permission for android 6.0
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PM.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) || !PM.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("SimpleWeatherAPP", "Requesting permission for Android 6.0");
                PM.reqPermissions();
            }
        }

        OnlineService task = new OnlineService(MainActivity.this);    //asynctask class to fetch data

        //check extra intent parameters
        String lat = getIntent().getStringExtra("latitude");
        String lon = getIntent().getStringExtra("longitude");

        if (lat != null && lon != null && !lat.equals(null) && !lon.equals(null)) {
            task.execute(lat, lon);        //execute the asynctask process to fetch data
        } else {
            try {
                //check permission for location access
                if (PM.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || PM.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Locationlistener which response to locationmanager
                    LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    };
                    locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
                    // getting GPS status
                    boolean isGPSEnabled = locationManager
                            .isProviderEnabled(LocationManager.GPS_PROVIDER);

                    // getting network status
                    boolean isNetworkEnabled = locationManager
                            .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    Location location = null;
                    // check GPS and Network status
                    if (!isGPSEnabled && !isNetworkEnabled) {
                        Toast.makeText(this, "Please turn on your gps and internet and restart the app", Toast.LENGTH_LONG).show();
                    } else {
                        if (isGPSEnabled) {
                            //update knownlocation
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 2, locationListener);
                            if (locationManager != null) {
                                //get location based on last knownlocation
                                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            }
                        }

                        if (isNetworkEnabled) {
                            //update knownlocation
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 2, locationListener);
                            if (locationManager != null) {
                                //get location based on last knownlocation
                                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            }
                        }

                        if (location != null) {
                            //execute the asynctask process to fetch data based on current latitude and longitude
                            task.execute(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //swipe detector
        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());

        //initialize floating action button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openautocompleteactivity by googleplacepi
                openAutocompleteActivity();
            }
        });
    }

    /**
     * Called after the request permission process has finished to return its result.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for(int i : grantResults) {
            Log.d("SimpleWeatherAPP", "Result: " + i);
        }
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        refreshActivity();
                    } else {
                        Toast.makeText(this, "Please allow this app to access your location", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    /**
     * Called after the asyntask process has finished to return its result.
     */
    @Override
    public void onTaskCompleted(String result){
        Log.d("SimpleWeatherAPP", "result : " + result);
        try {
            //convert result to JSONObj
            JSONObject obj = new JSONObject(result);
            Log.d("SimpleWeatherAPP", obj.toString());

            //check "cod" value, 200 means success in fetching the data
            if(obj.getInt("cod") != 200){
                Toast.makeText(this, "No data retrieved from openweathermap API, please check your input and your internet connection", Toast.LENGTH_LONG).show();
            } else {
                //parse JSONObj
                JSONParser parser = new JSONParser();
                parser.renderWeather(obj);
                //get JSON parsing result
                ArrayList<List> weather7DayList = parser.getWeather7DayList();
                //set main view
                setMainView(0, weather7DayList);
                setListView(weather7DayList);
            }
        } catch (Throwable t) {
            Toast.makeText(this, "No data retrieved from openweathermap API, please check your input and your internet connection", Toast.LENGTH_LONG).show();
            Log.e("SimpleWeatherAPP", "Could not parse malformed JSON: \"" + result + "\"");
        }
    }

    /**
     * set main view in main activity
     */
    public void setMainView(int index, ArrayList<List> weather7DayList) {
        //set today weather view
        cityField.setText(weather7DayList.get(index).get(0).toString() +", "+ weather7DayList.get(0).get(1).toString());
        weatherField.setText(weather7DayList.get(index).get(3).toString());
        humidityField.setText("Humidity: "+ weather7DayList.get(index).get(4).toString() + " %");
        pressureField.setText("Pressure: "+ weather7DayList.get(index).get(5).toString() + " hPa");
        currentTemperatureField.setText(weather7DayList.get(index).get(6).toString() + " ℃" +
                    " - " + weather7DayList.get(index).get(7).toString() + " ℃");
        dateField.setText(weather7DayList.get(index).get(2).toString());
        String iconCode = setWeatherIcon(Integer.parseInt(weather7DayList.get(index).get(8).toString()));
        weatherIcon.setText(iconCode);
    }

    /**
     * set Listview in main activity
     */
    public void setListView(ArrayList<List> weather7DayList) {
        ListAdapter listAdapter = new ListAdapter(this, weather7DayList, weatherFont);
        ListView listView = (ListView) findViewById(R.id.weather_list);
        listView.setAdapter(listAdapter);
    }

    /**
     * set weather icon to be displayed
     */
    public String setWeatherIcon(int actualId){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            icon = this.getString(R.string.weather_sunny);
        } else {
            switch(id) {
                case 2 : icon = this.getString(R.string.weather_thunder);
                    break;
                case 3 : icon = this.getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = this.getString(R.string.weather_foggy);
                    break;
                case 8 : icon = this.getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = this.getString(R.string.weather_snowy);
                    break;
                case 5 : icon = this.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    /**
     * Open auto complete activity
     */
    public void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e("SimpleWeatherAPP", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("SimpleWeatherAPP", "Place Selected: " + place.getName().toString());
                Log.i("SimpleWeatherAPP", Double.toString(place.getLatLng().latitude)+","+ Double.toString(place.getLatLng().longitude));

                //fetch weather data via asynctask based on selected place
                OnlineService change = new OnlineService(MainActivity.this);
                change.execute(Double.toString(place.getLatLng().latitude), Double.toString(place.getLatLng().longitude));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("SimpleWeatherAPP", "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }

    /**
     * Detecting touch
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * Handling left swipe to go to next activity
     */
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            if(event2.getX() < event1.getX()){
                //switch another activity by swiping left
                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(getApplicationContext(), PlaceListActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }

            return true;
        }
    }

    /**
     * Refresh activity
     */
    public void refreshActivity() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

}
