package com.example.speakinenglish.fragment

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.advertise.AdsManager
import com.example.api.FirebaseCallerAPI
import com.example.api.FirebaseCallerAPI.initilizePeerIfNotSame
import com.example.api.FirebaseCallerAPI.initilizePeerIfSame
import com.example.api.FirestoreQuestionApi
import com.example.api.interfaces.FirebaseCallerAPI.FirebaseCallerCallback
import com.example.api.interfaces.FirebaseCallerAPI.FirebaseCallerSnapshotCallback
import com.example.api.model.Grammar
import com.example.api.model.User
import com.example.speakinenglish.R
import com.example.speakinenglish.interfaces.InterfaceJava
import com.example.speakinenglish.util.RandomGenerate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_calling.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [CallingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallingFragment : Fragment() {
    var questionType:String? = null

    var isPeerConnected = false

    var uniqueId = ""
    var username = ""
    var friendsUsername = ""
    var isAudio = false
    var isVideo = false
    var createdBy: String? = null

    var pageExit = false
    lateinit var audioManager: AudioManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AdsManager.loadBannerAd(adView1)

        audioManager = (requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager?)!!
        val arguments = arguments
        if (arguments?.getString("type") != null){
            questionType = arguments.getString("type")!!
        }
        username = (if (arguments?.getString("username") != null) arguments?.getString("username") else "").toString()
        val incoming: String = (if (arguments?.getString("incoming") != null) arguments?.getString("incoming") else "").toString()
        createdBy = (if (arguments?.getString("type") != null) arguments?.getString("createdBy") else "").toString()

        friendsUsername = incoming

        setupWebView()

        mic.setOnClickListener {
            isAudio = !isAudio
//            callJavaScriptFunction("javascript:toggleAudio(\"$isAudio\")")
            if (isAudio) {
                mic.setImageResource(R.drawable.speaker_active)
                audioManager!!.mode = AudioManager.MODE_IN_CALL
                audioManager!!.mode = AudioManager.MODE_NORMAL
            } else {
                mic.setImageResource(R.drawable.speaker_inactive)
                audioManager!!.mode = AudioManager.MODE_NORMAL
                audioManager!!.mode = AudioManager.MODE_IN_CALL
            }
            audioManager!!.isSpeakerphoneOn = isAudio
        }
        webView.post {
            callJavaScriptFunction("javascript:toggleVideo(\"$isVideo\")")
            connectAudio(true)
        }

        endCall.setOnClickListener {
            connectAudio(false)
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pageExit = true
        createdBy?.let { FirebaseCallerAPI.onDestroy(it) }
    }

    fun setupWebView() {
        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.resources)
                }
            }
        })
        webView.getSettings().setJavaScriptEnabled(true)
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false)
//        webView.addJavascriptInterface(InterfaceJava(this@CallingFragment), "Android")
        loadVideoCall()
    }

    fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        webView.loadUrl(filePath)
        webView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                initializePeer()
            }
        })
    }

    fun onPeerConnected() {
        isPeerConnected = true
    }

    fun onPeerDisconnected() {
        Log.d("TAG", "onPeerDisconnected: ")
    }

    fun initializePeer() {
        uniqueId = getUniqueId()
        callJavaScriptFunction("javascript:init(\"$uniqueId\")")
        if (createdBy.equals(username, ignoreCase = true)) {
            if (pageExit) return
            initilizePeerIfSame(username,uniqueId,friendsUsername,object : FirebaseCallerCallback {
                override fun OnSuccessListener(user: User?) {
                    if (user !=null){
                        Glide.with(requireContext()).load(user.avatar)
                            .into(others_image)
                        others_name.setText(user.getName())
                        others_level.setText("Level:"+user.getOwnlevel())
                    }
                }

                override fun OnCancelled(error: DatabaseError) {
                }

            })
        } else {
            Handler().postDelayed({
                friendsUsername = createdBy.toString()
                initilizePeerIfNotSame(friendsUsername,object :FirebaseCallerCallback{
                    override fun OnSuccessListener(user: User?) {
                        if (user != null){
                            Glide.with(requireContext()).load(user.avatar)
                                .into(others_image)
                            others_name.setText(user.getName())
                            others_level.setText("Level:"+user.getOwnlevel())
                        }
                        else{
                            sendCallRequest()
                        }
                    }

                    override fun OnCancelled(error: DatabaseError) {
                    }

                })
            }, 3000)
        }
    }

    fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(
                context,
                "You are not connected. Please check your internet.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        FirebaseCallerAPI.listenConnId(friendsUsername,object : FirebaseCallerSnapshotCallback {
            override fun OnSuccessListener(snapshot: DataSnapshot) {
                if (snapshot.value == null) return
                val connId = snapshot.getValue(String::class.java)
                callJavaScriptFunction("javascript:startCall(\"$connId\")")
            }

            override fun OnCancelled(error: DatabaseError) {
            }

        })
    }

    fun callJavaScriptFunction(function: String?) {
        webView.post(Runnable {
            if (function != null) {
                webView.evaluateJavascript(function, null)
            }
        })
    }

    fun connectAudio(connect:Boolean){
        if (connect)
            callJavaScriptFunction("javascript:toggleAudio(\"$connect\")")
        else
            callJavaScriptFunction("javascript:toggleAudio(\"$connect\")")
    }

    @JvmName("getUniqueId1")
    fun getUniqueId(): String {
        return UUID.randomUUID().toString()
    }
}