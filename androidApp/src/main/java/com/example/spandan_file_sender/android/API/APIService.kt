package com.example.spandan_file_sender.android.API

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import javax.annotation.processing.Generated

interface APIService
{

    @GET("SMJ1/testing.php")
    suspend fun getdata() : ResponseBody

    @GET("mydata")
    suspend fun getdata1() : ResponseBody

    @Multipart
    @POST("SMJ1/x2s.php")
    suspend fun uploadPdfFile(
        @Part pdffile : MultipartBody.Part
    ) : ResponseBody
}



data class myinfo(
    val name : String,
    val age : Float
)