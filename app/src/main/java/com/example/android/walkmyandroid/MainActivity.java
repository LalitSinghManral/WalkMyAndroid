/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.walkmyandroid;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements
        FetchAddressTask.OnTaskCompleted {

    private Button mGetLocation;
    private TextView mLocationTextView;
    private final static String TAG = "com.example.android.LOG";
    private final static int LOCATION_PERMISSION = 1;

    //private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;

    private boolean mTrackingLocation;

    private LocationCallback mLocationCallback;

    private final String LOCATION_TRACKING_STORE = "location_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGetLocation = findViewById(R.id.button_location);
        mLocationTextView = findViewById(R.id.textview_location);

        mAndroidImageView = findViewById(R.id.imageview_android);

        mRotateAnim = (AnimatorSet)AnimatorInflater.loadAnimator
                (this,R.animator.rotate);

        mRotateAnim.setTarget(mAndroidImageView);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTrackingLocation){
                    startTrackingLocation();
                }
                else {
                    stopTrackingLocation();
                }
            }
        });

        if (savedInstanceState != null){
            mTrackingLocation = savedInstanceState.getBoolean(LOCATION_TRACKING_STORE);
        }

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                new FetchAddressTask(MainActivity.this,
                        MainActivity.this).execute(locationResult.getLastLocation());
            }
        };
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }
        else {
            Log.i(TAG,"Location Permission Already Granted");

            //For Last Location
           /* mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {

                @Override
                public void onSuccess(Location location) {
                    *//*if (location != null){
                        mLastLocation = location;
                        mLocationTextView.setText(getString(R.string.location_text,
                                mLastLocation.getLatitude(),
                                mLastLocation.getLongitude(),
                                mLastLocation.getTime()));
                    }
                    else {
                        mLocationTextView.setText(R.string.no_location);
                    }*//*

                    new FetchAddressTask(MainActivity.this,
                            MainActivity.this).execute(location);
                }
            });*/

            //FOr tracking Location
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(),
                    mLocationCallback,null);
        }

        mLocationTextView.setText(getString(R.string.address_text,
                getString(R.string.loading),System.currentTimeMillis()));

        mRotateAnim.start();
        mTrackingLocation = true;
        mGetLocation.setText(R.string.stop_tracking_location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSION :

                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startTrackingLocation();
                }

                break;

                default:
                    return;
        }
    }


    private void stopTrackingLocation(){
        if (mTrackingLocation){
            mTrackingLocation = false;
            mRotateAnim.end();
            mGetLocation.setText(R.string.get_tracking_location);
            mLocationTextView.setText(R.string.textview_hint_track);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onTaskCompletion(String result) {
        if (mTrackingLocation){
            mLocationTextView.setText(getString(R.string.address_text,
                    result,System.currentTimeMillis()));
        }
        else {
            mLocationTextView.setText(R.string.gps_off);
        }
    }

    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(LOCATION_TRACKING_STORE, mTrackingLocation);
    }
}
