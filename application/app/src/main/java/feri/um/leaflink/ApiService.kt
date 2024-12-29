package feri.um.leaflink

import retrofit2.Call
import retrofit2.http.GET
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
}
