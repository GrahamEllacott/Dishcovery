package com.example.dishcovery.data.api

import com.example.dishcovery.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

// imgbb API Response Models
data class ImgbbResponse(
    val data: ImgbbData?,
    val success: Boolean,
    val status: Int
)

data class ImgbbData(
    val id: String?,
    val url: String?,
    val display_url: String?,
    val delete_url: String?
)

// imgbb API Interface
interface ImgbbApiService {
    @FormUrlEncoded
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Field("image") imageBase64: String
    ): ImgbbResponse

    companion object {
        // Replace with your imgbb API key
        private const val API_KEY = BuildConfig.IMG_BB_API_KEY
        private const val BASE_URL = "https://api.imgbb.com/"

        fun create(): ImgbbApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ImgbbApiService::class.java)
        }

        fun getApiKey(): String = API_KEY
    }
}