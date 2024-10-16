package com.example.safetapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.safetapp.Model.LocationC;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class livePoliceLocationAndNearbyPlaces extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Context context = this;
    Marker m;
    private static final int REQUEST_CODE_LOCATION_PERMISION = 1;
    private String lat, lon;
    double latitude, longitude;
    DatabaseReference livePoliceLocRef = FirebaseDatabase.getInstance().getReference("Live Police Location");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_police_location_and_nearby_places);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(livePoliceLocationAndNearbyPlaces.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(livePoliceLocationAndNearbyPlaces.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            Location location = locationResult.getLastLocation();
                            Log.d("LocationValue", "onLocationResult:" + location.getLatitude());
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                        }
                    }
                }, Looper.getMainLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Example Police Stations near Kodambakkam, Royapuram, and Thiruvika Nagar
        m = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(13.0635, 80.2482)) // Royapuram Police Station
                .title("Royapuram Police Station")
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_police_icon)));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(13.0500, 80.2115)) // Kodambakkam Police Station
                .title("Kodambakkam Police Station")
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_police_icon)));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(13.0713, 80.2177)) // Thiruvika Nagar Police Station
                .title("Thiruvika Nagar Police Station")
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_police_icon)));

        // Example Hospitals
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(13.0590, 80.2150)) // Fortis Malar Hospital
                .title("Fortis Malar Hospital")
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hospital)));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(13.0525, 80.2180)) // Ramachandra Hospital
                .title("Sri Ramachandra Medical Center")
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hospital)));

        // Example NGOs
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(13.0700, 80.2100)) // NGO Example
                .title("Helping Hands NGO")
                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ngo)));

        livePoliceLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LocationC locationC = dataSnapshot.getValue(LocationC.class);
                lat = locationC.getLatitude();
                lon = locationC.getLongitude();
                lat = lat.replace("!", ".");
                lon = lon.replace("!", ".");
                double latitude, longitude;
                latitude = Double.parseDouble(lat);
                longitude = Double.parseDouble(lon);
                updateMarker(latitude, longitude);
            }

            private void updateMarker(double latitude, double longitude) {
                m.setPosition(new LatLng(latitude, longitude));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        getCurrentLocation();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable drawable = ContextCompat.getDrawable(context, vectorResId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        Button dialogButton = dialog.findViewById(R.id.btn_submit);
        final EditText Review = dialog.findViewById(R.id.review);
        final RatingBar ratingBar = dialog.findViewById(R.id.rating_bar);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rating = String.valueOf(ratingBar.getRating());
                String review = Review.getText().toString();
                Intent intent = new Intent(livePoliceLocationAndNearbyPlaces.this, PolygonMap.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
