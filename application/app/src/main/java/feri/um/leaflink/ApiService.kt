package feri.um.leaflink

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @GET("events")
    fun getEvents(): Call<List<Event>>

    @GET("parks")
    fun getParks(): Call<ParksResponse>

    @GET("events/{id}")
    fun getEventById(@Path("id") id: String): Call<Event>

    @GET("parks/{id}")
    fun getParkDetails(@Path("id") parkId: String): Call<Park>

    @GET("images/event/{eventId}")
    fun getImagesByEventId(@Path("eventId") eventId: String): Call<List<Image>>

    @GET("pollen")
    fun getPollens(): Call<List<Pollen>>

    @GET("air")
    fun getAirQuality(): Call<List<AirQuality>>

    @POST("events")
    fun addEvent(@Body event: EventNew): Call<Event>

    @Multipart
    @POST("processImages/upload")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
}
