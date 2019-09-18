package com.app.linkscanner.network

import com.app.linkscanner.network.models.scan_response.ScanResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import com.app.linkscanner.network.models.search_response.SearchResponse
import retrofit2.Call
import retrofit2.create
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://urlscan.io/api/v1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface LinkScannerAPIService {
    @GET("search/")
    fun getSearchResults(@Query("q") q: String, @Query("size") size: Int): Call<SearchResponse>

    @GET("result/{uuid}")
    fun getScanResult(@Path("uuid") uuid: String): Call<ScanResponse>
}

object LinkScannerAPI {
    val searchService: LinkScannerAPIService by lazy { retrofit.create(LinkScannerAPIService::class.java) }
    val scanService: LinkScannerAPIService by lazy { retrofit.create(LinkScannerAPIService::class.java) }
}
