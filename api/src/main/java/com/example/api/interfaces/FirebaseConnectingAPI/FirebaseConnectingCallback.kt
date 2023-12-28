package com.example.api.interfaces.FirebaseConnectingAPI

import com.google.firebase.database.DataSnapshot

interface FirebaseConnectingCallback{
    fun OnSuccessListener(snapshot: DataSnapshot)
    fun OnFailureListener(error: Exception)
}
