package ro.ananimarius.allridev3;

import android.util.Log;
import android.webkit.CookieManager;

import org.json.JSONException;
import org.json.JSONObject;

public class Functions {
    public String getAuthTokenCookie(){ //SEARCH FOR THE COOKIE TO BE SENT TO THE API
        CookieManager cookieManagerCheck = CookieManager.getInstance();
        String cookie = cookieManagerCheck.getCookie("http://192.168.43.52:8080"); //10.0.2.2 192.168.1.4:8080//switchIP
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

    public String parseCookie(String authToken) {
        String authTokenParsed = null;
        if (authToken != null) {
            try {
                authTokenParsed = authToken.substring(authToken.indexOf(":") + 2, authToken.length() - 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return authTokenParsed;
    }




}
