package com.example.mimos.simpleweatherapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<List> placeList;
    private Typeface weatherFont;

    /**
     * Creating Recycle View holder
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView city, date, weatherIcon, country;
        public int db_id;

        //each holder shows city, country, date and weather icon
        public MyViewHolder(View view) {
            super(view);
            weatherFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/weather.ttf");
            city = (TextView) view.findViewById(R.id.city_field_card);
            country = (TextView) view.findViewById(R.id.country_field_card);
            date = (TextView) view.findViewById(R.id.date_field_card);
            weatherIcon = (TextView) view.findViewById(R.id.weather_field_card);
            weatherIcon.setTypeface(weatherFont);
        }
    }

    //init adapter
    public PlacesAdapter(Context mContext, ArrayList<List> placeList) {
        this.mContext = mContext;
        this.placeList = placeList;
    }

    /**
     * Called when creating MyViewHolder holder.
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //put place_card layout as itemView design
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_card, parent, false);

        return new MyViewHolder(itemView);
    }

    /**
     * set each of viewholder.
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.city.setText(placeList.get(position).get(0).toString());
        holder.country.setText(placeList.get(position).get(1).toString());
        holder.date.setText(placeList.get(position).get(2).toString());
        String iconCode = setWeatherIcon(Integer.parseInt(placeList.get(position).get(3).toString()));
        holder.weatherIcon.setText(iconCode);
        holder.db_id = Integer.parseInt(placeList.get(position).get(4).toString());
        Log.d("SimpleWeatherAPP", "holder.db_id: " + holder.db_id);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.itemView, holder.db_id);
            }
        });

    }

    /**
     * Showing popup menu when touch the itemview
     */
    private void showPopupMenu(View view, int db_id) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(db_id));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private int db_id;
        public MyMenuItemClickListener(int db_id) {
            this.db_id = db_id;
            Log.d("SimpleWeatherAPP", "db_id: " + db_id);
        }

        /**
         * Called when user touch menu item (details / remove).
         */
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            Intent intent;
            DatabaseHandler db = new DatabaseHandler(mContext);
            switch (menuItem.getItemId()) {
                case R.id.action_details:
                    //touch detail will bring the user to 7 days weather report of selected place
                    String[] place = db.getPlace(this.db_id);
                    ((Activity) mContext).finish();
                    ((Activity) mContext).overridePendingTransition(0, 0);
                    intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("latitude", place[1]);
                    intent.putExtra("longitude", place[2]);
                    intent.setFlags(intent.getFlags());
                    mContext.startActivity(intent);
                    ((Activity) mContext).overridePendingTransition(0, 0);

                    return true;
                case R.id.action_remove:
                    //touch remove, will remove selected place
                    db.deleteCity(this.db_id);
                    ((Activity) mContext).finish();
                    ((Activity) mContext).overridePendingTransition(0, 0);
                    intent = new Intent(mContext, PlaceListActivity.class);
                    intent.setFlags(intent.getFlags());
                    mContext.startActivity(intent);
                    ((Activity) mContext).overridePendingTransition(0, 0);

                    return true;
                default:
            }
            return false;
        }
    }

    /**
     * Get number of item.
     */
    @Override
    public int getItemCount() {
        return placeList.size();
    }

    /**
     * Set weather icon.
     */
    private String setWeatherIcon(int actualId){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            icon = mContext.getString(R.string.weather_sunny);
        } else {
            switch(id) {
                case 2 : icon = mContext.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = mContext.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = mContext.getString(R.string.weather_foggy);
                    break;
                case 8 : icon = mContext.getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = mContext.getString(R.string.weather_snowy);
                    break;
                case 5 : icon = mContext.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }
}
