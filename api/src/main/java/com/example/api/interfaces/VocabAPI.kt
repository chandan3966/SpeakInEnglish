package com.example.api.interfaces

import com.example.api.model.VocabInternalRes
import com.example.api.model.VocabResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface VocabAPI {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "Platform: android")
    @GET("api/v2/entries/en/{word}")
    fun getWordInfo(@Path("word") word: String): Call<List<VocabInternalRes>>
}