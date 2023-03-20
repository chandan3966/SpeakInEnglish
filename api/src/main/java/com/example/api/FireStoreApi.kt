package com.example.api

import android.util.Log
import com.example.api.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FireStoreApi {
//    val db = Firebase.firestore
    final val TAG = "FireStoreApi"

    val db = FirebaseDatabase.getInstance().getReference()
        .child("profiles")

    fun addUser(id:String,name:String,level:String,gender:String,avatar:String,listener: FireStoreCallback?):User{
        val user = User(
             name,
             gender,
             level,
             id,
             avatar
        )

        db.child(id)
            .setValue(user)
            .addOnSuccessListener {
                listener?.OnSuccessListener(null)
            }
            .addOnFailureListener {
                listener?.OnFailureListener(it)
            }
        return user
    }

    fun hasUser(id:String,listener:FireStoreCallback?=null){
        try {
            db.child(id)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "DocumentSnapshot added with ID: ${snapshot.getValue()}")
                        listener?.OnSuccessListener(snapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        listener?.OnFailureListener(error.toException())
                        Log.w(TAG, "Error adding document")
                    }

                })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}

interface FireStoreCallback{
    fun OnSuccessListener(snapshot: DataSnapshot?)
    fun OnFailureListener(e: Exception)
}