package com.nat.mymapproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.OnConnectionFailedListener, ClusterManager.OnClusterClickListener<MyItem> {


    public static final String DEFAULT_NAME = "USER";
    private GoogleMap googleMap;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private FirebaseDatabase databaseTest;
    private DatabaseReference myRefTest;

    private DatabaseReference mRefPrivate;
    private static final String TAG = "lifecycle";

    private MarkInfo markInfo;

    int countMarker = 0;
    static int cat = 0;

    private ClusterManager<MyItem> mClusterManager;
    private List<LatLng> mMarker = new ArrayList<>();

    private String userName;
    private String userEmail;
    static private String userID;
    private String userPhoto;

    public static final int PRIVATE = 1;
    public static final int PUBLIC = 0;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirechatUser;

    private String mUsername;
    private String mPhotoUrl;




    //https://www.youtube.com/watch?v=mPOhnTnLcSY
    //https://firebase.google.com/docs/storage/android/start
    //https://www.quora.com/How-do-I-store-and-load-images-with-Firebase-Storage
    // http://pmarshall.me/2016/02/20/image-storage-with-firebase.html


    ValueEventListener updateMarker = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            DataSnapshot dbPublic = dataSnapshot.child("public");
            getDataMarker((Map<String, Object>) dbPublic.getValue(), PUBLIC);

            if (userID != null) {

                DataSnapshot dbPrivate = dataSnapshot.child("private");

                for (DataSnapshot child : dbPrivate.getChildren()) {

                    if (userID.equals(child.getKey().toString())) {
                        getDataMarker((Map<String, Object>) child.getValue(), PRIVATE);
                    }
                }
            }

            //getDataMarker((Map<String, Object>) dataSnapshot.getValue());

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            //Toast.makeText(getApplicationContext(), "Faled to read " + databaseError.toException(), Toast.LENGTH_SHORT).show();
        }
    };

    private void getDataMarker(Map<String, Object> data, int accessCode) {

        //ArrayList<Double> dataMarker = new ArrayList<>();
        // му даты есть размер, т.е. можно если что сверять нам массив с этим по размерам
        for (Map.Entry<String, Object> entry : data.entrySet()) {

            Map singleData = (Map) entry.getValue();

            if (singleData.size() == 1) {
                break;
            }
            //dataMarker.add((Double) singleUser.get("longitude"));

            double lat = (Double) singleData.get("latitude");
            double lng = (Double) singleData.get("longitude");
            //String title = (String) singleData.get("titel");
            //String text = (String) singleData.get("text");

            String title = "Cat";
            String text = ("LatLng: " + lat + "; " + lng);

            LatLng latlng = new LatLng(lat, lng);

            if (accessCode == PRIVATE) {
                addmarkers(latlng, PRIVATE);
            } else {
                addmarkers(latlng, PUBLIC);
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MapActivity onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //myRef.addValueEventListener(updateMarker);
        Log.d(TAG, "MapActivity onResume");


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        myRef.addValueEventListener(updateMarker);
        Log.d(TAG, "MapActivity onRestart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "MapActivity onCreate");

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("map");
        myRef.addValueEventListener(updateMarker);

        myRefTest = FirebaseDatabase.getInstance().getReference("map").child("private");

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppIndex.API).build();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirechatUser = mFirebaseAuth.getCurrentUser();

        if (mFirechatUser == null) {
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirechatUser.getDisplayName();
            userID = mFirechatUser.getUid();
            userName = mFirechatUser.getDisplayName();
            if (mFirechatUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirechatUser.getPhotoUrl().toString();
            }
        }

        //setUpClusterer();


        // mRefPrivate = database.getReference("map").child("private");
        // mRefPrivate.addValueEventListener(updateMarker);


    }

    private void setUpClusterer() {
        // Position the map.
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));
        setUpMapIfNeeded();

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(this, googleMap);
        mClusterManager.setRenderer(new MyClusterRenderer(this, googleMap,
                mClusterManager));

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        googleMap.setOnCameraIdleListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        //googleMap.setOnMapLongClickListener((GoogleMap.OnMapLongClickListener) mClusterManager);
        //googleMap.setOnMapLoadedCallback((GoogleMap.OnMapLoadedCallback) mClusterManager);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem item) {

                Intent intent = new Intent(MapsActivity.this, InfoActivity.class);
                intent.putExtra("accessCode", item.getAccessCode());
                // это мы перешлём юсера который зашёл а не тот который записал инфу
                // intent.putExtra("userName", userName);

                intent.putExtra("userID", userID);
                intent.putExtra("latitude", item.mPosition.latitude);
                intent.putExtra("longitude", item.mPosition.longitude);
                startActivity(intent);

                //put your code here
                return false;
            }
        });

        // Add cluster items (markers) to the cluster manager.
        //addItems();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setOnMyLocationButtonClickListener(this);
        myLocation();

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        googleMap.setOnMapClickListener(this);
        googleMap.setOnMapLongClickListener(this);
        googleMap.setInfoWindowAdapter(this);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);

        setUpClusterer();
        mClusterManager.setOnClusterClickListener(this);
    }


    private void myLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

        } else {
            // Show rationale and request permission.
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        /*
        myRef.getRef().push();
        String key = myRef.getRef().push().getKey();

        myRef.child(key).setValue(latLng);

        Toast.makeText(getApplicationContext(), "Data write database", Toast.LENGTH_SHORT).show();
        */
    }



    @Override
    public void onMapLongClick(final LatLng latLng) {

        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), googleMap.getCameraPosition().zoom+1));

        //myRef.getRef().push();
       //String key = myRef.getRef().push().getKey();
        String address = "";
        address = getAddress(latLng.latitude, latLng.longitude);

        markInfo = new MarkInfo(latLng, userName, address);
        String key = "";//markInfo.hashKey(latLng);


        String mm = String.valueOf(latLng.longitude);
        String mm1 = String.valueOf(latLng.latitude);
        mm = mm.replace('.', '-');
        mm1 = mm1.replace('.', '-');


        key = mm + mm1;

        /*myRef.child("private").child(userID).child(key).setValue(latLng);
        myRef.child("private").child(userID).child(key).child("title").setValue("title");
        myRef.child("private").child(userID).child(key).child("text").setValue("text");
        myRef.child("private").child(userID).child(key).child("address").setValue(address);
        myRef.child("private").child(userID).child(key).child("user_name").setValue(userName);*/


        DatabaseReference upvotesRef = myRef.child("private").child(userID).child(key);
        final String finalAddress = address;
        upvotesRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                /*Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }*/

                mutableData.setValue(latLng);
                mutableData.child("title").setValue("title");
                mutableData.child("text").setValue("text");
                mutableData.child("address").setValue(finalAddress);
                mutableData.child("user_name").setValue(userName);

                return Transaction.success(mutableData);

                //mutableData.setValue(markInfo);
                //return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                System.out.println("Transaction completed");
            }
        });

        //myRef.child("private").child(userID).child(key).child("user_ID").setValue(userID);
        //myRef.child("private").child(userID).child(key).child("user_photo").setValue(userPhoto);

        // Для публичных маркеров
        /*
        myRef.child("public").child(key).setValue(latLng);
        myRef.child("public").child(key).child("title").setValue("title");
        myRef.child("public").child(key).child("text").setValue("text");
        myRef.child("public").child(key).child("adress").setValue(address);
        */

        //Toast.makeText(getApplicationContext(), "Data write database", Toast.LENGTH_SHORT).show();
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), googleMap.getCameraPosition().zoom+1));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), googleMap.getCameraPosition().zoom-1));
    }

    private void addmarkers(LatLng latLng, int accessCode) {

        if(mMarker.size() != 0){
            boolean flag = true;

            for(LatLng mLanLng : mMarker){
                if (mLanLng.equals(latLng)){
                    flag = false;
                    break;
                }
            }

            if (flag){
                mMarker.add(latLng);
                /*Marker m = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        //.draggable(false)
                        .snippet(text)
                        .draggable(false)
                        //.alpha((float) 0.3)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));*/
                MyItem item = new MyItem(latLng, accessCode);
                //item.
                mClusterManager.addItem(item);
            }

        } else {
            mMarker.add(latLng);
            /*Marker m = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    //.draggable(false)
                    .snippet(text)
                    .draggable(false)
                    //.alpha((float) 0.3)

                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));*/
            mClusterManager.addItem(new MyItem(latLng, accessCode));
        }
        mClusterManager.cluster();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                googleMap.clear();
                mClusterManager.clearItems();
                mMarker.clear();

                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = DEFAULT_NAME;
                startActivity(new Intent(this, AuthenticationActivity.class));
                return true;
                //Intent intent = new Intent(this, AuthenticationActivity.class);
                //startActivityForResult(intent, 1);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
       /* marker.setAlpha(0.5f);*/
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra("latitude", marker.getPosition().latitude);
        intent.putExtra("longitude", marker.getPosition().longitude);
        startActivity(intent);
        //Toast.makeText(this, "Click Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) {

        //String firstName = cluster.getItems().iterator().next().name;
        ///Toast.makeText(this, cluster.getSize() + " (including ", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;

    }

    public String getAddress(double lat, double lng) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        //Locale aLocale = new Builder().setLanguage("ru").build();

        //Geocoder geocoder = new Geocoder(this, aLocale);
        String s = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() != 0) {
                Address mAddress = addresses.get(0);

                for (int i = 0; i <= mAddress.getMaxAddressLineIndex(); i++) {

                    s += mAddress.getAddressLine(i) + ", ";
                }
                //Toast.makeText(getApplicationContext(), s , Toast.LENGTH_SHORT).show();

                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                // street = addresses.get(0).nam;

                String mStreet = addresses.get(0).getThoroughfare();

                //Tooast.makeText(getApplicationContext(), mStreet, Toast.LENGTH_SHORT).show();


                //Toast.makeText(getApplicationContext(), city + ", " + state + ", " + country + ", " + postalCode + ", " + knownName, Toast.LENGTH_SHORT).show();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;

    }


















    /*
        //addmarkers(latLng);
        //Toast.makeText(getApplicationContext(), "Long Click" + latLng, Toast.LENGTH_SHORT).show();

        myRef.push().child("user");
        myRef.child("user").child("name").setValue("Nat");
        myRef.child("user").child("data").setValue(latLng);

        /*if(countMarker == 1){
            myRef.child("user").child("data1").setValue(latLng);
        }
        else {
            myRef.child("user").child("data").setValue(latLng);
        }
        countMarker ++;


        Toast.makeText(getApplicationContext(), "Data write database", Toast.LENGTH_SHORT).show();
        */



    //addmarkers(latLng, 1);

    //Toast.makeText(getApplicationContext(), "Click" + latLng, Toast.LENGTH_SHORT).show();

    //myRef.getRef().child("user");
       /* myRef.child("user").push().child("ID").setValue(accountID);
        myRef.child("user").child(accountID[0]).child("name").setValue(accountID[1]);
        myRef.child("user").child(accountID[0]).child("mark_data").setValue(latLng);*/



    /*String name0 = "any";
            //String name0 = dataSnapshot.child("user").child(accountID[0]).child("name").getValue(String.class);
            double lat0 = dataSnapshot.child("user").child(accountID[0]).child("mark_data").child("latitude").getValue(double.class);
            double lng0 = dataSnapshot.child("user").child(accountID[0]).child("data").child("longitude").getValue(double.class);

            LatLng latlng0 = new LatLng(lat0, lng0);

            addmarkers(latlng0, name0);*/

    // public -> data_#

    // http://stackoverflow.com/questions/38965731/how-to-get-all-childs-data-in-firebase-database


   // getDataMarker((Map<String,Object>) dataSnapshot.getValue());


            /*String name = dataSnapshot.child("user").child("name").getValue(String.class);
            double lat = dataSnapshot.child("user").child("data1").child("latitude").getValue(double.class);
            double lng = dataSnapshot.child("user").child("data1").child("longitude").getValue(double.class);

            LatLng latlng = new LatLng(lat, lng);

            addmarkers(latlng, name);

            //Toast.makeText(getApplicationContext(), name + "; LatLng: " + lat + "; " + lng, Toast.LENGTH_SHORT).show();

            String name1 = dataSnapshot.child("user1").child("name").getValue(String.class);
            double lat1 = dataSnapshot.child("user1").child("data").child("latitude").getValue(double.class);
            double lng1 = dataSnapshot.child("user1").child("data").child("longitude").getValue(double.class);

            LatLng latlng1 = new LatLng(lat1, lng1);

            addmarkers(latlng1, name1);*/



     /*String name0 = "any";
            //String name0 = dataSnapshot.child("user").child(accountID[0]).child("name").getValue(String.class);
            double lat0 = dataSnapshot.child("user").child(accountID[0]).child("mark_data").child("latitude").getValue(double.class);
            double lng0 = dataSnapshot.child("user").child(accountID[0]).child("data").child("longitude").getValue(double.class);

            LatLng latlng0 = new LatLng(lat0, lng0);

            addmarkers(latlng0, name0);*/

    // public -> data_#

    // http://stackoverflow.com/questions/38965731/how-to-get-all-childs-data-in-firebase-database


    //dataMarker((Map<String,Object>) dataSnapshot.getValue());


            /*String name = dataSnapshot.child("user").child("name").getValue(String.class);
            double lat = dataSnapshot.child("user").child("data1").child("latitude").getValue(double.class);
            double lng = dataSnapshot.child("user").child("data1").child("longitude").getValue(double.class);

            LatLng latlng = new LatLng(lat, lng);

            addmarkers(latlng, name);

            //Toast.makeText(getApplicationContext(), name + "; LatLng: " + lat + "; " + lng, Toast.LENGTH_SHORT).show();

            String name1 = dataSnapshot.child("user1").child("name").getValue(String.class);
            double lat1 = dataSnapshot.child("user1").child("data").child("latitude").getValue(double.class);
            double lng1 = dataSnapshot.child("user1").child("data").child("longitude").getValue(double.class);

            LatLng latlng1 = new LatLng(lat1, lng1);

            addmarkers(latlng1, name1);*/










    //    @Override
    /*protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }
        String accountID = data.getStringExtra("name");
        Toast.makeText(getApplicationContext(), accountID, Toast.LENGTH_SHORT).show();

        //return;
        //String name = data.getStringExtra("name");
        //Toast.makeText(getApplicationContext(), accountID, Toast.LENGTH_SHORT).show();
    }*/
}






    /*private Marker addmarkers(LatLng latLng) {

        Marker m;
        m = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Test")
                .draggable(false)
                .snippet("Latlng :" + latLng));
        return m;
    }

    private Marker addmarkers(LatLng latLng, int i) {

        Marker m;
        m = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Test")
                .draggable(false)
                .snippet("Latlng :" + latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        return m;
    }
*/
   /* @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.myBtn:
                myRef.addValueEventListener(updateMarker);
                *//*myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("user").child("name").getValue(String.class);
                        double lat = dataSnapshot.child("user").child("data").child("latitude").getValue(double.class);
                        double lng = dataSnapshot.child("user").child("data").child("longitude").getValue(double.class);

                        LatLng latlng = new LatLng(lat, lng);

                        addmarkers(latlng, name);

                        Toast.makeText(getApplicationContext(), name + "; LatLng: " + lat + "; " + lng, Toast.LENGTH_SHORT).show();

                        String name1 = dataSnapshot.child("user1").child("name").getValue(String.class);
                        double lat1 = dataSnapshot.child("user1").child("data").child("latitude").getValue(double.class);
                        double lng1 = dataSnapshot.child("user1").child("data").child("longitude").getValue(double.class);

                        LatLng latlng1 = new LatLng(lat1, lng1);

                        addmarkers(latlng1, name1);

                        Toast.makeText(getApplicationContext(), name1 + "; LatLng: " + lat1 + "; " + lng1, Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Faled to read " + databaseError.toException(), Toast.LENGTH_SHORT).show();

                    }
                });*//*
                break;
            default:
                break;
        }
    }*/


 /* myRef.child("user").child("name").setValue("new name");
        myRef.child("user").child("data").setValue("new data");

        myRef.getRef().push();

        myRef.child("user1").child("name").setValue("next name");
        myRef.child("user1").child("data").setValue("next data");*/

