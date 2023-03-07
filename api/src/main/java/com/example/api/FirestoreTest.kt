package com.example.api

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirestoreTest {

    val db = Firebase.firestore

    fun addOrUpdateData(hashMap: HashMap<String,Any>? = null){
        var user:HashMap<String,Any>? = null
        if (hashMap == null){
            user = hashMapOf(
                "first" to "Ada",
                "last" to "Lovelace",
                "born" to 1815
            )
        }
        else {
            user = hashMap
        }

// Add a new document with a generated ID
        db.collection("testing").document("1")
            .set(user)
    }

    fun readData(){
        db.collection("testing").document("1")
            .get()
            .addOnSuccessListener { result ->
                for (document in result.data!!.entries) {
                    Log.d("Firestore Read Data", "${document.key} => ${document.value}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Read Data", "Error getting documents.", exception)
            }
    }


    fun deleteData(){

// Add a new document with a generated ID
        db.collection("testing").document("1")
            .delete()
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore Add Data", "DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore Add Data", "Error adding document", e)
            }
    }

}