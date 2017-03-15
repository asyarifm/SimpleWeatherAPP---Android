package com.example.mimos.simpleweatherapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OnlineService extends AsyncTask<String, Integer, String> {

    //declare ontaskcomplete interface
    private OnTaskCompleted taskCompleted;

    //init variable
    private HttpURLConnection urlConnection = null;
    private int timeout = 60000;
    private int id = 0;

    public OnlineService(OnTaskCompleted c) {
        this.taskCompleted = c;
    }

    /**
     * asyntask process.
     */
    @Override
    protected String doInBackground(String... arg0) {
        String result = "";
        String api_key = "1a60c49a1ee041562874026d15cd7c2f";   //openweathermap api key
        //get latitude and longitude from input parameter
        String lat = (String) arg0[0];
        String lon = (String) arg0[1];

        //link to fetch weather data
        String url = "http://api.openweathermap.org/data/2.5/forecast/daily?lat="+lat+"&lon="+lon+"&units=metric&cnt=7&lang=en&appid="+api_key;
        Log.d("SimpleWeatherAPP", "URL: " + url);

        //if input parameter is more than 2, get 3rd parameter as weather information id
        if (arg0.length > 2) {
            this.id = Integer.parseInt(arg0[2]);
        }
        //requet weather data
        result = reqWeatherData(url, arg0.length);
        return result;
    }

    protected void onPreExecute(){

    }

    /**
     * retrieve asynctask result.
     */
    @Override
    protected void onPostExecute(String result) {
        //forward the result to onTaskCompleted interface
        taskCompleted.onTaskCompleted(result);
    }

    /**
     * Fetch JSON data from openweathermap api
     */
    public String reqWeatherData(String link, int numParams) {
        String result = "";
        Log.d("SimpleWeatherAPP", "RequestWeather");

        URL url = null;
        try {
            url = new URL(link);
            //init connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(timeout);
            urlConnection.connect();

            Log.d("SimpleWeatherApp", link);

            //fetching data
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = reader.readLine())!=null) {
                builder.append(line + "\n");
            }
            //finish fetching data
            is.close();

            Log.d("SimpleWeatherAPP", "JSON: " + builder.toString());
            //set the retrieved data as a result
            result = builder.toString();
            urlConnection.disconnect();

            //if there is id in input param, append at back of the result
            if(numParams > 2) {
                result = result + "//" + this.id;
            }
        } catch (Exception e) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            result = "timeout";
            Log.e("SimpleWeatherApp", e.getMessage());
        }

        return result;
    }
}
