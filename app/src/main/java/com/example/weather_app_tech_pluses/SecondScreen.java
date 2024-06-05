package com.example.weather_app_tech_pluses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class SecondScreen extends AppCompatActivity {


    //    weather api
    final String APP_ID = "ae6f26c375e21e775ffa0f24bf7d1069";
    //    weather api
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";


    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 101;

    String Location_Provider = LocationManager.GPS_PROVIDER;
    String Latitude;
    String Longitude;
    String address;

    TextView NameofCity, weatherState, Temperature, humidityTextView,latitudeTextView,longitudeTextView,addressTextView;
    ImageView mweatherIcon;
    RelativeLayout mCityFinder;
    LocationManager mLocationManager;
    LocationListener mLocationListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen);

        weatherState = findViewById(R.id.weatherCondition);
        Temperature = findViewById(R.id.temperature);
        mweatherIcon = findViewById(R.id.weatherIcon);
        mCityFinder = findViewById(R.id.cityFinder);
        NameofCity = findViewById(R.id.cityName);
        humidityTextView = findViewById(R.id.humidity);
        latitudeTextView = findViewById(R.id.latitude);
        longitudeTextView = findViewById(R.id.longitude);
        addressTextView = findViewById(R.id.address);

        //  go to next activity
        mCityFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondScreen.this, CityFinderActivity.class);
                startActivity(intent);
            }
        });

    }

    //    first method that runs when the app start
    @Override
    protected void onResume() {
        super.onResume();
        Intent mIntent=getIntent();
        String city= mIntent.getStringExtra("City");
        String latitude = mIntent.getStringExtra("Latitude");
        String longitude = mIntent.getStringExtra("Longitude");
        String address = mIntent.getStringExtra("Address");
        if(city!=null)
        {
            getWeatherForNewCity(city);
        }
        else
        {
            latitudeTextView.setText("Latitude: " + latitude);
            longitudeTextView.setText("Longitude: " + longitude);
            addressTextView.setText("Address: " + address);
            getWeatherForCurrentLocation();
        }
    }


    private void getWeatherForNewCity(String city)
    {
        RequestParams params=new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsdoSomeNetworking(params);

        latitudeTextView.setText("Latitude: ");
        longitudeTextView.setText("Longitude: ");
        addressTextView.setText("Address: ");

    }



    private void getWeatherForCurrentLocation() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Latitude = String.valueOf(location.getLatitude());
                Longitude = String.valueOf(location.getLongitude());

                latitudeTextView.setText("Latitude: " + Latitude);
                longitudeTextView.setText("Longitude: " + Longitude);

                Locale locale = Locale.getDefault();

                // Reverse geocode to get address
                Geocoder geocoder = new Geocoder(SecondScreen.this, locale);
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && addresses.size() > 0) {
                        address = addresses.get(0).getAddressLine(0);
                        addressTextView.setText("Address: " + address);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                RequestParams params = new RequestParams();
                params.put("lat", Latitude);
                params.put("lon", Longitude);
                params.put("appid", APP_ID);
                letsdoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //not able to get location
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListner);
    }

    //   checking is user allow location or not
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE)
        {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(SecondScreen.this,"Locationget Successffully" , Toast.LENGTH_SHORT).show();
                getWeatherForCurrentLocation();
            }
            else
            {
                //user denied the permission
            }
        }
    }

    //running when fetching the current location
    private void letsdoSomeNetworking(RequestParams params)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL,params,new JsonHttpResponseHandler()
        {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {

                Toast.makeText(SecondScreen.this, "Data Get Success",Toast.LENGTH_SHORT).show();
                com.example.weather_app_tech_pluses.WeatherData weatherD= null;
                try {
                    weatherD = com.example.weather_app_tech_pluses.WeatherData.fromJson(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                updateUI(weatherD);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }


    private void updateUI(WeatherData weather)
    {
        Temperature.setText(weather.getmTemperature());
        NameofCity.setText("Location: " +weather.getMcity());
        weatherState.setText("State: " +weather.getmWeatherType());
        int resourseID = getResources().getIdentifier(weather.getMicon(),"drawable",getPackageName());
        mweatherIcon.setImageResource(resourseID);
        humidityTextView.setText("Humidity: " + weather.getHumidity() + "%");
        latitudeTextView.setText("Latitude: " + Latitude);
        longitudeTextView.setText("Longitude: " + Longitude);
        addressTextView.setText("Address: " + this.address);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager!=null)
        {
            mLocationManager.removeUpdates(mLocationListner);
        }
    }
}