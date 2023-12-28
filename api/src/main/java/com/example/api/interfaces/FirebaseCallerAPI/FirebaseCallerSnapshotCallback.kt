package com.example.api.interfaces.FirebaseCallerAPI

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

interface FirebaseCallerSnapshotCallback{
    fun OnSuccessListener(snapshot: DataSnapshot)
    fun OnCancelled(error: DatabaseError)
}