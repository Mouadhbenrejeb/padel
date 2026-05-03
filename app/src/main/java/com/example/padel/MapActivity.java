package com.example.padel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    MapView map;
    FusedLocationProviderClient fusedLocationClient;

    GeoPoint padelPoint = new GeoPoint(33.8882449, 10.1158896);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        map = findViewById(R.id.map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        map.getController().setZoom(18.0);
        map.getController().setCenter(padelPoint);

        Marker marker = new Marker(map);
        marker.setPosition(padelPoint);
        marker.setTitle("PadelPark");
        map.getOverlays().add(marker);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

        } else {
            getMyLocation();
        }
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        GeoPoint myPoint = new GeoPoint(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        Marker myMarker = new Marker(map);
                        myMarker.setPosition(myPoint);
                        myMarker.setTitle("Ma position");

                        map.getOverlays().add(myMarker);
                        map.invalidate();

                    } else {
                        Toast.makeText(this,
                                "Position indisponible",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation();
            } else {
                Toast.makeText(this,
                        "Permission localisation refusée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}