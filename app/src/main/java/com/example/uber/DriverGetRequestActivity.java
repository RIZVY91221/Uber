package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverGetRequestActivity extends AppCompatActivity implements View.OnClickListener {

    private Button nerbyRequest;
    private ListView requestList;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ArrayList<String> listRequestsforDrive;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_get_request);

        nerbyRequest=findViewById(R.id.btnUpadateRequest);
        requestList=findViewById(R.id.listRequest);
        listRequestsforDrive=new ArrayList<>();
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,listRequestsforDrive);
        requestList.setAdapter(adapter);
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        listRequestsforDrive.clear();

        nerbyRequest.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_driver,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.driverLogout){

            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {

                    if (e==null){
                        Toast.makeText(DriverGetRequestActivity.this, "Logout Driver", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(DriverGetRequestActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {


        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateRequestListView(location);
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
            Location currentDriverLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLocation);
        }
        else if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(DriverGetRequestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DriverGetRequestActivity.this,new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},1000);
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        }
    }
    
    private void updateRequestListView(Location driverLocation){
       if (driverLocation !=null){
           listRequestsforDrive.clear();
           final ParseGeoPoint driverCurrentLocation=new ParseGeoPoint(driverLocation.getLatitude(),driverLocation.getLongitude());
           ParseQuery<ParseObject> requestQuery= ParseQuery.getQuery("RequestCar");
           requestQuery.whereNear("PassengerLocation",driverCurrentLocation);
           requestQuery.findInBackground(new FindCallback<ParseObject>() {
               @Override
               public void done(List<ParseObject> objects, ParseException e) {
                   if (e==null){
                       if (objects.size()>0){
                           for (ParseObject nearRequest:objects){
                               Double milesDistencePassenger=driverCurrentLocation.distanceInMilesTo((ParseGeoPoint) nearRequest.get("PassengerLocation"));
                               float roundedValuMiles=Math.round(milesDistencePassenger*10)/10;
                               listRequestsforDrive.add("There Are: "+roundedValuMiles+ " Miles to "+nearRequest.get("username"));

                           }

                       }
                       else {
                           Toast.makeText(DriverGetRequestActivity.this, "No request Here!", Toast.LENGTH_SHORT).show();
                       }

                   }
                   adapter.notifyDataSetChanged();

               }
           });
       }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(DriverGetRequestActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        }
    }
}



