package com.example.api

import android.widget.ExpandableListView
import com.example.api.model.QuestionSession
import com.example.api.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

object FirebaseCallerAPI {
    final val TAG = "FirebaseCallerAPI"

    val profileRef = FirebaseDatabase.getInstance().getReference().child("profiles")

    val userRef = FirebaseDatabase.getInstance().getReference().child("users")

    val userPrefRef = FirebaseDatabase.getInstance().getReference().child("users_preference")

    fun initilizePeerIfSame(username:String,uniqueId:String,friendsUsername:String,listener: FirebaseCallerCallback){
        userRef.child(username).child("connId").setValue(uniqueId)
        userRef.child(username).child("isAvailable").setValue(true)

        profileRef
            .child(friendsUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: User? = snapshot.getValue(User::class.java)
                    listener?.OnSuccessListener(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener?.OnCancelled(error)
                }
            })
    }

    fun initilizePeerIfNotSame(friendsUsername:String,listener: FirebaseCallerCallback){
        profileRef
            .child(friendsUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    listener?.OnSuccessListener(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener?.OnCancelled(error)
                }
            })

        userRef
            .child(friendsUsername)
            .child("connId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        listener?.OnSuccessListener(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    listener?.OnCancelled(error)
                }
            })
    }

    fun listenConnId(friendsUsername:String,listener: FirebaseCallerSnapshotCallback) {
        userRef.child(friendsUsername).child("connId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener?.OnSuccessListener(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener?.OnCancelled(error)
                }
            })
    }

    fun listenOtherConnId(username:String,listener: FirebaseCallerSnapshotCallback) {
        userRef.child(username).child("connId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener?.OnSuccessListener(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener?.OnCancelled(error)
                }
            })
    }

    fun fetchSessionQuestions(username:String, listener: FirebaseCallerSnapshotCallback) {
        userRef.child(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener?.OnSuccessListener(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener?.OnCancelled(error)
                }
            })
    }

    fun changeValues(createdBy: String,username: String,value:Long,listener:FirebaseCallerNextCallback){
        if (createdBy.equals(username, ignoreCase = true)){
            var next = value
            userRef.child(createdBy)
                .child("creatorNxt")
                .setValue(next+1)
                .addOnSuccessListener {
                    listener.OnCreatorListener(next+1)
                }
        }
        else{
            var next = value
            userRef.child(createdBy)
                .child("otherNxt")
                .setValue(next+1)
                .addOnSuccessListener {
                    listener.OnInCallerListener(next+1)
                }
        }
    }

    fun listenOtherClick(createdBy: String,listener:FirebaseCallerEqualCallback){
        userRef.child(createdBy)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("creatorNxt") && snapshot.hasChild("otherNxt") &&
                        snapshot.hasChild("qtype") &&
                        snapshot.hasChild("qtypeQs") &&
                        snapshot.hasChild("otherqtype") &&
                        snapshot.hasChild("otherqtypeQs")){
                        var questions = QuestionSession(snapshot.child("qtype").value.toString(),
                            snapshot.child("qtypeQs").value as ArrayList<Long>,
                            snapshot.child("otherqtype").value.toString(),
                            snapshot.child("otherqtypeQs").value as ArrayList<Long>)
                        if (snapshot.child("otherNxt").value == snapshot.child("creatorNxt").value)
                            listener.OnEqualListener(snapshot.child("creatorNxt").value as Long,
                                snapshot.child("otherNxt").value as Long,
                                questions)
                        else
                            listener.NotEqualListener(snapshot.child("creatorNxt").value as Long,
                                snapshot.child("otherNxt").value as Long,
                                questions)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    fun extractNames(creator:String, other:String ,listener: FirebaseNameCallback){
        profileRef.child(creator)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("name"))
                        listener.CreatorListener(snapshot.child("name").value.toString())
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        profileRef.child(other)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("name"))
                        listener.OtherListener(snapshot.child("name").value.toString())
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun onDestroy(createdBy:String){
        userRef.child(createdBy).setValue(null)
//        userPrefRef.child(createdBy).setValue(null)
    }

    interface FirebaseCallerCallback{
        fun OnSuccessListener(user: User?)
        fun OnCancelled(error: DatabaseError)
    }

    interface FirebaseCallerSnapshotCallback{
        fun OnSuccessListener(snapshot: DataSnapshot)
        fun OnCancelled(error: DatabaseError)
    }

    interface FirebaseCallerNextCallback{
        fun OnCreatorListener(value:Long)
        fun OnInCallerListener(value:Long)
    }

    interface FirebaseCallerEqualCallback{
        fun OnEqualListener(value: Long,otherValue:Long,question: QuestionSession)
        fun NotEqualListener(value: Long,otherValue:Long,question: QuestionSession)
    }

    interface FirebaseNameCallback{
        fun CreatorListener(name: String)
        fun OtherListener(name: String)
    }
}