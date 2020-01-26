package com.example.androidmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private MapView mapView;
    private Button btnmotor, btnmobil;
    private EditText etinitplace, etdirectionplace;
    public  static int kendaraan = 0;
    public static int kode_cari = 0;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private MapboxMap mapboxMap;
    private String MAPBOX_API = BuildConfig.MAPBOX_API_KEY;
    public CarmenFeature init, direction;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    Symbol motorsymbol, mobilsymbol, tujuansymbol;
    SymbolManager symbolManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, MAPBOX_API);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
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

                // Annotation / Marker
                symbolManager = new SymbolManager(mapView, mapboxMap, style);

                symbolManager.setIconAllowOverlap(true);
                symbolManager.setIconIgnorePlacement(true);

                // Add symbol at specified lat/lon

                Bitmap motorblue = getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_motorcycle_blue_24dp);
                style.addImage("motor_blue", motorblue);
                Bitmap markerblue = getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_place_blue_24dp);
                style.addImage("marker_blue", markerblue);
                Bitmap carblue = getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_directions_car_blue_24dp);
                style.addImage("car_blue", carblue);

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
            public void onClick(View v) {
                kode_cari = 0;
                Intent intent = new PlaceAutocomplete.IntentBuilder()
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
            public void onClick(View v) {
                kode_cari = 1;
                Intent intent = new PlaceAutocomplete.IntentBuilder()
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
            if(kode_cari == 0){
                init = PlaceAutocomplete.getPlace(data);
                etinitplace.setText(init.placeName());

                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        // Annotation / Marker
                        if(kendaraan == 0){
                            if(motorsymbol != null){
                                symbolManager.delete(motorsymbol);
                            }

                            if(mobilsymbol != null){
                                symbolManager.delete(mobilsymbol);
                            }
                            // Add symbol at specified lat/lon
                            motorsymbol = symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(((Point) init.geometry()).latitude(),
                                            ((Point) init.geometry()).longitude()))
                                    .withIconImage("motor_blue")
                                    .withIconSize(1.0f));
                        }else{
                            if(motorsymbol != null){
                                symbolManager.delete(motorsymbol);
                            }

                            if(mobilsymbol != null){
                                symbolManager.delete(mobilsymbol);
                            }
                            // Add symbol at specified lat/lon
                            Symbol mobil = symbolManager.create(new SymbolOptions()
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
                    }
                }
            }else{
                direction = PlaceAutocomplete.getPlace(data);
                etdirectionplace.setText(direction.placeName());

                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        if(tujuansymbol != null){
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
                }
            }
        }
    }

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
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
