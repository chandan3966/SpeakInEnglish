package com.example.api.interfaces.FirebaseCallerAPI

import com.example.api.model.User
import com.google.firebase.database.DatabaseError

interface FirebaseCallerCallback{
    fun OnSuccessListener(user: User?)
    fun OnCancelled(error: DatabaseError)
}