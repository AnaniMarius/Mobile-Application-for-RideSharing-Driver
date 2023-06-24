package ro.ananimarius.allridev3.ui.home;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
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
import com.google.maps.model.Duration;
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
import java.util.Set;
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
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
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
import ro.ananimarius.allridev3.Common.PolylineData;
import ro.ananimarius.allridev3.Common.RetrofitClient;
import ro.ananimarius.allridev3.Common.RideDTO;
import ro.ananimarius.allridev3.Common.UnsafeOkHttpClient;
import ro.ananimarius.allridev3.Common.WaypointDTO;
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

    Button endRideBtn;
    Button cancelRideBtn;

    Boolean cancelRide=false;
    Boolean endRide=false;
    public void toggleRideButtons(){
        endRideBtn = getView().findViewById(R.id.end_ride_btn);
        cancelRideBtn = getView().findViewById(R.id.cancel_ride_btn);
        if(activeRide==true){
            endRideBtn.setVisibility(View.VISIBLE);
            cancelRideBtn.setVisibility(View.VISIBLE);
        }
        else {
            endRideBtn.setVisibility(View.GONE);
            cancelRideBtn.setVisibility(View.GONE);
        }
    }


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

        @FormUrlEncoded
        @POST("user/changeTheStatusOfTheRequest")
        Call<RideDTO> changeTheStatusOfTheRequest(   @Field("authToken") String authToken,
                                            @Field("idToken") String googleId,
                                            @Field("status") boolean status,
                                            @Field("customerId") String customerId,
                                            @Field ("destLatitude") double destLatitude,
                                            @Field ("destLongitude") double destLongitude);

        @FormUrlEncoded
        @POST("user/checkCurrentRide")
        Call<RideDTO> checkCurrentRide(   @Field("authToken") String authToken,
                                          @Field("driverId") String driverId,
                                          @Field("customerId") String customerId,
                                          @Field("endRide") boolean endRide,
                                          @Field("cancelRide") boolean cancelRide);
    }
