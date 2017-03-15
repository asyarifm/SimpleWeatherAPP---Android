package com.example.mimos.simpleweatherapp;


import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListAdapter extends ArrayAdapter<List>{

    private Context context;
    private ArrayList<List> itemsArrayList;
    private Typeface weatherFont;

    public ListAdapter(Context context, ArrayList<List> itemsArrayList, Typeface weatherFont) {

        super(context, R.layout.weather_list_view, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
        this.weatherFont = weatherFont;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Get rowView from inflater
        View rowView = inflater.inflate(R.layout.weather_list_view, parent, false);

        //Get the two text view from the rowView
        TextView dateListView = (TextView) rowView.findViewById(R.id.date_list_view);
        TextView weatherIconListView = (TextView) rowView.findViewById(R.id.weather_icon_list_view);

        //Set the text for textView
        weatherIconListView.setTypeface(weatherFont);
        dateListView.setText(itemsArrayList.get(position).get(2).toString().replaceFirst(" ", "\n"));
        String iconCode = setWeatherIcon(Integer.parseInt(itemsArrayList.get(position).get(8).toString()));
        weatherIconListView.setText(iconCode);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMainView(position);
            }
        });

        return rowView;
    }

    public void changeMainView(int index) {
        TextView cityField = (TextView) ((Activity)context).findViewById(R.id.city_field);
        TextView dateField = (TextView) ((Activity)context).findViewById(R.id.date_field);
        TextView weatherField = (TextView) ((Activity)context).findViewById(R.id.weather_field);
        TextView humidityField = (TextView) ((Activity)context).findViewById(R.id.humidity_field);
        TextView pressureField = (TextView) ((Activity)context).findViewById(R.id.pressure_field);
        TextView currentTemperatureField = (TextView) ((Activity)context).findViewById(R.id.current_temperature_field);
        TextView weatherIcon = (TextView) ((Activity)context).findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        cityField.setText(itemsArrayList.get(index).get(0).toString() +", "+ itemsArrayList.get(0).get(1).toString());
        weatherField.setText(itemsArrayList.get(index).get(3).toString());
        humidityField.setText("Humidity: "+ itemsArrayList.get(index).get(4).toString() + " %");
        pressureField.setText("Pressure: "+ itemsArrayList.get(index).get(5).toString() + " hPa");
        currentTemperatureField.setText(itemsArrayList.get(index).get(6).toString() + " ℃" +
                " - " + itemsArrayList.get(index).get(7).toString() + " ℃");
        dateField.setText(itemsArrayList.get(index).get(2).toString());
        String iconCode = setWeatherIcon(Integer.parseInt(itemsArrayList.get(index).get(8).toString()));
        weatherIcon.setText(iconCode);
    }

    /**
     * Preparing List for 6 days weather data
     */
    /*
    private ArrayList<List> prepareList(ArrayList<List> weather7daysLists){
        ArrayList<List> items = new ArrayList<List>();
        for (int i = 1; i < weather7daysLists.size(); i++) {
            items.add(weather7daysLists.get(i));
        }
        return items;
    }
    */
    /**
     * set weather icon to be displayed
     */
    public String setWeatherIcon(int actualId){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            icon = context.getString(R.string.weather_sunny);
        } else {
            switch(id) {
                case 2 : icon = context.getString(R.string.weather_thunder);
                    break;
                case 3 : icon = context.getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = context.getString(R.string.weather_foggy);
                    break;
                case 8 : icon = context.getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = context.getString(R.string.weather_snowy);
                    break;
                case 5 : icon = context.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }
}
