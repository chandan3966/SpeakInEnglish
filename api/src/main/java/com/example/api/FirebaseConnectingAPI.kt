package com.example.api

import android.util.Log
import com.example.api.model.Filter
import com.example.api.model.User
import com.example.api.util.Utils
import com.google.firebase.database.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseConnectingAPI {
    final val TAG = "FirebaseConnectingAPI"

    val profileRef = FirebaseDatabase.getInstance().getReference().child("profiles")

    val userRef = FirebaseDatabase.getInstance().getReference().child("users")

    val userPrefRef = FirebaseDatabase.getInstance().getReference().child("users_preference")


    fun fetchUserWithStatus(gender:String,level:String,user:User,listener: FirebaseConnectingCallback){
        userRef
            .orderByChild("status")
            .equalTo(0.toDouble()).limitToFirst(1)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener.OnSuccessListener(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.OnFailureListener(error.toException())
                }
            })

    }

    fun removePreviousSession(user:User){
        userRef.child(user.id).setValue(null)
    }

    fun roomAvailable(key:String,qtype:String,question:ArrayList<Int>,username:String){
        // other caller initated call and got connected
        userRef
            .child(key)
            .child("incoming")
            .setValue(username)
        userRef
            .child(key)
            .child("status")
            .setValue(1)
    }

    fun roomNotAvailable(username: String, gender:String, level:String,qtype:String,question:ArrayList<Int>,user: User, listener: FirebaseConnectingCallback){

        // Not Available
        var filter = Filter(gender,level,user.gender,user.ownlevel,user.id)
        val room = HashMap<String, Any>()
        room["incoming"] = username
        room["createdBy"] = username
        room["isAvailable"] = true
        room["status"] = 0
//        room["qtype"] = qtype
//        room["qtypeQs"] = question
//        room["creatorNxt"] = 0
//        room["otherNxt"] = 0
//        room["users_preference"] = filter

        userRef.child(username)
            .setValue(room)
            .addOnSuccessListener {
                userRef.child(username)
                    .addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.child("status").exists()) {
                                if (snapshot.child("status").getValue(Int::class.java) == 1) {
                                    listener.OnSuccessListener(snapshot)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            listener.OnFailureListener(error.toException())
                        }

                    })
            }
            .addOnFailureListener {
                listener.OnFailureListener(it)
            }
    }

    interface FirebaseConnectingCallback{
        fun OnSuccessListener(snapshot: DataSnapshot)
        fun OnFailureListener(error: Exception)
    }
}