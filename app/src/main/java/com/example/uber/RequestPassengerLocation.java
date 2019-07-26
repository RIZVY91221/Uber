package com.example.uber;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RequestPassengerLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnPickUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_passenger_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnPickUp=findViewById(R.id.btnPickUp);
        btnPickUp.setText("Pick Up "+getIntent().getStringExtra("rUsername"));

        btnPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Toast.makeText(RequestPassengerLocation.this, "Pick Up"+getIntent().getStringExtra("rUsername"), Toast.LENGTH_SHORT).show();

                ParseQuery<ParseObject> pickUpQuery=ParseQuery.getQuery("RequestCar");
                pickUpQuery.whereEqualTo("username",getIntent().getStringExtra("rUsername"));
                pickUpQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size()>0 && e==null){
                            for (ParseObject uberRequest:objects){
                                uberRequest.put("pickUpDriver", ParseUser.getCurrentUser().getUsername());
                                uberRequest.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e==null){
                                            Intent googleNavigate=new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr="
                                                            + getIntent().getDoubleExtra("dLatitude",
                                                            0) + ","
                                                            + getIntent().getDoubleExtra("dLongitude",
                                                            0) + "&" + "daddr="
                                                            + getIntent().getDoubleExtra("pLatitude",
                                                            0) + "," +
                                                            getIntent().getDoubleExtra("pLongitude",
                                                                    0)));
                                            startActivity(googleNavigate);
                                        }
                                        else {
                                            Toast.makeText(RequestPassengerLocation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });

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


       LatLng pLocation=new LatLng(getIntent().getDoubleExtra("pLatitude",0),
               getIntent().getDoubleExtra("pLongitude",0));

        LatLng dLocation=new LatLng(getIntent().getDoubleExtra("dLatitude",0),
                getIntent().getDoubleExtra("dLongitude",0));

      // mMap.addMarker(new MarkerOptions().position(pLocation).title("PassengerLocation"));
      // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pLocation,15));

        // Add a marker in Sydney and move the camera
        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        Marker pMarker=mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger here !"));
        Marker dMarker=mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver here!"));

        ArrayList<Marker>myMarker=new ArrayList<>();
        myMarker.add(pMarker);
        myMarker.add(dMarker);

        for (Marker marker:myMarker){
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds= builder.build();
        CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngBounds(bounds,0);
        mMap.animateCamera(cameraUpdate);

    }
}