//{
            /*@Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("user").child("name").getValue(String.class);
                double lat = dataSnapshot.child("user").child("data").child("latitude").getValue(double.class);
                double lng = dataSnapshot.child("user").child("data").child("longitude").getValue(double.class);

                LatLng latlng = new LatLng(lat, lng);

                addmarkers(latlng, name);


                //Toast.makeText(getApplicationContext(), name + "; LatLng: " + lat + "; " + lng, Toast.LENGTH_SHORT).show();

                String name1 = dataSnapshot.child("user1").child("name").getValue(String.class);
                double lat1 = dataSnapshot.child("user1").child("data").child("latitude").getValue(double.class);
                double lng1 = dataSnapshot.child("user1").child("data").child("longitude").getValue(double.class);

                LatLng latlng1 = new LatLng(lat1, lng1);

                addmarkers(latlng1, name1);

                //Toast.makeText(getApplicationContext(), name1 + "; LatLng: " + lat1 + "; " + lng1, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Faled to read " + databaseError.toException(), Toast.LENGTH_SHORT).show();

            }
        });
    }*/



/*
//myRef.addValueEventListener(mapListener);

    ValueEventListener mapListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String name = dataSnapshot.child("user").child("name").getValue(String.class);
            double lat = dataSnapshot.child("user").child("data").child("latitude").getValue(double.class);
            double lng = dataSnapshot.child("user").child("data").child("longitude").getValue(double.class);

            LatLng latlng = new LatLng(lat, lng);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(), "Faled to read " + databaseError.toException(), Toast.LENGTH_SHORT).show();
        }
    };

        myRef.addValueEventListener(mapListener);*/

