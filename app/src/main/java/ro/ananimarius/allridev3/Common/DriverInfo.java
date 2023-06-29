package ro.ananimarius.allridev3.Common;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class DriverInfo implements Cloneable{
    public String accId;
    public String email;
    public String familyName;
    public String givenName;
    public Uri photoURL;
    public LatLng latLong;

    public DriverInfo() {
    }

    public DriverInfo(String accId, String email, String familyName, String givenName, Uri photoURL) {
        this.accId = accId;
        this.email = email;
        this.familyName = familyName;
        this.givenName = givenName;
        this.photoURL = photoURL;
    }

//    public static String buildWelcomeMessage() {
//        return new StringBuilder("Welcome ").append(DriverInfo.currentUser)
//    }

    public String getAccId() {
        return accId;
    }

    public void setAccId(String accId) {
        this.accId = accId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public Uri getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(Uri photoURL) {
        this.photoURL = photoURL;
    }

    public LatLng getLatLong() {
        return latLong;
    }

    public void setLatLong(LatLng latLong) {
        this.latLong = latLong;
    }
}
