package com.example.mimos.simpleweatherapp;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaceListActivity extends AppCompatActivity implements OnTaskCompleted {
    //declare global variable
    private GestureDetectorCompat gestureDetectorCompat;
    private RecyclerView recyclerView;
    private PlacesAdapter adapter;
    private ArrayList<List> placeList;
    private DatabaseHandler db;
    private FloatingActionButton fab;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);

        //load all data from database
        loadAlldata();

        //init variable
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        placeList = new ArrayList<List>();
        adapter = new PlacesAdapter(this, placeList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);

        //if recycleview is not null, add layout, decoration, animation and item adapter to it
        if(recyclerView != null) {
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
        }

        //detect touch
        gestureDetectorCompat = new GestureDetectorCompat(this, new My2ndGestureListener());
        //handle swipe right
        fab = (FloatingActionButton) findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open auto complete activity by googleplaceapi
                openAutocompleteActivity();
            }
        });
    }

    /**
     * Load all data from database.
     */
    public void loadAlldata() {
        db = new DatabaseHandler(this);
        //Reading all places
        Log.d("SimpleWeatherAPP ", "Reading all places..");
        placeList = db.getAllPlaces();

        //fetch weather information for every place
        if (!placeList.isEmpty()) {
            for (List pl : placeList) {
                OnlineService task = new OnlineService(PlaceListActivity.this);
                task.execute(pl.get(1).toString(), pl.get(2).toString(), pl.get(0).toString());
            }
        } else {
            //if the database empty show this message
            Toast.makeText(this, "There is no record in database, please click '+' button to add a record", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Adding weather data to the list
     */
    private void retrievePlacesList(List weatherToday, String db_id) {

        placeList.add(Arrays.asList(weatherToday.get(0), weatherToday.get(1), weatherToday.get(2), weatherToday.get(8), db_id));

        adapter.notifyDataSetChanged();
    }

    /**
     * getting result of asynctask process
     */
    @Override
    public void onTaskCompleted(String result){
        Log.d("SimpleWeatherAPP", "result : " + result);
        //split the weather information and id
        String[] data = result.split("//");
        Log.d("SimpleWeatherAPP", "aftersplit result : " + result);
        result = data[0];
        String db_id = data[1];

        try {
            //convert result to JSONObj
            JSONObject obj = new JSONObject(result);
            Log.d("SimpleWeatherAPP", obj.toString());

            //check "cod" value, 200 means success in fetching the data
            if(obj.getInt("cod") != 200){
                Toast.makeText(this, "No data retrieved from openweathermap API", Toast.LENGTH_LONG).show();
            } else {
                //parse JSONObj
                JSONParser parser = new JSONParser();
                parser.renderWeather(obj);
                ArrayList<List> weather7DayList = parser.getWeather7DayList();
                //pass today weather to list
                retrievePlacesList(weather7DayList.get(0), db_id);
            }
        } catch (Throwable t) {
            Toast.makeText(this, "No data retrieved from openweathermap API, please check your input and your internet connection", Toast.LENGTH_LONG).show();
            Log.e("Simple Weather APP", "Could not parse malformed JSON: \"" + result + "\"");
        }
    }

    /**
     *RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    /**
     * detecting touch
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * handle swipe right
     */
    class My2ndGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            if(event2.getX() > event1.getX()){

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }

            return true;
        }
    }

    /**
     * Open auto complete activity
     */
    private void openAutocompleteActivity() {
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
                Log.i("SimpleWeatherAPP", "Place Added: " + place.getName().toString());
                Log.i("SimpleWeatherAPP", Double.toString(place.getLatLng().latitude) + "," + Double.toString(place.getLatLng().longitude));

                //add a place to db
                db.addPlace(Double.toString(place.getLatLng().latitude), Double.toString(place.getLatLng().longitude));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("SimpleWeatherAPP", "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }
}
