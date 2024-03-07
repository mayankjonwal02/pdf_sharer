package com.example.spandan_file_sender.android.API

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ConnectionSpec
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class APIViewModel(context: Context):ViewModel()
{
    lateinit var apiService: APIService
    var ipaddress = "172.31.2.248"
    private val trustAllManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(trustAllManager), null)
    }

    init {
        var gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$ipaddress")
            .client(
                OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory,trustAllManager)
                    .hostnameVerifier { _,_ -> true  }
                    .build()


            )
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(APIService::class.java)
    }


    suspend fun mydata(): String {
        try {
            val responce = apiService.getdata()
            return responce.string().toString()

        }
        catch (e:IOException)
        {
            return e.message.toString()
        }

    }


    suspend fun uploadPdf(context: Context, path:String): String {
        val file = File(path)
        val requestBody = MultipartBody.Part.createFormData(
            "pdfFile",file.name, RequestBody.create(MediaType.parse("application/pdf"),file)
        )
        try {
            val responce = apiService.uploadPdfFile(requestBody)
            Log.e("upload",responce.string().toString())
            GlobalScope.launch(Dispatchers.Main)
            {
                Toast.makeText(context,responce.string().toString(),Toast.LENGTH_SHORT).show()

            }
            return responce.string().toString()
        }
        catch (e:IOException)
        {
            Log.e("upload",e.message.toString())
            GlobalScope.launch(Dispatchers.Main)
            {
                Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()

            }
            return e.message.toString()
        }
    }


}