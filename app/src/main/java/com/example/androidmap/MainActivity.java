package com.example.androidmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private MapView mapView;
    private Button btnmotor, btnmobil;
    private EditText etinitplace, etdirectionplace;
    public  static int kendaraan = 0;
    public static int kode_cari = 0;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private MapboxMap mapboxMap;
    private String MAPBOX_API = BuildConfig.MAPBOX_API_KEY; // API Key
    public CarmenFeature init, direction;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    Symbol motorsymbol, mobilsymbol, tujuansymbol;
    SymbolManager symbolManager;
    private Point start, finish;
    private MapboxDirections client;
    private DirectionsRoute currentRoute;


    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get API Instance FROM API
        Mapbox.getInstance(this, MAPBOX_API);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        // Create Map, this will override onMapReady
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnmotor = findViewById(R.id.motor);
        btnmotor.setOnClickListener(this);
        btnmobil = findViewById(R.id.mobil);
        btnmobil.setOnClickListener(this);
        etinitplace = findViewById(R.id.initplace);
        etinitplace.setOnClickListener(this);
        etdirectionplace = findViewById(R.id.directionplace);
        etdirectionplace.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.motor: // When Motorcycle Button Clicked
                // Change Color Button
                btnmotor.setBackgroundResource(R.drawable.rounded_btn_green);
                btnmotor.setTextColor(Color.parseColor("#FFFFFF"));
                btnmotor.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.ic_motorcycle_white_24dp, 0, 0);
                btnmobil.setBackgroundResource(R.drawable.rounded_btn);
                btnmobil.setTextColor(Color.parseColor("#000000"));
                btnmobil.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.ic_directions_car_black_24dp, 0, 0);
                // Set State of Vehicle to 0 When Motorcycle is active
                kendaraan = 0;
                break;

            case R.id.mobil: // When Car Button Clicked
                // Change Color Button
                btnmobil.setBackgroundResource(R.drawable.rounded_btn_green);
                btnmobil.setTextColor(Color.parseColor("#FFFFFF"));
                btnmobil.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.ic_directions_car_white_24dp, 0, 0);
                btnmotor.setBackgroundResource(R.drawable.rounded_btn);
                btnmotor.setTextColor(Color.parseColor("#000000"));
                btnmotor.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.ic_motorcycle_black_24dp, 0, 0);
                // Set State of Vehicle to 1 When Car is active
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
                // Set Compass Position
                uiSettings.setCompassGravity(Gravity.BOTTOM | Gravity.END);
                uiSettings.setCompassMargins(0, 0, 155, 16);

                // Annotation / Marker
                // Create SymbolManager for Calling Annotation or Marker
                symbolManager = new SymbolManager(mapView, mapboxMap, style);
                // Set stle of icon marker
                symbolManager.setIconAllowOverlap(true);
                symbolManager.setIconIgnorePlacement(true);

                // Create customize marker form drawable
                Bitmap motorblue = getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_motorcycle_blue_24dp);
                style.addImage("motor_blue", motorblue);
                Bitmap markerblue = getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_place_blue_24dp);
                style.addImage("marker_blue", markerblue);
                Bitmap carblue = getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_directions_car_blue_24dp);
                style.addImage("car_blue", carblue);

                // This will initiate function to call Search
                initSearchFab();

                // Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);

                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);
            }
        });
    }

    private void initSearchFab() {
        etinitplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // When Edit Text init clicked
                kode_cari = 0; // This state for init
                Intent intent = new PlaceAutocomplete.IntentBuilder() // Call Search
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : MAPBOX_API)
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MainActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });

        etdirectionplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // When Edit Text Direction clicked
                kode_cari = 1; // This state for direction
                Intent intent = new PlaceAutocomplete.IntentBuilder() // Call Search
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : MAPBOX_API)
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MainActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    // Create an empty GeoJSON source using the empty feature collection
    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    // Set up a new symbol layer for displaying the searched location's feature coordinates
    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    // Create Direction When Init Place and Direction not Null
    private void callDirection(Style style){
        if(client!=null){ // If route already create
            getRoute(start, finish);
        }else{ // If route not created or first run
            style.addSource(new GeoJsonSource(ROUTE_SOURCE_ID,
                    FeatureCollection.fromFeatures(new Feature[] {})));

            GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[] {
                    Feature.fromGeometry(start),
                    Feature.fromGeometry(finish)}));
            style.addSource(iconGeoJsonSource);

            LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

            // Add the LineLayer to the map. This layer will display the directions route.
            routeLayer.setProperties(
                    lineCap(Property.LINE_CAP_ROUND),
                    lineJoin(Property.LINE_JOIN_ROUND),
                    lineWidth(5f),
                    lineColor(Color.parseColor("#009688"))
            );
            style.addLayer(routeLayer);
            getRoute(start, finish);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if(kode_cari == 0){ // When searching initial place
                init = PlaceAutocomplete.getPlace(data); // Get Data
                etinitplace.setText(init.placeName()); // Set Edit Text

                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        // Annotation / Marker
                        if(kendaraan == 0){ // If Vehicle was motorcycle
                            if(motorsymbol != null){
                                symbolManager.delete(motorsymbol); // Check if icon already created
                            }

                            if(mobilsymbol != null){ // Check if icon already created
                                symbolManager.delete(mobilsymbol);
                            }
                            // Add symbol at specified lat/lon
                            motorsymbol = symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(((Point) init.geometry()).latitude(),
                                            ((Point) init.geometry()).longitude()))
                                    .withIconImage("motor_blue")
                                    .withIconSize(1.0f));
                        }else{ // Vechicle was car
                            if(motorsymbol != null){ // Check if icon already created
                                symbolManager.delete(motorsymbol);
                            }

                            if(mobilsymbol != null){ // Check if icon already created
                                symbolManager.delete(mobilsymbol);
                            }
                            // Add symbol at specified lat/lon
                            motorsymbol = symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(((Point) init.geometry()).latitude(),
                                            ((Point) init.geometry()).longitude()))
                                    .withIconImage("car_blue")
                                    .withIconSize(1.0f));
                        }

                        // Move map camera to the selected location
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(((Point) init.geometry()).latitude(),
                                                ((Point) init.geometry()).longitude()))
                                        .zoom(13)
                                        .build()), 3000);

                        if(direction!=null){ // If initial and direction place not null, call function direction to create route
                            start = Point.fromLngLat(((Point) init.geometry()).longitude(), ((Point) init.geometry()).latitude());
                            finish = Point.fromLngLat(((Point) direction.geometry()).longitude(), ((Point) direction.geometry()).latitude());
                            callDirection(style);
                        }
                    }
                }
            }else{
                direction = PlaceAutocomplete.getPlace(data); // Get place data
                etdirectionplace.setText(direction.placeName()); // Set text to direction edit text

                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        if(tujuansymbol != null){ // Check if icon already created
                            symbolManager.delete(tujuansymbol);
                        }

                        // Add symbol at specified lat/lon
                        tujuansymbol = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(((Point) direction.geometry()).latitude(),
                                        ((Point) direction.geometry()).longitude()))
                                .withIconImage("marker_blue")
                                .withIconSize(1.0f));

                        // Move map camera to the selected location
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(((Point) direction.geometry()).latitude(),
                                                ((Point) direction.geometry()).longitude()))
                                        .zoom(13)
                                        .build()), 3000);
                    }
                    if(init!=null){ // If initial and direction place not null, call function direction to create route
                        start = Point.fromLngLat(((Point) init.geometry()).longitude(), ((Point) init.geometry()).latitude());
                        finish = Point.fromLngLat(((Point) direction.geometry()).longitude(), ((Point) direction.geometry()).latitude());
                        if (style != null) {
                            callDirection(style);
                        }
                    }
                }
            }
        }
    }

    // Create root
    private void getRoute(Point origin, Point destination) {
        // Get initial or setting route
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                .accessToken(MAPBOX_API)
                .build();

        // Call API from MapBox Direction API
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
            // You can get the generic HTTP info about the response
                Timber.d("Response code: " + response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }

                // Get the directions route
                currentRoute = response.body().routes().get(0);

                // Make a toast which displays the route's distance
                Toast.makeText(MainActivity.this, String.valueOf(currentRoute.distance()), Toast.LENGTH_SHORT).show();

                if (mapboxMap != null) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

                            // Retrieve and update the source designated for showing the directions route
                            GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

                            // Create a LineString with the directions route's geometry and
                            // reset the GeoJSON source for the route LineLayer source
                            if (source != null) {
                                Timber.d("onResponse: source != null");
                                source.setGeoJson(FeatureCollection.fromFeature(
                                        Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: " + throwable.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Convert drawable to bitmap
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = null;
        if (drawable != null) {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
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
        // Cancel the Directions API request
        if (client != null) {
            client.cancelCall();
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
