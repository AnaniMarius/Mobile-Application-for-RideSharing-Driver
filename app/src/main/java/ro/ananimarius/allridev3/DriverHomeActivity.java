package ro.ananimarius.allridev3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import retrofit2.Retrofit;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ro.ananimarius.allridev3.Common.DriverInfo;
import ro.ananimarius.allridev3.databinding.ActivityDriverHomeBinding;

public class DriverHomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDriverHomeBinding binding;


    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDriverHome.toolbar);

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();

        //here is used the HOMEFRAGMENT.JAVA
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if (account != null) {
//            TextView nameTxt = (TextView)findViewById(R.id.nameText);
//            nameTxt.setText(account.getGivenName()); //GIVES RUNTIME ERROR
//        }


        Bundle bundle = new Bundle();
        bundle.putString("googleId", account.getIdToken());
        bundle.putString("email", account.getEmail());
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_driver_home);
        navController.setGraph(R.navigation.mobile_navigation, bundle);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

//        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_driver_home);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);

        //set data for user
//        View headerView=navigationView.getHeaderView(0);
//        TextView txt_name=(TextView) headerView.findViewById(R.id.txt_name);
//        TextView txt_phone=(TextView) headerView.findViewById(R.id.txt_phone);
//        TextView txt_star=(TextView) headerView.findViewById(R.id.txt_star);

        //txt_name.setText(DriverInfo.buildWelcomeMessage());

        init();
    }

private String getAuthTokenCookie(){ //SEARCH FOR THE COOKIE TO BE SENT TO THE API
    CookieManager cookieManagerCheck = CookieManager.getInstance();
    String cookie = cookieManagerCheck.getCookie("http://10.0.2.2:8080");
    if (cookie != null) {
        //the cookie exists
        Log.d("COOKIE_GET", "authToken value: " + cookie);
        //Toast.makeText(getApplicationContext(), "Cookie found ", Toast.LENGTH_SHORT).show();
    } else {
        //the cookie does not exist
        Log.d("COOKIE_GET", "authToken cookie not found");
        //Toast.makeText(getApplicationContext(), "Cookie not found ", Toast.LENGTH_SHORT).show();
    }
    return cookie;
}

private void deleteCookie(){ //delete cookie from the client
    CookieManager cookieManagerCheck = CookieManager.getInstance();
    String cookie = cookieManagerCheck.getCookie("http://10.0.2.2:8080");
    if (cookie != null) {
        // The cookie exists
        Log.d("COOKIE_DELETED", "authToken value: " + cookie);
        //Toast.makeText(getApplicationContext(), "Cookie found has been deleted ", Toast.LENGTH_SHORT).show();

        //delete the cookie
        cookieManagerCheck.setCookie("http://10.0.2.2:8080", "authToken=;expires=Thu, 01 Jan 1970 00:00:00 GMT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManagerCheck.flush();
        } else {
            CookieSyncManager.createInstance(this);
            CookieSyncManager.getInstance().sync();
        }
    } else {
        //the cookie does not exist
        Log.d("COOKIE_DELETED", "authToken cookie not found");
        //Toast.makeText(getApplicationContext(), "Cookie not found ", Toast.LENGTH_SHORT).show();
    }
}

private void init() {
    //here we initialize the signout button
    navigationView.setNavigationItemSelectedListener(item -> {
        if(item.getItemId() == R.id.nav_sign_out) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
            builder.setTitle("Sign out")
                    .setMessage("Confirm to sign out")
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setPositiveButton("Sign Out", (dialogInterface, i) -> {
                        //revoke access token before signing out
                        GoogleSignInClient signInClient = GoogleSignIn.getClient(DriverHomeActivity.this,
                                GoogleSignInOptions.DEFAULT_SIGN_IN);
                        signInClient.revokeAccess().addOnCompleteListener(task -> {
                            //make an HTTP request to the signout endpoint of the API
                            new SignOutTask().execute();
                        });
                    })
                    .setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(R.color.colorAccent));
            });
            dialog.show();
        }
        return true;
    });
}

    class SignOutTask extends AsyncTask<Void, Void, Boolean> { //SEND THE AUTHTOKEN BACK TO DELETE IT
        @Override
        protected Boolean doInBackground(Void... params) {
            String authToken = getAuthTokenCookie();
            //try to parse
            String authTokenParsed=null;
            try {
                JSONObject jsonObject = new JSONObject(authToken.substring(9));
                authTokenParsed = jsonObject.getString("authToken");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = "http://10.0.2.2:8080/user/signout?authToken=" + authTokenParsed;
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    deleteCookie();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //handle the result of the API request
            if (result) {
                Intent intent = new Intent(DriverHomeActivity.this, SplashScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                //handle unsuccessful response
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_driver_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        new SignOutTask().execute();
    }
}