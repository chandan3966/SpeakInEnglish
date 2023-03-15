package com.example.api.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName

@IgnoreExtraProperties
data class FindingObject(
    @SerializedName("createdBy")
    var createdBy: String = "",
    @SerializedName("incoming")
    var incoming: String = "",
    @SerializedName("isAvailable")
    var isAvailable: Boolean = true,
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("gender")
    var gender :Double = 2.0,
    @SerializedName("level")
    var level :Double = 2.0,
    @SerializedName("selfGender")
    var selfGender :Double = 2.0,
    @SerializedName("selfLevel")
    var selfLevel :Double = 2.0,
    @SerializedName("id")
    var id :String = "",
)