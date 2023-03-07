package com.example.speakinenglish.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.advertise.AdsManager
import com.example.api.FirebaseConnectingAPI
import com.example.api.model.User
import com.example.speakinenglish.R
import com.example.speakinenglish.activity.CallerActivity
import com.example.speakinenglish.activity.MainActivity
import com.example.speakinenglish.container.AppPrefs
import com.example.speakinenglish.util.RandomGenerate
import com.google.firebase.database.DataSnapshot
import com.google.gson.Gson
import kotlinx.android.synthetic.main.finding_someone_layout.*


class FindingSomeone : Fragment() {
    var isOkay = false
    var questionType = ""
    var activity: MainActivity? = null
    var gender = ""
    var level = ""

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.activity = (activity as MainActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finding_someone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AdsManager.requestNativeAd(
            llAdView1,
            com.example.advertise.R.layout.main_ad_template_view,
            getString(R.string.ad_exit_native))

        val arguments = arguments
        if (arguments != null) {
            questionType = arguments.getString("type")!!
//            gender = arguments.getString("gender").toString()
//            level = arguments.getString("level").toString()
        }
        var username = Gson().fromJson(AppPrefs.user.get(), User::class.java).id

        FirebaseConnectingAPI.removePreviousSession(Gson().fromJson(AppPrefs.user.get(),User::class.java))

        FirebaseConnectingAPI.fetchUserWithStatus(gender,level,Gson().fromJson(AppPrefs.user.get(),User::class.java)
            ,object :FirebaseConnectingAPI.FirebaseConnectingCallback{
            override fun OnSuccessListener(snapshot: DataSnapshot) {
                if(snapshot.getChildrenCount() > 0) {
                    //get 1 user with status 0 as room is available
                    isOkay = true
                    for(childSnap in snapshot.getChildren()) {
                        //change status to 1 so that other user can move to caller screen
                        childSnap.key?.let { FirebaseConnectingAPI.roomAvailable(it,questionType,
                            RandomGenerate.getArray(getQuestionLimit(questionType),getQuestionsSize(questionType)),
                                username) }

                        if (activity != null){
                            val intent = Intent(activity, CallerActivity::class.java)
                            val incoming = childSnap.child("incoming").getValue(
                                String::class.java
                            )
                            val createdBy = childSnap.child("createdBy").getValue(
                                String::class.java
                            )
                            val isAvailable = childSnap.child("isAvailable").getValue(
                                Boolean::class.java
                            )!!
                            intent.putExtra("username", username)
                            intent.putExtra("incoming", incoming)
                            intent.putExtra("createdBy", createdBy)
                            intent.putExtra("isAvailable", isAvailable)
                            intent.putExtra("type", questionType)
                            context?.startActivity(intent)
                        }
                    }
                }
                else{
                    //create user if room not available and wait for status change to 1
                    FirebaseConnectingAPI.roomNotAvailable(username,gender,level,questionType,
                        RandomGenerate.getArray(getQuestionLimit(questionType),getQuestionsSize(questionType))
                        ,Gson().fromJson(AppPrefs.user.get(),User::class.java)
                        ,object :FirebaseConnectingAPI.FirebaseConnectingCallback{
                        override fun OnSuccessListener(snapshot: DataSnapshot) {
                            if (isOkay) return

                            isOkay = true
                            if (activity != null){
                                val intent = Intent(activity, CallerActivity::class.java)
                                val incoming = snapshot.child("incoming").getValue(
                                    String::class.java
                                )
                                val createdBy = snapshot.child("createdBy").getValue(
                                    String::class.java
                                )
                                val isAvailable = snapshot.child("isAvailable").getValue(
                                    Boolean::class.java
                                )!!
                                intent.putExtra("username", username)
                                intent.putExtra("incoming", incoming)
                                intent.putExtra("createdBy", createdBy)
                                intent.putExtra("isAvailable", isAvailable)
                                intent.putExtra("type", questionType)
                                context?.startActivity(intent)
                            }
                        }

                        override fun OnFailureListener(error: Exception) {

                        }

                    })
                }
            }

            override fun OnFailureListener(error: Exception) {
            }
        })

    }


    fun getQuestionLimit(type:String):Int{
        return when{
            type.equals("questions") -> AppPrefs.questions.get()
            type.equals("words") -> AppPrefs.words.get()
            type.equals("grammar") -> AppPrefs.grammar.get()
            else-> 0
        }
    }

    fun getQuestionsSize(type:String):Int{
        return when{
            type.equals("questions") -> 2
            type.equals("words") -> 5
            type.equals("grammar") -> 2
            else-> 0
        }
    }

}