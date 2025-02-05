package feri.um.leaflink.ui

import feri.um.leaflink.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://172.211.85.100:3000/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS) // Set connection timeout
        .readTimeout(300, TimeUnit.SECONDS)    // Set read timeout
        .writeTimeout(300, TimeUnit.SECONDS)   // Set write timeout
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Attach the custom OkHttpClient
            .build()
        retrofit.create(ApiService::class.java)
    }
}
