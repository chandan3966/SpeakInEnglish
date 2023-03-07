package com.example.api.model

import com.google.gson.annotations.SerializedName

data class VocabResponse(
    @SerializedName("title")
    val title:String = "",
    @SerializedName("message")
    val message:String = "",
    @SerializedName("resolution")
    val resolution:String = "",
    val data:List<VocabInternalRes> = emptyList(),
)

data class VocabInternalRes(
    @SerializedName("word")
    val word:String = "",
    @SerializedName("phonetic")
    val phonetic:String = "",
    @SerializedName("phonetics")
    val phonetics:List<Phonetics> = emptyList(),
    @SerializedName("origin")
    val origin:String = "",
    @SerializedName("meanings")
    val meanings:List<Meanings> = emptyList(),
    @SerializedName("sourceUrls")
    val sourceUrls:List<String> = emptyList(),
)


data class Phonetics(
    @SerializedName("text")
    val text:String = "",
    @SerializedName("audio")
    val audio:String = "",
)

data class Meanings(
    @SerializedName("partOfSpeech")
    val partOfSpeech:String = "",
    @SerializedName("definitions")
    val definitions:List<Definitions> = emptyList(),
    @SerializedName("synonyms")
    val synonyms:List<String> = emptyList(),
    @SerializedName("antonyms")
    val antonyms:List<String> = emptyList(),
)

data class Definitions(
    @SerializedName("definition")
    val definition:String = "",
    @SerializedName("example")
    val example:String = "",
    @SerializedName("synonyms")
    val synonyms:List<String> = emptyList(),
    @SerializedName("antonyms")
    val antonyms:List<String> = emptyList(),
)