package ro.ananimarius.allridev3.ui.home;

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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

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
import ro.ananimarius.allridev3.Common.Notification;
import ro.ananimarius.allridev3.DriverHomeActivity;
import ro.ananimarius.allridev3.Functions;
import ro.ananimarius.allridev3.R;
import ro.ananimarius.allridev3.SplashScreenActivity;
import ro.ananimarius.allridev3.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

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
    }

    //CHECK THE NOTIFICATIONS
    private Handler handler = new Handler();
    private int delay = 5000; // 5 seconds in milliseconds
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
    private void checkNotifications() {
        Call<Notification> call = api.getRequestFromCustomerNotification(authToken, idToken);
        call.enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                if (response.isSuccessful()) {
                    Notification notification = response.body();
                    if (notification != null) {
                        Toast.makeText(getContext(), "Request from: " + notification.getCustomerFirstName(), Toast.LENGTH_SHORT).show();
                        notifications.add(notification);
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

    private void removeNotifications(){
        TimerTask removeExpiredNotificationsTask = new TimerTask() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                    Iterator<Notification> iterator = notifications.iterator();
                    while (iterator.hasNext()) {
                        Notification nextN = iterator.next();
                        if ((currentTimeMillis - nextN.getTimeCreated()) > 20000) {
                            iterator.remove();
                        }
                    }
                }

        };
    }


}