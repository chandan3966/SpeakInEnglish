package com.example.api.interfaces.FirebaseConnectingAPI

import com.google.firebase.firestore.DocumentSnapshot

interface FirebaseConnectingCallbackFirestore{
    fun OnSuccessListener(snapshot: List<DocumentSnapshot>)
    fun OnFailureListener(error: Exception)
}