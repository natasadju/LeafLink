package si.um.feri.leaf.utils;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import si.um.feri.leaf.Event;
import si.um.feri.leaf.Park;

import java.util.List;

public interface LeafLinkApi {
    @GET("events")
    Call<List<Event>> getEvents();

    @GET("parks/{id}")
    Call<Park> getPark(@Path("id") String parkId);

    @GET("parks")
    Call<List<Park>> getParks();

}
