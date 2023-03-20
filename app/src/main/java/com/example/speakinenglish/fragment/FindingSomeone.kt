package com.example.speakinenglish.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appyhigh.adutils.callbacks.NativeAdLoadCallback
import com.example.advertise.AdsManager
import com.example.api.FirebaseConnectingAPI
import com.example.api.model.User
import com.example.speakinenglish.R
import com.example.speakinenglish.activity.CallerActivity
import com.example.speakinenglish.activity.MainActivity
import com.example.speakinenglish.container.AppPref
import com.example.speakinenglish.util.RandomGenerate
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import kotlinx.android.synthetic.main.finding_someone_layout.*


class FindingSomeone : Fragment() {
    var isOkay = false
    var questionType = ""
    var activity: MainActivity? = null
    var gender = ""
    var level = ""
    lateinit var timer:CountDownTimer

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
//        AdsManager.requestNativeAd(
//            llAdView1,
//            com.example.advertise.R.layout.main_ad_template_view,
//            getString(R.string.ad_exit_native))
        context?.let {
            AdsManager.loadNativeAdFromService(
                context = it,
                lifecycle = lifecycle,
                layoutInflater = layoutInflater,
                adName = "ad_unit_finding",
                adUnit = getString(R.string.ad_finding_native),
                viewGroup = llAdView1,
                adType = AdsManager.ADType.MEDIUM,
                background = null, textColor1 = null, textColor2 = null,
                nativeAdLoadCallback = object :NativeAdLoadCallback(){
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                    }

                    override fun onAdFailed(adError: LoadAdError?) {
                        super.onAdFailed(adError)
                    }
                },
                preloadAds = true,
                autoRefresh = true,
                loadTimeOut = 5000
            )
        }

        val arguments = arguments
        if (arguments != null) {
            questionType = arguments.getString("type")!!
            gender = arguments.getString("gender").toString()
            level = arguments.getString("level").toString()
        }
        var username = Gson().fromJson(AppPref.getString(requireContext(),AppPref.user), User::class.java).id

        animationView.progress = 0.0f
        animationView.frame = 1
        animationView.setMinAndMaxFrame(1, 111)
        var millies = 60000*3
        timer = object : CountDownTimer(millies.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    animationView.frame = ((((millies-millisUntilFinished)*111)/millies).toInt())
                }
                catch (e:Exception){

                }
            }

            override fun onFinish() {
                (activity as MainActivity).onBackPressed()
                Toast.makeText(requireContext(),"Sorry no matched user found.",Toast.LENGTH_SHORT).show()
            }
        }.start()

        FirebaseConnectingAPI.removePreviousSession(Gson().fromJson(AppPref.getString(requireContext(),AppPref.user),User::class.java))

        FirebaseConnectingAPI.fetchUserWithStatus(gender,level,Gson().fromJson(AppPref.getString(requireContext(),AppPref.user),User::class.java)
            ,object :FirebaseConnectingAPI.FirebaseConnectingCallbackFirestore{
            override fun OnSuccessListener(snapshot: List<DocumentSnapshot>) {
                if(snapshot.size > 0) {
                    //get 1 user with status 0 as room is available
                    isOkay = true
                    for(childSnap in snapshot) {
                        //change status to 1 so that other user can move to caller screen
                        childSnap.id?.let { FirebaseConnectingAPI.roomAvailable(it,questionType,
                            RandomGenerate.getArray(getQuestionLimit(questionType),getQuestionsSize(questionType)),
                                username)
                        }

                        if (activity != null){
                            timer.cancel()
                            val intent = Intent(activity, CallerActivity::class.java)
                            val incoming = childSnap.data?.get("incoming") as String
                            val createdBy = childSnap.data?.get("createdBy") as String
                            val isAvailable = childSnap.data?.get("isAvailable") as Boolean
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
                        ,Gson().fromJson(AppPref.getString(requireContext(),AppPref.user),User::class.java)
                        ,object :FirebaseConnectingAPI.FirebaseConnectingCallback{
                        override fun OnSuccessListener(snapshot: DataSnapshot) {
                            if (isOkay) return

                            isOkay = true
                            if (activity != null){
                                timer.cancel()
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
            type.equals("questions") -> AppPref.getInt(requireContext(),AppPref.questions)!!
            type.equals("words") -> AppPref.getInt(requireContext(),AppPref.words)!!
            type.equals("grammar") -> AppPref.getInt(requireContext(),AppPref.grammar)!!
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