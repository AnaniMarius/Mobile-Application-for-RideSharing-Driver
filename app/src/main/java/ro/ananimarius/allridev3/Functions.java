package ro.ananimarius.allridev3;

import android.util.Log;
import android.webkit.CookieManager;

import org.json.JSONException;
import org.json.JSONObject;

public class Functions {
    public String getAuthTokenCookie(){ //SEARCH FOR THE COOKIE TO BE SENT TO THE API
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

    public String parseCookie(String authToken)
    {
        String authTokenParsed = null;
        try {
            JSONObject jsonObject = new JSONObject(authToken.substring(9));
            authTokenParsed = jsonObject.getString("authToken");
        } catch (
                JSONException e) {
            e.printStackTrace();
        }
        return authTokenParsed;
    }


}
