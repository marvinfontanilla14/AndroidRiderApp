package com.example.khean07.androidriderapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.khean07.androidriderapp.Common.Common;
import com.example.khean07.androidriderapp.Helper.CustomInfoWindow;
import com.example.khean07.androidriderapp.Model.Notification;
import com.example.khean07.androidriderapp.Model.FCMResponse;
import com.example.khean07.androidriderapp.Model.Rider;
import com.example.khean07.androidriderapp.Model.Sender;
import com.example.khean07.androidriderapp.Model.Token;
import com.example.khean07.androidriderapp.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    SupportMapFragment mapFragment;

    private static final String TAG = "MARVDEV";
    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICE_RES_REQUEST = 300193;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    FusedLocationProviderClient mFusedLocationClient;

    DatabaseReference ref;
    GeoFire geoFire;
    Marker mUserMarker;
//    MaterialAnimatedSwitch location_switch;

    private Runnable mAnimation;


    // Car animation
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition, endPosition,currentPosition;
    private int index, next;
    //    private Button btnGo;
    private PlaceAutocompleteFragment places;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;

//    private IGoogleAPI mService;

    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnPickupRequest;

    boolean isDriverFound=false;
    String driverId="";
    int radius = 1; //1km
    int distance = 1; //1km
    private static final int LIMIT = 3;

    //Send Alert
    IFCMService mService;

    //Presense System
    DatabaseReference driversAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getFCMService();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Maps
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        imgExpandable = (ImageView) findViewById(R.id.imgExpandable);
        mBottomSheet = BottomSheetRiderFragment.newInstance("Rider bottom sheet");
        imgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());
            }
        });

        btnPickupRequest = (Button) findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isDriverFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToDriver(driverId);

            }
        });

        setUpLocation();
        updateFirebaseToken();


    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getInstance().getCurrentUser().getUid()).setValue(token);
    }

    private void sendRequestToDriver(String driverId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        tokens.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
                    Token token = postSnapShot.getValue(Token.class); // Get Token object from dataase with key

                    // make raw payload - convert latling to json
                    String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    Notification notification = new Notification("EDMTDEV",json_lat_lng); // send it to Driver app and we will deserialize it again
                    Sender content = new Sender(notification, token.getToken()); // Send this notification to token

                    mService.sendMessage(content)
                            .enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if(response.body().success == 1)
                                        Toast.makeText(Home.this,"Request sent!", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(Home.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                   Log.e("Error", t.getMessage());
                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

        if(mUserMarker.isVisible())
            mUserMarker.remove();

        //Add new marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
            .title("Pickup Here")
            .snippet("")
            .position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mUserMarker.showInfoWindow();

        btnPickupRequest.setText("Getting you DRIVER...");

        findDriver();

    }

    private void findDriver() {
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gfDrivers = new GeoFire(drivers);

        GeoQuery geoQuery = gfDrivers.queryAtLocation(
                new GeoLocation(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude()),
                radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!isDriverFound) {
                    isDriverFound = true;
                    driverId = key;
                    btnPickupRequest.setText("CALL DRIVER");
                    Toast.makeText(Home.this,""+key,Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!isDriverFound){
                    radius++;
                    findDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPlayService()){
                        buildGoogleApiClient();
                        createLocationRequest();
//                        if(location_switch.isChecked()){
                            displayLocation();
//                        }
                    }
                }
                break;
        }

    }

    private void setUpLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);

        } else {
            if(checkPlayService()){
                buildGoogleApiClient();
                createLocationRequest();
//                if(location_switch.isChecked()){
                    displayLocation();
//                }
            }
        }

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {

                            //Presense System
                            driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
                            driversAvailable.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // if have any change from Driver table., we will reload all drivers available
                                    loadAllAvailableDriver();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

//                            onLocationChanged(location);
                            mLastLocation = location;

//                            if(location_switch.isChecked()) {
                            final double latitude = mLastLocation.getLatitude();
                            final double longitude = mLastLocation.getLongitude();


                            if (mUserMarker != null) {
                                mUserMarker.remove();
                            }
                            mUserMarker = mMap.addMarker(new MarkerOptions()
//                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.head_1_64x64))
                                    .position(new LatLng(latitude, longitude))
                                    .title("You"));

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
//                                        rotateMarker(mCurrent,0,mMap);
                            loadAllAvailableDriver();
//                            }
                        } else {
                            Log.d("ERROR", "Cannot get your location");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });


    }

    private void loadAllAvailableDriver() {

        //First, we need delete all markers on map (include our location marker and available driver marker)
        mMap.clear();
        //after that, just add our location again
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                .title("You"));

        // Load all available Driver in distance 3km
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gf = new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(
                new GeoLocation(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude()),
                distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //Use key to get email from table users
                //Table users is table when driver register account and update unformation
                //just open your driver to check this table name

                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Because rider and user model is same properties
                                //So we can use Rider model to get User here
                                Rider rider = dataSnapshot.getValue(Rider.class);

                                //Add driver to map
                                mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.latitude,location.longitude))
                                .flat(true)
                                .title(rider.getName())
                                .snippet("Phone: "+rider.getPhone())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.head_1_64x64)));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(distance <= LIMIT){
                    distance++;
                    loadAllAvailableDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));


   }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MainActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
            }
        }

        ;

    };

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
