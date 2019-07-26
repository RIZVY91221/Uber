package com.example.uber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button requestBtn;
    private boolean isCancelRequest=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ParseQuery<ParseObject>  cancelRequestQuery=ParseQuery.getQuery("RequestCar");
        cancelRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        cancelRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size()>0 && e==null){
                    isCancelRequest=false;
                    requestBtn.setText("Cancel Request");
                }
            }
        });

        findViewById(R.id.logoutFromPassenger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e==null) {
                            finish();
                        }
                    }
                });
            }
        });

        requestBtn=findViewById(R.id.reqestBtn);
        requestBtn.setOnClickListener(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updatePassengerCameraLoacation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT<23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        else if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
               ActivityCompat.requestPermissions(PassengerActivity.this,new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},1000);
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updatePassengerCameraLoacation(currentPassengerLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(PassengerActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updatePassengerCameraLoacation(currentPassengerLocation);
            }
        }
    }
    public void updatePassengerCameraLoacation(Location location){
        LatLng passengerLocation=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation,15));

        mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here!"));
    }

    @Override
    public void onClick(View v) {

        if (isCancelRequest) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentpassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (currentpassengerLocation != null) {

                    ParseObject requestCar = new ParseObject("RequestCar");
                    requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint userLocation = new ParseGeoPoint(currentpassengerLocation.getLatitude(), currentpassengerLocation.getLongitude());
                    requestCar.put("PassengerLocation", userLocation);

                    requestCar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                FancyToast.makeText(PassengerActivity.this, "Request Send ", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                                requestBtn.setText("Cancel Request");
                                isCancelRequest=false;

                            } else {
                                FancyToast.makeText(PassengerActivity.this, e.getMessage(), FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "There is no user!", Toast.LENGTH_SHORT).show();
                }
            }
        }else {

            ParseQuery<ParseObject>  currentRequestQuery=ParseQuery.getQuery("RequestCar");
            currentRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            currentRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> requestList, ParseException e) {
                    if (requestList.size()>0 && e==null){
                        isCancelRequest=true;
                        requestBtn.setText("Request Car");

                        for (ParseObject request:requestList){
                            request.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    FancyToast.makeText(PassengerActivity.this,"Cancel request",FancyToast.LENGTH_SHORT,FancyToast.INFO,true).show();

                                }
                            });
                        }
                    }
                }
            });
        }
    }


}
