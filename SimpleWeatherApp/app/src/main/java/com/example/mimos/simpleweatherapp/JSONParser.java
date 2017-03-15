package com.example.mimos.simpleweatherapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Class to extract information from JSON
 */
public class JSONParser {
    private ArrayList<List> weather7DayList;

    public JSONParser() {
        weather7DayList = new ArrayList<List>();
    }

    public void renderWeather(JSONObject json) {
        try {
            //put 7 days weather information as a JSONArray
            JSONArray arrJson= json.getJSONArray("list");
            //get city name
            String city = json.getJSONObject("city").getString("name").toUpperCase(Locale.US);
            //get country code (2 chars)
            String country = json.getJSONObject("city").getString("country");

            String date = "";
            String weather = "";
            String humidity = "";
            String pressure = "";
            String minTemp = "";
            String maxTemp = "";
            String icon = "";

            //get weather information for each day
            for (int i=0; i < arrJson.length(); i++) {
                //getdate
                DateFormat df = DateFormat.getDateTimeInstance();
                DateFormat formatter = new SimpleDateFormat("EEEE d/MM/yyyy");
                date = formatter.format(new Date(arrJson.getJSONObject(i).getLong("dt") * 1000));

                //getweather
                JSONObject details= arrJson.getJSONObject(i).getJSONArray("weather").getJSONObject(0);
                Log.d("SimpleWeatherAPP", "details: " + details.toString());
                weather = details.getString("description").toUpperCase(Locale.US);
                Log.d("SimpleWeatherAPP", "weather: " + weather);

                //gethumidity
                humidity = arrJson.getJSONObject(i).getString("humidity");
                Log.d("SimpleWeatherAPP", "humidity: " + humidity);

                //getpressure
                pressure = arrJson.getJSONObject(i).getString("pressure");
                Log.d("SimpleWeatherAPP", "pressure: " + pressure);

                //gettemperature
                JSONObject temp = arrJson.getJSONObject(i).getJSONObject("temp");
                minTemp = temp.get("min").toString();
                maxTemp = temp.get("max").toString();
                Log.d("SimpleWeatherAPP", "minTemp: " + minTemp + " maxTemp: " + maxTemp);

                //geticoncode (iconid)
                icon = details.get("id").toString();

                //add each day weather information to a list
                weather7DayList.add(i, Arrays.asList(city, country, date, weather, humidity, pressure, minTemp, maxTemp, icon));
            }
        } catch (Exception e) {
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    public ArrayList<List> getWeather7DayList() {
        return weather7DayList;
    }
}
