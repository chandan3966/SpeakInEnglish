package com.example.api

import com.example.api.interfaces.FirebaseConnectingAPI.FirebaseConnectingCallback
import com.example.api.interfaces.FirebaseConnectingAPI.FirebaseConnectingCallbackFirestore
import com.example.api.model.Filter
import com.example.api.model.FindingObject
import com.example.api.model.User
import com.example.api.util.Utils
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import java.util.Collections.emptyList


object FirebaseConnectingAPI {
    final val TAG = "FirebaseConnectingAPI"
    val userRef = FirebaseDatabase.getInstance().getReference().child("users")
    val userRefStore = FirebaseFirestore.getInstance().collection("users")

    lateinit var findingObject: FindingObject
    fun fetchUserWithStatus(gender:String,level:String,user:User,listener: FirebaseConnectingCallbackFirestore){
        var newGender = Utils.generateGender(gender)
        var newLevel = Utils.generateLevel(level)
        if (gender.contains("any")){
            userRefStore.whereEqualTo("status",0)
                .whereIn("selfLevel", listOf(newLevel,2.0))
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (it.documents.size>0){
                        if (((it.documents[0].data?.get("level") as Double) == Utils.generateLevel(user.ownlevel) || (it.documents[0].data?.get("level") as Double) == 2.0)
                            &&
                            ((it.documents[0].data?.get("gender") as Double) == Utils.generateGender(user.gender) || (it.documents[0].data?.get("gender") as Double) == 2.0)){
                            listener.OnSuccessListener(it.documents)
                        }
                        else {
                            listener.OnSuccessListener(emptyList())
                        }
                    }
                    else {
                        listener.OnSuccessListener(it.documents)
                    }
                }
                .addOnFailureListener {
                    listener.OnFailureListener(it)
                }
        }
        else if(level.contains("any")){
            userRefStore.whereEqualTo("status",0)
                .whereIn("selfGender", listOf(newGender,2.0))
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (it.documents.size>0){
                        if (((it.documents[0].data?.get("level") as Double) == Utils.generateLevel(user.ownlevel) || (it.documents[0].data?.get("level") as Double) == 2.0)
                            &&
                            ((it.documents[0].data?.get("gender") as Double) == Utils.generateGender(user.gender) || (it.documents[0].data?.get("gender") as Double) == 2.0)){
                            listener.OnSuccessListener(it.documents)
                        }
                        else {
                            listener.OnSuccessListener(emptyList())
                        }
                    }
                    else {
                        listener.OnSuccessListener(it.documents)
                    }
                }
                .addOnFailureListener {
                    listener.OnFailureListener(it)
                }
        }


//        userRef
//            .orderByChild("status")
//            .equalTo(0.toDouble())
//            .limitToFirst(1)
//            .addListenerForSingleValueEvent(object :ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    listener.OnSuccessListener(snapshot)
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    listener.OnFailureListener(error.toException())
//                }
//            })
    }

    fun removePreviousSession(user:User){
        userRef.child(user.id).setValue(null)
        userRefStore.document(user.id).delete()
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
        userRef
            .child(key)
            .child("otherqtype")
            .setValue(qtype)
        userRef
            .child(key)
            .child("otherqtypeQs")
            .setValue(question)
        try {
            findingObject.incoming = username
            findingObject.status = 1
            userRefStore.document(key).set(Gson().fromJson(Gson().toJson(findingObject),object : TypeToken<HashMap<String?, Any?>?>() {}.getType()))
        }
        catch (e:UninitializedPropertyAccessException){
            userRefStore.document(key).get().addOnSuccessListener {
                findingObject = Gson().fromJson(Gson().toJson(it.data),object : TypeToken<FindingObject?>() {}.getType())
                findingObject.incoming = username
                findingObject.status = 1
                userRefStore.document(key).set(Gson().fromJson(Gson().toJson(findingObject),object : TypeToken<HashMap<String?, Any?>?>() {}.getType()))
            }
        }
    }

    fun roomNotAvailable(username: String, gender:String, level:String, qtype:String, question: ArrayList<Int>, user: User, listener: FirebaseConnectingCallback){

        // Not Available
        var filter = Filter(gender,level,user.gender,user.ownlevel,user.id)
        val room = HashMap<String, Any>()
        room["incoming"] = username
        room["createdBy"] = username
        room["isAvailable"] = true
        room["status"] = 0
        room["qtype"] = qtype
        room["qtypeQs"] = question
        var newGender = Utils.generateGender(gender)
        var newLevel = Utils.generateLevel(level)

        var newSelfGender = Utils.generateGender(user.gender)
        var newSelfLevel = Utils.generateLevel(user.ownlevel)
        findingObject = FindingObject(
            username,
            username,
            true,
            0,
            newGender,
            newLevel,
            newSelfGender,
            newSelfLevel,
            user.id
        )
        userRefStore.document(username)
            .set(
                Gson().fromJson(Gson().toJson(findingObject),object : TypeToken<HashMap<String?, Any?>?>() {}.getType())
            )

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



}