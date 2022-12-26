package ro.ananimarius.allridev3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.tasks.Task;

import ro.ananimarius.allridev3.Common.DriverInfo;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int loginRequestCode=666999;
    private final static int RESOLVE_HINT=420420;
    private Button phoneSignin;
    private Button googleSignin;
    GoogleSignInOptions gsio;
    GoogleSignInClient gsic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sign_in);

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
    private void navigateToSignInActivity() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        driverInstance=new DriverInfo(account.getId(),account.getEmail(),account.getFamilyName(),
                account.getGivenName(),account.getPhotoUrl());
        Toast.makeText(getApplicationContext(), driverInstance.getEmail(), Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(getApplicationContext(), DriverHomeActivity.class);
        startActivity(intent);
        finish();
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