//    Retrofit retrofit = new Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8080")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
    OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
    Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("http://192.168.1.219:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit = builder.build();
    HomeFragment.APIInterface api = retrofit.create(HomeFragment.APIInterface.class);
    
    GoogleSignInAccount account;

    private void init() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 17f));

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
                            Toast.makeText(getContext(), response.body().toString(), Toast.LENGTH_SHORT).show();
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

    private Handler handlerCheckCurrentRide;
    private Runnable runnableCheckCurrentRide;
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

        try{
            endRideBtn=root.findViewById(R.id.end_ride_btn);
            endRideBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endRide=true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            cancelRideBtn=root.findViewById(R.id.cancel_ride_btn);
            cancelRideBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelRide=true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        //continuously calling the checkCurrentRide endpoint to check if there is a ride active

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
                        mMap.getUiSettings().setZoomControlsEnabled(true);
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
//                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
//                        params.setMargins(0,0,0,50);
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

        handlerCheckCurrentRide = new Handler();
        runnableCheckCurrentRide = new Runnable() {
            @Override
            public void run() {
                checkCurrentRide();
                handler.postDelayed(this, 5000); // Schedule the Runnable to run again after 1 second
            }
        };

        handlerCheckCurrentRide.postDelayed(runnableCheckCurrentRide, 0); // Start immediately
    }

    Set<WaypointDTO> route;
    WaypointDTO waypointDTO=new WaypointDTO();
    private void checkCurrentRide(){
        Call<RideDTO> call = api.checkCurrentRide(authToken, idToken,null, endRide, cancelRide);
        call.enqueue(new Callback<RideDTO>() {
            @Override
            public void onResponse(Call<RideDTO> call, Response<RideDTO> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (activeRide == false) {
                            try {
                                //AICI VOI ACTIVA BUTOANELE SA FIE VIZIBILE
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(requireContext(), "The ride is in progress!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ride.setCost(response.body().getCost());
                            ride.setCurrency(response.body().getCurrency());
                            ride.setId(response.body().getId());
                            ride.setDriver(response.body().getDriver());
                            ride.setPassenger(response.body().getPassenger());
                            ride.setRoute(response.body().getRoute());

                            route = ride.getRoute();
                            if (route != null && !route.isEmpty()) {
                                waypointDTO = route.iterator().next();
                                LatLng customerPosition = new LatLng(waypointDTO.getCustomerLatitude(), waypointDTO.getCustomerLongitude());
                                customerMarkerOptions = new MarkerOptions()
                                        .position(customerPosition)
                                        .title("Customer")
                                        //.snippet("Customer's Destination")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                customerMarker = mMap.addMarker(customerMarkerOptions);
                                //compute the directions between the driver and the customer
                                calculateDirections(customerMarker, latitude, longitude, R.color.Blue, 0);
                                //add a marker to the destination
                                LatLng destinationPosition = new LatLng(waypointDTO.getDestinationLatitude(), waypointDTO.getDestinationLongitude());
                                destinationMarkerOptions = new MarkerOptions()
                                        .position(destinationPosition)
                                        .title("Destination")
                                        //.snippet("Customer's Destination")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                destinationMarker = mMap.addMarker(destinationMarkerOptions);
                                //compute the directions between the customer and the destination
                                calculateDirections(destinationMarker, waypointDTO.getCustomerLatitude(), waypointDTO.getCustomerLongitude(), R.color.Red, 1);
                                try{
                                    Toast.makeText(getContext(), "Request from: " + ride.getPassenger().getFirstName(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        ride.setCost(response.body().getCost());
                        ride.setCurrency(response.body().getCurrency());
                        ride.setId(response.body().getId());
                        ride.setDriver(response.body().getDriver());
                        ride.setPassenger(response.body().getPassenger());
                        ride.setRoute(response.body().getRoute());
                        ride.setCustomerCancelsRide(response.body().isCustomerCancelsRide());
                        ride.setDriverCancelsRide(response.body().isDriverCancelsRide());
                        ride.setCustomerEndsRide(response.body().isCustomerEndsRide());
                        ride.setDriverEndsRide(response.body().isDriverEndsRide());
                        ride.setNearCustomer(response.body().isNearCustomer());
                        ride.setNearDestination(response.body().isNearDestination());
                        route = ride.getRoute();


                        activeRide=true;
                        try {
                            toggleRideButtons();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if((ride.isCustomerEndsRide()==true && ride.isDriverEndsRide()==true)||(ride.isCustomerCancelsRide() == true)||(ride.isDriverCancelsRide() == true)){
                            activeRide=false;
                            List<Notification> notificationsCopy = new ArrayList<>(notifications);
                            Notification nextN=null;
                            for (Notification notification : notificationsCopy) {
                                nextN = notification;
                            }
                            declineRequest(nextN, false);
                            try {
                                endRide=false;
                                cancelRide=false;
                                toggleRideButtons();
                                mMap.clear();
                                removeAllPolylines(outsideUsingResult);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        activeRide = false;
                        try {
                            endRide=false;
                            cancelRide=false;
                            toggleRideButtons();
                            mMap.clear();
                            removeAllPolylines(outsideUsingResult);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        List<Notification> notificationsCopy = new ArrayList<>(notifications);
                        Notification nextN=null;
                        for (Notification notification : notificationsCopy) {
                            nextN = notification;
                        }
                        declineRequest(nextN, false);
                        Toast.makeText(getContext(), "Request rejected!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    activeRide = false;
                    try {
                        endRide=false;
                        cancelRide=false;
                        toggleRideButtons();
                        mMap.clear();
                        removeAllPolylines(outsideUsingResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    List<Notification> notificationsCopy = new ArrayList<>(notifications);
                    Notification nextN=null;
                    for (Notification notification : notificationsCopy) {
                        nextN = notification;
                    }
                    declineRequest(nextN, false);
                    //Toast.makeText(getContext(), "checkCurrentRide error: " + response.code()+"+"+response.message(), Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<RideDTO> call, Throwable t) {
                if(activeRide==true) {
                    activeRide = false;
                    try {
                        endRide = false;
                        cancelRide = false;
                        toggleRideButtons();
                        mMap.clear();
                        removeAllPolylines(outsideUsingResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    List<Notification> notificationsCopy = new ArrayList<>(notifications);
                    Notification nextN = null;
                    for (Notification notification : notificationsCopy) {
                        nextN = notification;
                    }
                    declineRequest(nextN, false);
                }
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
                //Toast.makeText(getContext(), "checkCurrentRide, Status code: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        endRide=false;
        cancelRide=false;
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
    double[] tripDurations = new double[2];
    double[] tripDistances = new double[2];
    private void checkNotifications() {
        if(activeRide==false) {
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
                            calculateDirections(customerMarker, latitude, longitude, R.color.Blue, 0);
                            //add a marker to the destination
                            LatLng destinationPosition = new LatLng(notification.getDestLatitude(), notification.getDestLongitude());
                            destinationMarkerOptions = new MarkerOptions()
                                    .position(destinationPosition)
                                    .title("Destination")
                                    //.snippet("Customer's Destination")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            destinationMarker = mMap.addMarker(destinationMarkerOptions);
                            //compute the directions between the customer and the destination
                            calculateDirections(destinationMarker, notification.getCustLatitude(), notification.getCustLongitude(), R.color.Red, 1);
                            Toast.makeText(getContext(), "Request from: " + notification.getCustomerFirstName(), Toast.LENGTH_SHORT).show();
                            notifications.add(notification);
                        } else if (chipDecline != null && layoutAccept != null) {
                            chipDecline.setVisibility(View.GONE);
                            layoutAccept.setVisibility(View.GONE);
                        }
                        removeNotifications();
                    } else {
                        Toast.makeText(getContext(), "CheckNotificationError: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Notification> call, Throwable t) {
                    // Handle the error
                }
            });
        }
    }

    boolean activeRide=false;
    private void removeNotifications() {
        TimerTask removeExpiredNotificationsTask = new TimerTask() {
            private int progress = 0;
            long timePassed;
            Notification nextN = null;
            boolean notificationHasBeenAccepted=false;
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                List<Notification> notificationsCopy = new ArrayList<>(notifications);
                for (Notification notification : notificationsCopy) {
                    nextN = notification;
                    timePassed = currentTimeMillis - nextN.getTimeCreated();
                    if (timePassed > 6000) {
                        declineRequest(nextN, notificationHasBeenAccepted);
                    }
                }

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() != null) {
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
                                        declineRequest(nextN, notificationHasBeenAccepted);
                                    }
                                });
                                layoutAccept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!notifications.isEmpty()) {
                                            Notification notification = notifications.get(0);
                                            changeTheStatusOfTheRequest(true, notification.getCustomerId(), notification);
                                            chipDecline.setVisibility(View.GONE);
                                            layoutAccept.setVisibility(View.GONE);
                                            circularProgressBar.setProgress(0f);
                                            progress = 0;
                                            notificationHasBeenAccepted = true;
                                            activeRide = true;
                                            turnByTurnDirections();
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
                                        Toast.makeText(getContext(), "Accept Action", Toast.LENGTH_SHORT).show();
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
                        }
                    });
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(removeExpiredNotificationsTask, 0, 100); // run every 100 milliseconds
    }

    private void declineRequest(Notification notification,boolean hasBeenAccepted) {
        if (notification != null) {
            if (activeRide == false) {
                notifications.remove(notification);
                removeAllPolylines(outsideUsingResult);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        destinationMarker.remove();
                        customerMarker.remove();
                        //Toast.makeText(getContext(), "The request has been declined", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            /////////CALL SOME KIND OF REQUEST DECLINED REQUEST
            if (!hasBeenAccepted) {
                changeTheStatusOfTheRequest(false, notification.getCustomerId(), notification);
            }
        }
    }
    RideDTO ride=new RideDTO();
    private void changeTheStatusOfTheRequest(boolean status, String customerId, Notification notification) {
        if(activeRide==false) {
            Call<RideDTO> call = api.changeTheStatusOfTheRequest(authToken, idToken, status, customerId, notification.getDestLatitude(), notification.getDestLongitude());
            call.enqueue(new Callback<RideDTO>() {
                @Override
                public void onResponse(Call<RideDTO> call, Response<RideDTO> response) {
                    if (response.isSuccessful()) {
                        if (status == false) {
                            Toast.makeText(getContext(), "The request has been rejected!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "The request has been accepted!", Toast.LENGTH_SHORT).show();
                            ride.setCost(response.body().getCost());
                            ride.setCurrency(response.body().getCurrency());
                            ride.setId(response.body().getId());
                            ride.setDriver(response.body().getDriver());
                            ride.setPassenger(response.body().getPassenger());
                            ride.setRoute(response.body().getRoute());
                        }
                    } else {
                        Toast.makeText(getContext(), "changeTheStatusOfTheRequest error: " + response.code() + "+" + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RideDTO> call, Throwable t) {
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
                    Toast.makeText(getContext(), "changeTheStatusOfTheRequest, Status code: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //map directions api

    private GeoApiContext mGeoApiContext=null;
    DirectionsResult outsideUsingResult=null;
    private void calculateDirections(Marker marker, Double auxLatitude, Double auxLongitude, int polyLineColor, int index){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(false);
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
                outsideUsingResult=result;
                tripDurations[index] = result.routes[0].legs[0].duration.inSeconds;
                tripDistances[index] = result.routes[0].legs[0].distance.inMeters;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txt_estimate_time = getView().findViewById(R.id.txt_estimate_time);
                        if (tripDurations[0]!=0 && tripDurations[1]!=0) {
                            txt_estimate_time.setText(String.valueOf((int)(tripDurations[0]+tripDurations[1])/60)+" min");
                        }
                        txt_estimate_distance=getView().findViewById(R.id.txt_estimate_distance);
                        if(tripDistances[0]!=0 && tripDistances[1]!=0){
                            txt_estimate_distance.setText(String.valueOf((float)(tripDistances[0]+tripDistances[1])/1000)+ " km");
                        }
                    }
                });
                addPolylinesToMap(result, polyLineColor);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }
    private void removeAllPolylines(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (PolylineData polylineData : mPolyLinesData) {
                    polylineData.getPolyline().remove();
                }
                mPolyLinesData.clear();
                mPolyLinesData = new ArrayList<>();
            }
        });
    }
    private void addPolylinesToMap(final DirectionsResult result, int polyLineColor){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                if(mPolyLinesData.size() > 1){
                    for(PolylineData polylineData: mPolyLinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }

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
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));
                }
            }
        });
    }
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

    }

    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    @Override
    public void onPolylineClick(Polyline polyline) {

        for(PolylineData polylineData: mPolyLinesData){
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.BlueViolet));
                polylineData.getPolyline().setZIndex(1);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.DarkGray));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    private void turnByTurnDirections(){
        Notification notification = notifications.get(0);
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + notification.getDestLatitude() + "," + notification.getDestLongitude() +
                        "+to:" + notification.getDestLatitude() + "," + notification.getDestLongitude() +
                        //"+to:" + notification.getDestLatitude() + "," + notification.getDestLongitude() +
                        "&mode=d"));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            String uri = "waze://?ll=" + latitude + "," + longitude + "&navigate=yes";
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.waze");
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                uri = "http://maps.apple.com/?ll=" + latitude + "," + longitude;
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.apple.maps");
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    uri = "geo:" + latitude + "," + longitude + "?z=15";
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("org.openstreetmap");
                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Please install Google Maps, or use other separate GPS application!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
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