/*private Marker addmarkers(double latitude, double longitude) {

        Marker m;
        m = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Test")
                .draggable(false)
        );

        return m;
    }*/





//database = FirebaseDatabase.getInstance();
//myRef = database.getReference("map");

//https://www.firebase.com/docs/android/guide/saving-data.html
//DatabaseReference ref = FirebaseDatabase.getInstance().getReference("map").child("public");

// http://stackoverflow.com/questions/40436277/how-to-remove-specific-marker-on-android-googlemap
// https://developers.google.com/maps/documentation/javascript/3.exp/reference#Marker




// https://stackoverflow.com/questions/13756261/how-to-get-the-current-location-in-google-maps-android-api-v2
// в примере еще класс был какой то абстактный, его похоже нужно добавить,
// я так поняла он пермишены обрабатывает
// https://stackoverflow.com/questions/35521756/error-package-permissionutils-does-not-exist

// PLACE PICKER посмотреть обязательно!!!!!!!
//https://habrahabr.ru/post/270217/

//http://www.androidhive.info/2015/02/android-location-api-using-google-play-services/


/*
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();



        PendingResult < PlaceLikelihoodBuffer > result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                }
                likelyPlaces.release();
            }
        });
*/
//PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        /*try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        */


        /*
         Toast.makeText(getApplicationContext(), addresses.get(0).getAdminArea(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), addresses.get(0).getCountryCode(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), addresses.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), addresses.get(0).getFeatureName(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), addresses.get(0).getLocality().toString(), Toast.LENGTH_SHORT).show();

                    Toast.makeText(getApplicationContext(), addresses.get(0).getPhone(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), addresses.get(0).getUrl(), Toast.LENGTH_SHORT).show();

                    Toast.makeText(getApplicationContext(), addresses.get(0).getSubAdminArea(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), addresses.get(0).getSubLocality(), Toast.LENGTH_SHORT).show();

                    Toast.makeText(getApplicationContext(), addresses.get(0).getSubThoroughfare(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), addresses.get(0).getThoroughfare(), Toast.LENGTH_SHORT).show();

                    Toast.makeText(getApplicationContext(), addresses.get(0).getPostalCode(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), addresses.get(0).getPremises(), Toast.LENGTH_SHORT).show();

                    Toast.makeText(getApplicationContext(), addresses.get(0).getLocale().toString(), Toast.LENGTH_SHORT).show();

                    Toast.makeText(getApplicationContext(), addresses.get(0).getExtras().toString(), Toast.LENGTH_SHORT).show();
         */