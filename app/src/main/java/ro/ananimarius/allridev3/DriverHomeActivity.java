package ro.ananimarius.allridev3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

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
//        Button signOut=(Button) findViewById(R.id.nav_sign_out);
//        signOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), "teeeest", Toast.LENGTH_SHORT).show();
//            }
//        });
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
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_driver_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //set data for user
        View headerView=navigationView.getHeaderView(0);
        TextView txt_name=(TextView) headerView.findViewById(R.id.txt_name);
        TextView txt_phone=(TextView) headerView.findViewById(R.id.txt_phone);
        TextView txt_star=(TextView) headerView.findViewById(R.id.txt_star);

        //txt_name.setText(DriverInfo.buildWelcomeMessage());

        init();
    }

//    private void init() {
//        //here we initialize the signout button
//        navigationView.setNavigationItemSelectedListener(item -> {
//            if(item.getItemId() == R.id.nav_sign_out) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
//                builder.setTitle("Sign out")
//                        .setMessage("Confirm to sign out")
//                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
//                        .setPositiveButton("Sign Out", (dialogInterface, i) -> {
//                            //revoke access token before signing out
//                            GoogleSignInClient signInClient = GoogleSignIn.getClient(DriverHomeActivity.this,
//                                    GoogleSignInOptions.DEFAULT_SIGN_IN);
//                            signInClient.revokeAccess().addOnCompleteListener(task -> {
//                                //API SIGN OUT CODE TO COMPLETE HERE BEFORE INTENT
//
//                                //make an HTTP request to the signout endpoint of your API
//                                String url = "http://10.0.2.2:8080/user/signout";
//                                OkHttpClient client = new OkHttpClient();
//                                Request request = new Request.Builder()
//                                        .url(url)
//                                        .build();
//                                try {
//                                    Response response = client.newCall(request).execute();
//                                    if (response.isSuccessful()) {
//                                        //API SIGN OUT CODE TO COMPLETE HERE BEFORE INTENT
//                                        Intent intent = new Intent(DriverHomeActivity.this, SplashScreenActivity.class);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);
//                                        finish();
//                                    } else {
//                                        //handle unsuccessful response
//                                    }
//                                } catch (IOException e) {
//                                    //handle network error
//                                }
//                                Intent intent = new Intent(DriverHomeActivity.this, SplashScreenActivity.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                startActivity(intent);
//                                finish();
//                            });
//                        })
//                        .setCancelable(false);
//                AlertDialog dialog = builder.create();
//                dialog.setOnShowListener(dialogInterface -> {
//                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
//                            .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
//                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
//                            .setTextColor(getResources().getColor(R.color.colorAccent));
//                });
//                dialog.show();
//            }
//            return true;
//        });
//    }
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

    private class SignOutTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            //make an HTTP request to the signout endpoint of the API
            String url = "http://10.0.2.2:8080/user/signout";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
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
}