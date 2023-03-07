package com.example.api.externalapis

import android.content.Context
import com.example.api.interfaces.VocabAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object VocabularyApi {

    //example url
    //https://api.dictionaryapi.dev/api/v2/entries/en/hello
    val API = "https://api.dictionaryapi.dev/"


    fun ApiBuilder(activity: Context): VocabAPI {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        clientBuilder.addInterceptor(interceptor)


        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(API)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(VocabAPI::class.java)
    }

}