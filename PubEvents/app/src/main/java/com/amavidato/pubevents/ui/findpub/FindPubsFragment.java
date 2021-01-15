package com.amavidato.pubevents.ui.findpub;


import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.ui.findpub.map.MapViewModel;
import com.amavidato.pubevents.ui.findpub.FindPubsFragmentDirections;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.MyFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Map;

public class FindPubsFragment extends MyFragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = FindPubsFragment.class.getSimpleName();

    private MapViewModel mapViewModel;
    private ProgressBar progressBar;

    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    private boolean nightMode;
    // The entry points to the Places API.
//    private GeoDataClient mGeoDataClient;
//    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Quattromiglia di Rende - Cosenza, Italia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(39.353030, 16.235269);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1234;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private ImageButton mapModeBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        mapViewModel =
                ViewModelProviders.of(this).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_findpub_map, container, false);

        mapModeBtn = root.findViewById(R.id.mapview_style_btn);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Drawable img = null;
        if(hour < 6 || hour > 18){
            img = getResources().getDrawable(R.drawable.ic_baseline_day_24);
            nightMode = true;
        }else{
            img = getResources().getDrawable(R.drawable.ic_baseline_nights_stay_24);
            nightMode = false;
        }
        mapModeBtn.setBackground(img);
        mapModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean success = false;
                if(nightMode){
                    mapModeBtn.setBackground(getResources().getDrawable(R.drawable.ic_baseline_nights_stay_24));
                    success = mMap.setMapStyle(new MapStyleOptions(getResources()
                            .getString(R.string.map_style_day)));

                }else{
                    mapModeBtn.setBackground(getResources().getDrawable(R.drawable.ic_baseline_day_24));
                    success = mMap.setMapStyle(new MapStyleOptions(getResources()
                            .getString(R.string.map_style_night)));
                }
                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }else{
                    nightMode = !nightMode;
                }
            }
        });

        final TextView textView = root.findViewById(R.id.text_findpub);
        mapViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        progressBar = root.findViewById(R.id.loading);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_findpub_map);
        mapFragment.getMapAsync(this);

        setHasOptionsMenu(true);

        return root;
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


        boolean success = false;
        if(nightMode){
            mMap.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.map_style_night)));
        }else{
            mMap.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.map_style_day)));
        }

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        mMap.setMaxZoomPreference(20);

        initMapMarkers(mMap);

        // Move camera to default location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

    }

    private void initMapMarkers(final GoogleMap mMap) {

        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, document.getId() + " => " + data);
                        GeoPoint point = ((GeoPoint) data.get(DBManager.CollectionsPaths.PubFields.GEOLOCATION));
                        LatLng pubLocation = new LatLng(point.getLatitude(), point.getLongitude());
                        //Add a marker
                        MarkerOptions opt = new MarkerOptions().position(pubLocation).title((String) data.get(DBManager.CollectionsPaths.PubFields.NAME));
                        mMap.addMarker(opt);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        mMap.setOnInfoWindowClickListener(this);
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this.getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if(mLastKnownLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }else{
                                Log.d(TAG, "Current location is null. Using defaults.");
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }else{
                Log.d(TAG, "mLocationPermissionGranted FALSE");
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            Log.d(TAG, "Map null");
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                Log.d(TAG, "Permission Granted");
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                Log.d(TAG, "Permission NOT Granted");
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e(TAG+": %s", "Exception: "+e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Log.d(TAG, "getLocPerm: true");
        } else {
            this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            mLocationPermissionGranted = false;
            Log.d(TAG, "getLocPerm: false");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        Log.d(TAG, "onReqPerm");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                Log.d(TAG, "onReqPerm case");
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onReqPerm >0");
                    mLocationPermissionGranted = true;
                }
                mLocationPermissionGranted = true;  }
        }
        updateLocationUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(true);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                FindPubsFragmentDirections.ActionFindPubToPubsList action = FindPubsFragmentDirections.actionFindPubToPubsList(false);
                View navController = getActivity().findViewById(R.id.nav_host_fragment);
                Navigation.findNavController(navController).navigate(action);
                return false;
            }
        });

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Log.d("OnBackPressed",this.getClass().getSimpleName());
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        progressBar.setVisibility(View.VISIBLE);
        final String pubName = marker.getTitle();
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        final Pub pub = new Pub();
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, document.getId() + " => " + data);
                        String tmp = (String)data.get(DBManager.CollectionsPaths.PubFields.NAME);
                        if(pubName.equals(tmp)){
                            FindPubsFragmentDirections.ActionOpenPubFragment action = FindPubsFragmentDirections.actionOpenPubFragment(document.getId());
                            View navController = FindPubsFragment.this.getActivity().findViewById(R.id.nav_host_fragment);
                            Navigation.findNavController(navController).navigate(action);
                        }
                    }
                }
            }});
    }
}
