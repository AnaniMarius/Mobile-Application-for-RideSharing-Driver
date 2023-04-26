package ro.ananimarius.allridev3.ui.home;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.gson.JsonObject;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ro.ananimarius.allridev3.Common.DirectionsApiService;
import ro.ananimarius.allridev3.Common.DirectionsResponse;
import ro.ananimarius.allridev3.Common.Notification;
import ro.ananimarius.allridev3.Common.RetrofitClient;
import ro.ananimarius.allridev3.DriverHomeActivity;
import ro.ananimarius.allridev3.Functions;
import ro.ananimarius.allridev3.R;
import ro.ananimarius.allridev3.SplashScreenActivity;
import ro.ananimarius.allridev3.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener,GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;
    private FragmentHomeBinding binding;


    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    SupportMapFragment mapFragment;
    Double latitude;
    Double longitude;
    String authToken;
    String idToken;
    String email;


    //routers
    private CompositeDisposable compositeDisposable=new CompositeDisposable();
    private PolylineOptions black;

    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
        // Stop the periodic background task of checking the notifications
        handler.removeCallbacks(notificationsRunnable);
        super.onDestroy();
    }


    public interface APIInterface {
        @FormUrlEncoded
        @POST("user/updateLocation")
        Call<JsonObject> updateLocation(   @Field("authToken") String authToken,
                                           @Field("idToken") String googleId,
                                           @Field("latitude") double latitude,
                                           @Field("longitude")double longitude);

        @GET("user/getRequestFromCustomerNotification")
        Call<Notification> getRequestFromCustomerNotification(@Query("authToken") String authToken,
                                                  @Query("idToken") String idToken);
    }
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    HomeFragment.APIInterface api = retrofit.create(HomeFragment.APIInterface.class);
    GoogleSignInAccount account;

    private void init() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 14f));

                latitude=newPosition.latitude;
                longitude=newPosition.longitude;
                Functions func=new Functions();
                authToken=func.getAuthTokenCookie();
                authToken=func.parseCookie(authToken);
                //authToken=null;
                //send the location to the api
                Call<JsonObject> call = api.updateLocation(authToken, idToken, latitude, longitude);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            //finish();
                        } else {
                            Toast.makeText(getContext(), "UpdateLocationError: " + response.code()+"+"+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        int statusCode = 0;
                        String errorMessage = "";

                        if (t instanceof HttpException) {
                            HttpException httpException = (HttpException) t;
                            Response response = httpException.response();
                            statusCode = response.code();
                            errorMessage = response.message();
                        } else {
                            errorMessage = t.getMessage();
                        }
                        Toast.makeText(getContext(), "Error, Status code: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //receive the google account
        if (getArguments() != null) {
            idToken = getArguments().getString("googleId");
            email = getArguments().getString("email");
//            Toast.makeText(getContext(), email, Toast.LENGTH_SHORT).show();
            if (idToken != null && email != null) {
                //use the Google account information
            }
        }
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        init();

        //pasted from mapActivity
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //instantiate the mGeoApiContext for the directions api
        if(mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_api_key))
                    .build();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        //check permission
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(e -> Toast.makeText(getContext(),"Error to get location: "+e.getMessage(),Toast.LENGTH_SHORT).show())
                                    .addOnSuccessListener(location -> {
                                        LatLng userLatLng=new LatLng(location.getLatitude(),location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f));
                                    });
                            return true;
                        });

                        //set location button
                        View locationButton=((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        //place it to right bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                        params.setMargins(0,0,0,50);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(),"Permission "+permissionDeniedResponse.getPermissionName()+""+
                                " was denied!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

        try{
            boolean success=googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.uber_maps_style));
            if(!success){
                Log.e("ERROR","Style parsing error");
            }
        }catch(Resources.NotFoundException e){
            Log.e("ERROR",e.getMessage());
        }

        mMap.setOnPolylineClickListener(this);//all the polyline clicks will be intercepted by the method
    }


    //interface appear when the driver gets a request
    @BindView(R.id.chip_decline)
    Chip chip_decline;
    @BindView(R.id.layout_accept)
    CardView layout_accept;
    @BindView(R.id.circularProgressBar)
    CircularProgressBar circularProgressBar;
    @BindView(R.id.txt_estimate_time)
    TextView txt_estimate_time;
    @BindView(R.id.txt_estimate_distance)
    TextView txt_estimate_distance;
    @BindView(R.id.countDown)
    TextView countDown;

    //CHECK THE NOTIFICATIONS
    private Handler handler = new Handler();
    private int delay = 1000; // 5 seconds in milliseconds
    private Runnable notificationsRunnable = new Runnable() {
        @Override
        public void run() {
            checkNotifications();
            handler.postDelayed(this, delay);
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start the periodic background task
        handler.postDelayed(notificationsRunnable, delay);
    }


    List<Notification> notifications=new ArrayList<>();
    MarkerOptions destinationMarkerOptions;
    Marker destinationMarker;
    MarkerOptions customerMarkerOptions;
    Marker customerMarker;
    private void checkNotifications() {
        Call<Notification> call = api.getRequestFromCustomerNotification(authToken, idToken);
        call.enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                if (response.isSuccessful()) {
                    Notification notification = response.body();
                    Chip chipDecline = getView().findViewById(R.id.chip_decline);
                    CardView layoutAccept = getView().findViewById(R.id.layout_accept);

                    if (notification != null && chipDecline != null && layoutAccept != null) {
                        //make the interface visible
                        chipDecline.setVisibility(View.VISIBLE);
                        layoutAccept.setVisibility(View.VISIBLE);

                        //add a marker to the customer
                        LatLng customerPosition = new LatLng(notification.getCustLatitude(), notification.getCustLongitude());
                        customerMarkerOptions = new MarkerOptions()
                                .position(customerPosition)
                                .title("Customer")
                                //.snippet("Customer's Destination")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        customerMarker = mMap.addMarker(customerMarkerOptions);
                        //compute the directions between the driver and the customer
                        calculateDirections(customerMarker, latitude, longitude, R.color.teal_700);

                        //add a marker to the destination
                        LatLng destinationPosition = new LatLng(notification.getDestLatitude(), notification.getDestLongitude());
                        destinationMarkerOptions = new MarkerOptions()
                                .position(destinationPosition)
                                .title("Destination")
                                //.snippet("Customer's Destination")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        destinationMarker = mMap.addMarker(destinationMarkerOptions);
                        //compute the directions between the customer and the destination
                        calculateDirections(destinationMarker, notification.getCustLatitude(), notification.getCustLongitude(), R.color.purple_700);
                        Toast.makeText(getContext(), "Request from: " + notification.getCustomerFirstName(), Toast.LENGTH_SHORT).show();
                        notifications.add(notification);
                    }
                    else if(chipDecline != null && layoutAccept != null){
                        chipDecline.setVisibility(View.GONE);
                        layoutAccept.setVisibility(View.GONE);
                    }
                    removeNotifications();
                }
                else{
                    Toast.makeText(getContext(), "CheckNotificationError: " + response.code()+"+"+response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Notification> call, Throwable t) {
                // Handle the error
            }
        });
    }


    private void removeNotifications() {
        TimerTask removeExpiredNotificationsTask = new TimerTask() {
            private int progress = 0;
            long timePassed;
            Notification nextN = null;
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                List<Notification> notificationsCopy = new ArrayList<>(notifications);
                for (Notification notification : notificationsCopy) {
                    nextN = notification;
                    timePassed = currentTimeMillis - nextN.getTimeCreated();
                    if (timePassed > 6000) {
                        declineRequest(nextN);
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Chip chipDecline = getView().findViewById(R.id.chip_decline);
                        CardView layoutAccept = getView().findViewById(R.id.layout_accept);
                        CircularProgressBar circularProgressBar = getView().findViewById(R.id.circularProgressBar);
                        TextView countdown = getView().findViewById(R.id.countDown);

                        //if the decline is pressed, remove the interface
                        chipDecline.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                notifications.clear();
                                chipDecline.setVisibility(View.GONE);
                                layoutAccept.setVisibility(View.GONE);
                                declineRequest(nextN);
                            }
                        });
                        layoutAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!notifications.isEmpty()) {
                                    Notification notification = notifications.get(0);
                                    //acceptRequest(notification);
                                    chipDecline.setVisibility(View.GONE);
                                    layoutAccept.setVisibility(View.GONE);
                                    circularProgressBar.setProgress(0f);
                                    progress = 0;
                                }
                            }
                        });


                        if (!notifications.isEmpty()) {
                            // Display countdown from 6 to 0 seconds
                            int secondsLeft = 6 - (int) ((currentTimeMillis - notifications.get(0).getTimeCreated()) / 1000);
                            countdown.setText(String.valueOf(secondsLeft));
                            circularProgressBar.setProgress((float) progress / 100);
                            progress++;
                            if (secondsLeft <= 0) {
                                Toast.makeText(getContext(),"Accept Action",Toast.LENGTH_SHORT).show();
                                notifications.clear();
                                chipDecline.setVisibility(View.GONE);
                                layoutAccept.setVisibility(View.GONE);
                                circularProgressBar.setProgress(0f);
                                progress = 0;
                            }
                        } else {
                            chipDecline.setVisibility(View.GONE);
                            layoutAccept.setVisibility(View.GONE);
                            circularProgressBar.setProgress(0f);
                            progress = 0;
                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(removeExpiredNotificationsTask, 0, 100); // run every 100 milliseconds
    }

    private void declineRequest(Notification notification) {
        notifications.remove(notification);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                destinationMarker.remove();
                customerMarker.remove();
                Toast.makeText(getContext(), "The request has been declined", Toast.LENGTH_SHORT).show();
            }
        });
        /////////CALL SOME KIND OF REQUEST DECLINED REQUEST
    }

    //map directions api
    private GeoApiContext mGeoApiContext=null;
    private void calculateDirections(Marker marker, Double auxLatitude, Double auxLongitude, int polyLineColor){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        //mUserPosition.getGeo_point().getLatitude(),
                        //mUserPosition.getGeo_point().getLongitude()
                        auxLatitude,
                        auxLongitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result, polyLineColor);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }
    private void addPolylinesToMap(final DirectionsResult result, int polyLineColor){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }

                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), polyLineColor));
                    polyline.setClickable(true);

                }
            }
        });
    }
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

//    String custLoc;
//    String destLoc;
//    private void acceptRequest(Notification notification) {
//
//        //SAVE THE INFORMATION OF THE ACCEPTED NOTIFICATION
//        custLoc = notification.getCustLatitude() + "," + notification.getCustLongitude();
//        destLoc = notification.getDestLatitude() + "," + notification.getDestLongitude();
//        DirectionsApiService directionsApiService = RetrofitClient.getDirectionsApiService();
//        directionsApiService.getDirections("driving", "", custLoc, destLoc, "YOUR_API_KEY")
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<DirectionsResponse>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(DirectionsResponse directionsResponse) {
//                        // Handle the response from the API here
//                        // The directionsResponse object contains the information returned from the API
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        // Handle the error here
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//
//
//        notifications.clear();
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getContext(), "The request has been accepted", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Call the appropriate API endpoint to accept the request here
//    }

}