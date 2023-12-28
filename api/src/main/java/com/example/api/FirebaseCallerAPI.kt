package com.example.api

import com.example.api.interfaces.FirebaseCallerAPI.*
import com.example.api.model.QuestionSession
import com.example.api.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

object FirebaseCallerAPI {
    final val TAG = "FirebaseCallerAPI"

    val profileRef = FirebaseDatabase.getInstance().getReference().child("profiles")

    val userRef = FirebaseDatabase.getInstance().getReference().child("users")
    val userRefStore = FirebaseFirestore.getInstance().collection("users")


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

    fun changeGrammerAnswerValues(createdBy: String,username: String,value:Boolean,listener:FirebaseCallerNextAnswerCallback){
        if (createdBy.equals(username, ignoreCase = true)){
            userRef.child(createdBy)
                .child("creatorAns")
                .setValue(value)
                .addOnSuccessListener {
                    listener.OnListener(value)
                }
        }
        else{
            userRef.child(createdBy)
                .child("otherAns")
                .setValue(value)
                .addOnSuccessListener {
                    listener.OnListener(value)
                }
        }
    }

    fun resetGrammarAnswer(createdBy: String,value:Boolean,listener:FirebaseCallerNextAnswerCallback){
        userRef.child(createdBy)
            .child("creatorAns")
            .setValue(value)
            .addOnSuccessListener {
                listener.OnListener(value)
            }
        userRef.child(createdBy)
            .child("otherAns")
            .setValue(value)
            .addOnSuccessListener {
                listener.OnListener(value)
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

    fun listenGrammaerAnsClick(createdBy: String, listener: FirebaseCallerGrammarEqualCallback){
        userRef.child(createdBy)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("creatorNxt") && snapshot.hasChild("otherNxt") &&
                        snapshot.hasChild("creatorAns") && snapshot.hasChild("otherAns") &&
                        snapshot.hasChild("qtype") &&
                        snapshot.hasChild("qtypeQs") &&
                        snapshot.hasChild("otherqtype") &&
                        snapshot.hasChild("otherqtypeQs")){


                        var questions = QuestionSession(snapshot.child("qtype").value.toString(),
                            snapshot.child("qtypeQs").value as ArrayList<Long>,
                            snapshot.child("otherqtype").value.toString(),
                            snapshot.child("otherqtypeQs").value as ArrayList<Long>)

                        if (snapshot.child("creatorAns").value == true && snapshot.child("otherAns").value == true)
                            listener.OnEqualListener(
                                snapshot.child("creatorNxt").value as Long,
                                snapshot.child("otherNxt").value as Long,
                                snapshot.child("creatorAns").value as Boolean,
                                snapshot.child("otherAns").value as Boolean,
                                questions
                            )
                        else
                            listener.NotEqualListener(
                                snapshot.child("creatorNxt").value as Long,
                                snapshot.child("otherNxt").value as Long,
                                snapshot.child("creatorAns").value as Boolean,
                                snapshot.child("otherAns").value as Boolean,
                                questions
                            )
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

    fun setRating(user:String,rating: Float,listener : FirebaseCallerFinishValueCallback){
        profileRef.child(user).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue(User::class.java) != null){
                    var userTemp = snapshot.getValue(User::class.java) as User
                    var ratings = userTemp.getRating()
                    ratings.add(rating as Double)
                    userTemp.setRating(ratings)
                    profileRef.child(user).setValue(userTemp)
                        .addOnSuccessListener {
                            listener.onComplete()
                        }
                        .addOnFailureListener {
                            listener.onComplete()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun setReport(user:String,listener : FirebaseCallerFinishValueCallback){
        profileRef.child(user).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue(User::class.java) != null){
                    var userTemp = snapshot.getValue(User::class.java) as User
                    var reports = userTemp.getReported()
                    reports++
                    userTemp.setReported(reports)
                    profileRef.child(user).setValue(userTemp)
                        .addOnSuccessListener {
                            listener.onComplete()
                        }
                        .addOnFailureListener {
                            listener.onComplete()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun onDestroy(createdBy:String){
        userRef.child(createdBy).setValue(null)
        userRefStore.document(createdBy).delete()
//        userPrefRef.child(createdBy).setValue(null)
    }











}