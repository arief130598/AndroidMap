package com.example.androidmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.wanderingcan.persistentsearch.PersistentSearchView;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private MapView mapView;
    private Button btnmotor, btnmobil;
    private PersistentSearchView psinitplace, psdirectionplace;
    public int kendaraan = 0;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private MapboxMap mapboxMap;
    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, "pk.eyJ1IjoiYXJpZWYxMzA1OTgiLCJhIjoiY2s1NWNrbHloMDkzbzNucWkxYTJkMmpyOSJ9.gr6DVa3CzDJXrr667mRfvg");
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
                        UiSettings uiSettings = mapboxMap.getUiSettings();
                        uiSettings.setCompassGravity(Gravity.BOTTOM | Gravity.END);
                        uiSettings.setCompassMargins(0, 0, 155, 16);
                        System.out.println(uiSettings.getAttributionGravity());
                    }
                });
            }
        });

        btnmotor = findViewById(R.id.motor);
        btnmotor.setOnClickListener(this);
        btnmobil = findViewById(R.id.mobil);
        btnmobil.setOnClickListener(this);
        psinitplace = findViewById(R.id.initplace);
        psdirectionplace = findViewById(R.id.directionplace);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.motor:
                btnmotor.setBackgroundResource(R.drawable.rounded_btn_green);
                btnmotor.setTextColor(Color.parseColor("#FFFFFF"));
                btnmotor.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.ic_motorcycle_white_24dp, 0, 0);
                btnmobil.setBackgroundResource(R.drawable.rounded_btn);
                btnmobil.setTextColor(Color.parseColor("#000000"));
                btnmobil.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.ic_directions_car_black_24dp, 0, 0);
                kendaraan = 0;
                break;

            case R.id.mobil:
                btnmobil.setBackgroundResource(R.drawable.rounded_btn_green);
                btnmobil.setTextColor(Color.parseColor("#FFFFFF"));
                btnmobil.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.ic_directions_car_white_24dp, 0, 0);
                btnmotor.setBackgroundResource(R.drawable.rounded_btn);
                btnmotor.setTextColor(Color.parseColor("#000000"));
                btnmotor.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.ic_motorcycle_black_24dp, 0, 0);
                kendaraan = 1;
                break;
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Set Initial Location
                UiSettings uiSettings = mapboxMap.getUiSettings();
                uiSettings.setCompassGravity(Gravity.BOTTOM | Gravity.END);
                uiSettings.setCompassMargins(0, 0, 155, 16);
                System.out.println(uiSettings.getAttributionGravity());

                initSearchFab();
                addUserLocations();

                // Add the symbol layer icon to map for future use
                style.addImage(symbolIconId, BitmapFactory.decodeResource(
                        MainActivity.this.getResources(), R.drawable.ic_place_green_24dp));

                // Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);

                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);
            }
        });
    }

    private void initSearchFab() {
        psinitplace.setOnSearchListener(new PersistentSearchView.OnSearchListener() {
            @Override
            public void onSearchOpened() {
                psinitplace.openSearch();
            }

            @Override
            public void onSearchClosed() {
                //Called when the searchbar is closed by the user or by something calling
                //persistentSearchView.closeSearch();
            }

            @Override
            public void onSearchCleared() {
                //Called when the searchbar has been cleared by the user by removing all
                //the text or hitting the clear button. This also will be called if
                //persistentSearchView.populateSearchText() is set with a null string or
                //an empty string
            }

            @Override
            public void onSearchTermChanged(CharSequence term) {
                //Called when the text in the searchbar has been changed by the user or
                //by persistentSearchView.populateSearchText() with text passed in.
                //Best spot to handle giving suggestions to the user in the menu
            }

            @Override
            public void onSearch(CharSequence text) {
                //Called when the user hits the IME Action Search on the keyboard to search
                //Here is the best spot to handle searches
            }
        });
    }

    private void addUserLocations() {
        home = CarmenFeature.builder().text("Mapbox SF Office")
                .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
                .placeName("50 Beale St, San Francisco, CA")
                .id("mapbox-sf")
                .properties(new JsonObject())
                .build();

        work = CarmenFeature.builder().text("Mapbox DC Office")
                .placeName("740 15th Street NW, Washington DC")
                .geometry(Point.fromLngLat(-77.0338348, 38.899750))
                .id("mapbox-dc")
                .properties(new JsonObject())
                .build();
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
