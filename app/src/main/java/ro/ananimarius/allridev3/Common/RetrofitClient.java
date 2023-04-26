package ro.ananimarius.allridev3.Common;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit instance;

    // Declare the retrofit instance as static
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static DirectionsApiService getDirectionsApiService() {
        return getClient().create(DirectionsApiService.class);
    }

    public static Retrofit getClient() {
        if (instance == null) {
            instance = retrofit;
        }
        return instance;
    }
}
