package ro.ananimarius.allridev3.Common;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DirectionsApiService{
    @GET("maps/api/directions/json")
    Observable<DirectionsResponse> getDirections(
            @Query("mode") String mode,
            @Query("transit_routing_preference") String transit_routing,
            @Query("origin") String from,
            @Query("destination") String to,
            @Query("key") String key
    );
}