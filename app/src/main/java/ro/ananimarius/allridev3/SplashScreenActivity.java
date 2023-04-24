package ro.ananimarius.allridev3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.FormUrlEncoded;
import ro.ananimarius.allridev3.Common.DriverInfo;


public class SplashScreenActivity extends AppCompatActivity {

    private final static int loginRequestCode = 666999;
    private final static int RESOLVE_HINT = 420420;
    private Button phoneSignin;
    private Button googleSignin;
    GoogleSignInOptions gsio;
    GoogleSignInClient gsic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sign_in);

        //check if the app has location permission, and request it if not
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        googleSignin = (Button) findViewById(R.id.btn_google_sign_in);

        String AUTH_ID = "1054018382060-i69d011p6jksrqber2k4h1dn37taijev.apps.googleusercontent.com";

        gsio = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(AUTH_ID)
                .requestEmail()
                .build();
        gsic = GoogleSignIn.getClient(this, gsio);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {

            //get account info and send them to the info class
            //navigate to second activity
            navigateToSignInActivity();
        }

        googleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
                googleSignIn();
            }
        });


// FOR NON DEPRICATED PHONE SIGN IN, ALSO SALVAT IN STARBAR IN BROWSER
        phoneSignin = (Button) findViewById(R.id.btn_phone_sign_in);

        phoneSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestHint();
            }
        });
    }

    // Create an instance of FusedLocationProviderClient
    private FusedLocationProviderClient fusedLocationClient;
    double latitude = 0;
    double longitude = 0;
    Location mlocation = null;
    String address = null;
    String city;
    String state;
    String country;
    String postalCode;

    private void getLocation() {
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mlocation = location;
                Log.d("Location Changes", location.toString());
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Status Changed", String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Provider Disabled", provider);
            }
        };

        // Now first make a criteria with your requirements
        // this is done to save the battery life of the device
        // there are various other other criteria you can search for..
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        // Now create a location manager
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // This is the Best And IMPORTANT part
        final Looper looper = null;

        // Now whenever the button is clicked fetch the location one time

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
        locationManager.requestSingleUpdate(criteria, locationListener, looper);
    }
    //ASK PERMISSION AND CLOSE APP IF DENIED.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //ggetLocation();
            } else {
                Toast.makeText(getApplicationContext(), "Permission denied. Closing app...", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishAffinity();
                    }
                }, 2000); //2 seconds
            }
        }
    }
    private void googleSignIn() {
        Intent signInIntent=gsic.getSignInIntent();
        startActivityForResult(signInIntent,loginRequestCode);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==loginRequestCode){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);
                navigateToSignInActivity();
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!"+e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                //NON DEPRICATED SOLUTION
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                String phoneNumber=credential.getId();  //<-- will need to process phone number string
            }
            else if(
                    requestCode==RESOLVE_HINT&&resultCode== CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE
            ){
                //NO PHONE NUMBERS AVAILABLE
                Toast.makeText(getApplicationContext(),"No phone numbers found",Toast.LENGTH_LONG).show();
            }
        }
    }
    public DriverInfo driverInstance=new DriverInfo();
    //API CONNECTION TO SEND GOOGLE INFO
    public interface APIInterface {
        @FormUrlEncoded
        @POST("user/loginByGoogle")
        Call<JsonObject> sendGoogleAccount(@Field("idToken") String idToken,
                                             @Field("email") String email,
                                           @Field("familyName") String fName,
                                           @Field("givenName") String gName,
                                           @Field("isDriver") Boolean driverCheck,
                                           @Field("latitude") double latitude,
                                           @Field("longitude")double longitude);
    }
    private void navigateToSignInActivity() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        driverInstance=new DriverInfo(account.getIdToken(),account.getEmail(),account.getFamilyName(),
                account.getGivenName(),account.getPhotoUrl());
        Toast.makeText(getApplicationContext(), driverInstance.getEmail(), Toast.LENGTH_SHORT).show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIInterface api = retrofit.create(APIInterface.class);
        Call<JsonObject> call = api.sendGoogleAccount(account.getIdToken(), account.getEmail(), account.getFamilyName(),
                account.getGivenName(), true, latitude, longitude);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    //get the JSONObject from the response body
                    JsonObject jsonResponse = response.body();
                    //convert the JSONObject to a string
                    String jsonString = jsonResponse.toString();
                    // save the string to a cookie
                    //CookieManager.getInstance().setCookie("authToken=",jsonString);

                    //set the cookie with the domain name
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.setAcceptCookie(true);
                    cookieManager.setCookie( "http://10.0.2.2:8080","authToken"+ jsonString);

                    //sync the cookies accordingly to the android version
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        cookieManager.flush();
                    } else {
                        CookieSyncManager.createInstance(getApplicationContext());
                        CookieSyncManager.getInstance().sync();
                    }
                    Toast.makeText(getApplicationContext(), "ResponseCode " + response.code(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), DriverHomeActivity.class);
                    startActivity(intent);

                    //check for the cookie if it exists
                    CookieManager cookieManagerCheck = CookieManager.getInstance();
                    String cookie = cookieManagerCheck.getCookie("http://10.0.2.2:8080");
                    if (cookie != null) {
                        //the cookie exists
                        Log.d("COOKIE", "authToken value: " + cookie);
                        Toast.makeText(getApplicationContext(), "Cookie created " + cookie, Toast.LENGTH_SHORT).show();
                    } else {
                        //the cookie does not exist
                        Log.d("COOKIE", "authToken cookie not found");
                        Toast.makeText(getApplicationContext(), "Cookie failed to be created " + cookie, Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error sending Google account info to server: " + response.code(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Error sending Google account info to server, Status code: " + statusCode + ", Message: " + errorMessage+account.getIdToken().length(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //GoogleApiClient apiClient;
    //PHONE SIGN IN


    private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

//        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
//                apiClient, hintRequest); DEPRICATED
        PendingIntent intent = Credentials.getClient(this).getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }
}
