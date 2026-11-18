package com.example.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AvailableCountryDto(
    val countryCode: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class PublicHolidayDto(
    val date: String,
    val localName: String,
    val name: String,
    val countryCode: String,
    val fixed: Boolean? = null,
    val global: Boolean? = null,
    val types: List<String>? = null
)

interface HolidayApiService {

    @GET("api/v3/AvailableCountries")
    suspend fun getAvailableCountries(): Response<List<AvailableCountryDto>>

    @GET("api/v3/NextPublicHolidays/{countryCode}")
    suspend fun getNextPublicHolidays(
        @Path("countryCode") countryCode: String
    ): Response<List<PublicHolidayDto>>

    @GET("api/v3/PublicHolidays/{year}/{countryCode}")
    suspend fun getPublicHolidaysForYear(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): Response<List<PublicHolidayDto>>
}

object HolidayApiClient {
    private const val BASE_URL = "https://date.nager.at/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val service: HolidayApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(HolidayApiService::class.java)
    }
}
