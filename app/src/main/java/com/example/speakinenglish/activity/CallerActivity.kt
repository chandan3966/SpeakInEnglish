package com.example.speakinenglish.activity

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.advertise.AdsManager
import com.example.advertise.callbacks.AdCallbacks
import com.example.api.FirebaseCallerAPI
import com.example.api.FirebaseCallerAPI.changeGrammerAnswerValues
import com.example.api.FirebaseCallerAPI.changeValues
import com.example.api.FirebaseCallerAPI.fetchSessionQuestions
import com.example.api.FirebaseCallerAPI.listenGrammaerAnsClick
import com.example.api.FirebaseCallerAPI.listenOtherClick
import com.example.api.FirebaseCallerAPI.resetGrammarAnswer
import com.example.api.interfaces.FirebaseCallerAPI.*
import com.example.api.model.Grammar
import com.example.api.model.QuestionSession
import com.example.api.model.User
import com.example.speakinenglish.R
import com.example.speakinenglish.container.AppPref
import com.example.speakinenglish.interfaces.EndCallDialogInterface
import com.example.speakinenglish.interfaces.InterfaceJava
import com.example.speakinenglish.util.CustomDialogClass
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_calling.*
import java.util.*


class CallerActivity : AppCompatActivity() {
    var questionType:String? = null
    lateinit var user:User

    var isPeerConnected = false

    var uniqueId = ""
    var username = ""
    var friendsUsername = ""
    var isAudio = false
    var isVideo = false
    var createdBy: String? = null
    var qtype:String = ""
    var qtypeQuestions:ArrayList<Int> = ArrayList<Int>()
    var otherqtype:String = ""
    var otherqtypeQuestions:ArrayList<Int> =  ArrayList<Int>()

    var questionCount:Long = -1
    var grammarAns:Boolean = false

    var IsCreator = false
    var IsCaller = false
    var IsAnswerShown = false

    var pageExit = false
    lateinit var audioManager: AudioManager

    var userFetchedName = ""
    var friendFetchedName = ""

    lateinit var questionList:ArrayList<String>
    lateinit var wordsList:ArrayList<String>
    lateinit var grammarList:ArrayList<Grammar>
    var adLoaded:Boolean = false

    override fun onStart() {
        super.onStart()
        AdsManager.requestInterstitial(object : AdCallbacks {
            override fun AdClicked() {

            }

            override fun AdClosed() {

            }

            override fun AdFailed() {
                adLoaded = false
            }

            override fun AdLoad() {
                adLoaded = true
            }
        }, getString(R.string.ad_exit_interstitial))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.speakinenglish.R.layout.activity_caller)

        questionList = Gson().fromJson(AppPref.getString(applicationContext,AppPref.questionsCache),object : TypeToken<ArrayList<String>?>() {}.type) as ArrayList<String>
        wordsList = Gson().fromJson(AppPref.getString(applicationContext,AppPref.wordsCache),object : TypeToken<ArrayList<String>?>() {}.type) as ArrayList<String>
        grammarList = Gson().fromJson(AppPref.getString(applicationContext,AppPref.grammarCache),object : TypeToken<ArrayList<Grammar>?>() {}.type) as ArrayList<Grammar>
        if (intent.hasExtra("type")){
            questionType = intent.getStringExtra("type")
        }
        username = intent.getStringExtra("username")!!
        val incoming = intent.getStringExtra("incoming")!!
        createdBy = intent.getStringExtra("createdBy")!!
        user = Gson().fromJson(AppPref.getString(applicationContext,AppPref.user), User::class.java)
        audioManager = (getSystemService(Context.AUDIO_SERVICE) as AudioManager?)!!

        friendsUsername = incoming

        questionListeners()

        Glide.with(applicationContext).load(user.avatar).into(self_image)
        self_name.text = user.name
        self_level.text = "Level:"+user.ownlevel

        setupWebView()

        FirebaseCallerAPI.extractNames(username,friendsUsername,object : FirebaseNameCallback {
            override fun CreatorListener(name: String) {
                userFetchedName = name
            }

            override fun OtherListener(name: String) {
                friendFetchedName = name
            }
        })

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
            webView.loadUrl("https://www.google.com/")
            val cdd = CustomDialogClass(this@CallerActivity,object :EndCallDialogInterface{
                override fun onReported() {

                    pageExit = true
                    connectAudio(false)
                    if (createdBy?.equals(user.id) == true)
                        FirebaseCallerAPI.onDestroy(user.id)
                    else
                        FirebaseCallerAPI.onDestroy(friendsUsername)
                    MainActivity.listener?.backListener()
                    finish()
                }

                override fun onEnded(rating: Float) {
                    onClickStop()
                    onBackPressed()
                    if (adLoaded){
                        AdsManager.showInterstitial(this@CallerActivity)
                    }
                }

            })
            cdd.show()
        }
        runTimer()

        next_btn.setOnClickListener {
            if (questionCount<=5)
                changeValues(createdBy!!,username,questionCount,object :
                    FirebaseCallerNextCallback {
                    override fun OnCreatorListener(value: Long) {
                        questionCount = value.toLong()
                    }

                    override fun OnInCallerListener(value: Long) {
                        questionCount = value.toLong()
                    }

                })
        }


    }

    fun questionListeners(){
        fetchSessionQuestions(createdBy!!,object : FirebaseCallerSnapshotCallback {
            override fun OnSuccessListener(snapshot: DataSnapshot) {
                if (snapshot.hasChild("qtype") && snapshot.hasChild("qtypeQs")){
                    qtype = snapshot.child("qtype").value.toString()
                    qtypeQuestions = snapshot.child("qtypeQs").value as ArrayList<Int>
                }
                if(snapshot.hasChild("otherqtype") && snapshot.hasChild("otherqtypeQs")){
                    otherqtype = snapshot.child("otherqtype").value.toString()
                    otherqtypeQuestions = snapshot.child("otherqtypeQs").value as ArrayList<Int>
                }
            }

            override fun OnCancelled(error: DatabaseError) {
            }
        })

        listenOtherClick(createdBy!!,object : FirebaseCallerEqualCallback {

            override fun OnEqualListener(value: Long, otherValue: Long, question: QuestionSession) {
                if(IsAnswerShown){
                    next_btn.isClickable = true
                    answer.visibility = View.GONE
                    answer_btn.isClickable = true
                    resetGrammarAnswer(createdBy!!,false,object : FirebaseCallerNextAnswerCallback {

                        override fun OnListener(value: Boolean) {
                            grammarAns = value
                        }

                    })
                }
                if (value <= 2){
                    activity_num.text = "Activity ${value+1}"
                    assignQuestionUI(question.creatorType,question.creatorQns as ArrayList<Any>,value.toInt())
                }
                else if (value > 2 && value <= 5){
                    activity_num.text = "Activity ${value+1}"
                    assignQuestionUI(question.otherType,question.otherQns as ArrayList<Any>,otherValue.toInt()%3)
                }
            }

            override fun NotEqualListener(
                value: Long,
                otherValue: Long,
                question: QuestionSession
            ) {
                if (value > otherValue && IsCreator){
                    next_btn.isClickable = false
                }
                else if (value < otherValue && IsCaller){
                    next_btn.isClickable = false
                }
            }

        })

        answer.visibility = View.GONE
        answer_btn.visibility = View.GONE
        listenGrammaerAnsClick(createdBy!!,object :FirebaseCallerGrammarEqualCallback{
            override fun OnEqualListener(
                value: Long,
                otherValue: Long,
                valueAns: Boolean,
                otherValueAns: Boolean,
                question: QuestionSession
            ) {
                if (value <= 2 && question.creatorType.equals("grammar") && !IsAnswerShown){
                    answer.visibility = View.VISIBLE
                    IsAnswerShown = true
                    answer_btn.isClickable = false
                    answer.text = "Answer:  ${grammarList.get((question.creatorQns as ArrayList<Long>).get(value.toInt()).toInt()).grammar_key}"
                }
                else if (value > 2 && value <= 5 && question.otherType.equals("grammar") && !IsAnswerShown){
                    answer.visibility = View.VISIBLE
                    IsAnswerShown = true
                    answer_btn.isClickable = false
                    answer.text = "Answer:  ${grammarList.get((question.otherQns as ArrayList<Long>).get(otherValue.toInt()%3).toInt()).grammar_key}"
                }
            }

            override fun NotEqualListener(
                value: Long,
                otherValue: Long,
                valueAns: Boolean,
                otherValueAns: Boolean,
                question: QuestionSession
            ) {
                IsAnswerShown = false
                answer.visibility = View.GONE
            }

        })

        changeValues(createdBy!!,username,questionCount,object :FirebaseCallerNextCallback{
            override fun OnCreatorListener(value: Long) {
                questionCount = value.toLong()
            }

            override fun OnInCallerListener(value: Long) {
                questionCount = value.toLong()
            }

        })
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
        webView.addJavascriptInterface(InterfaceJava(this), "Android")
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
            FirebaseCallerAPI.initilizePeerIfSame(
                username,
                uniqueId,
                friendsUsername,
                object : FirebaseCallerCallback {
                    override fun OnSuccessListener(user: User?) {
                        if (user != null) {
                            //call initiator
                            IsCreator = true
                            Glide.with(applicationContext).load(user.avatar)
                                .into(others_image)
                            others_name.setText(user.getName())
                            others_level.setText("Level:" + user.getOwnlevel())
                            FirebaseCallerAPI.listenOtherConnId(username,object :FirebaseCallerSnapshotCallback{
                                override fun OnSuccessListener(snapshot: DataSnapshot) {
                                    if (snapshot.value == null) {
                                        try {
                                            endCall.performClick()
                                        }
                                        catch (e:Exception){
                                            e.printStackTrace()
                                        }
                                        return
                                    }
                                }

                                override fun OnCancelled(error: DatabaseError) {
                                    Log.d("CallerActivity", "OnCancelled: "+error.message)
                                }

                            })
                        }
                    }

                    override fun OnCancelled(error: DatabaseError) {
                        Log.d("CallerActivity", "OnCancelled: "+error.message)
                    }

                })
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                friendsUsername = createdBy.toString()
                FirebaseCallerAPI.initilizePeerIfNotSame(friendsUsername,
                    object : FirebaseCallerCallback {
                        override fun OnSuccessListener(user: User?) {
                            if (user != null) {
                                IsCaller = true
                                Glide.with(applicationContext).load(user.avatar)
                                    .into(others_image)
                                others_name.setText(user.getName())
                                others_level.setText("Level:" + user.getOwnlevel())
                            } else {
                                sendCallRequest()
                            }
                        }

                        override fun OnCancelled(error: DatabaseError) {
                            Log.d("CallerActivity", "OnCancelled: "+error.message)
                        }

                    })
            }, 3000)
        }
    }

    fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(
                this,
                "You are not connected. Please check your internet.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        FirebaseCallerAPI.listenConnId(friendsUsername,object :FirebaseCallerSnapshotCallback{
            override fun OnSuccessListener(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    try {
                        endCall.performClick()
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }
                    return
                }
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

    override fun onBackPressed() {
        pageExit = true
        connectAudio(false)
        if (createdBy?.equals(user.id) == true)
            FirebaseCallerAPI.onDestroy(user.id)
        else
            FirebaseCallerAPI.onDestroy(friendsUsername)
        MainActivity.listener?.backListener()
        finish()
    }

    @JvmName("getUniqueId1")
    fun getUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    fun assignQuestionUI(questionType: String, questions:ArrayList<Any>,quesNum:Int){
        if (questionType.equals("questions")){
            generateQuestionLayout(
                questionList.get((questions as ArrayList<Long>).get(quesNum).toInt())
            )
        }
        else if(questionType.equals("words")){
            var quesCount = quesNum * 2
            generateWordLayout(
                wordsList.get((questions as ArrayList<Long>).get(quesCount).toInt()),
                wordsList.get((questions as ArrayList<Long>).get(quesCount+1).toInt())
            )
        }
        else if(questionType.equals("grammar")){
            generateGrammarLayout(
                grammarList.get((questions as ArrayList<Long>).get(quesNum).toInt())
            )
        }
    }


    fun generateWordLayout(word1:String,
                           word2:String){
        answer_btn.visibility = View.GONE
        activity_title.text = "Guess the word game"
        activity_question.text = HtmlCompat.fromHtml("${if (IsCreator) userFetchedName else friendFetchedName} should describe the word without saying it <br> <b>-${if (IsCreator) word1 else "******"}</b> <br>and ${if (IsCreator) friendFetchedName else userFetchedName} should guess it <br> <br> <br>"+
                "${if (IsCreator) friendFetchedName else userFetchedName} should describe the word without saying it <br> <b>-${if (IsCreator) "******" else word2}</b> <br>and ${if (IsCreator) userFetchedName else friendFetchedName} should guess it <br> <br>",HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    fun generateQuestionLayout(question:String){
        activity_title.text = "Talk about the topic"
        activity_question.text = HtmlCompat.fromHtml("<b>-${question}</b>",HtmlCompat.FROM_HTML_MODE_COMPACT)
        answer_btn.visibility = View.GONE
    }

    fun generateGrammarLayout(grammar: Grammar){
        answer_btn.visibility = View.VISIBLE
        activity_title.text = "Answer the following Question"
        var quesString = grammar.grammar_question.toString()
        var its = 0
        var occurCount = getOccurances(quesString,')')
        quesStringnew = ""
        if (quesStringnew.equals(""))
            for (i in 0..occurCount){
                if (its == 0){
                    iteration(quesString,0)
                    its++
                }
                else
                    iteration(quesStringnew,its+1)
            }
        activity_question.text = HtmlCompat.fromHtml("${quesStringnew}",HtmlCompat.FROM_HTML_MODE_COMPACT)
        answer_btn.setOnClickListener {
            answer_btn.isClickable = false
            changeGrammerAnswerValues(createdBy!!,username,true,object :FirebaseCallerNextAnswerCallback{

                override fun OnListener(value: Boolean) {
                    grammarAns = value
                }

            })
        }
    }

    var quesStringnew = ""
    fun iteration(quesString:String,insertAt: Int){
        var index: Int = if (quesStringnew.equals("")) quesString.indexOf(")",insertAt) else quesStringnew.indexOf(")")
        if (index != -1){
            quesStringnew = quesString.insert(index-1,"<br>")
            quesStringnew = quesStringnew.replaceFirst(")","(")
        }
        else{
            quesStringnew = quesStringnew.replace("(",")")
        }
    }

    fun getOccurances(input:String,char: Char):Int{
        var count = 0
        for (i in 0 until input.length) {
            if (input.toCharArray().get(i) == char)
                count++
        }
        return count
    }

    fun String.insert(insertAt: Int, string: String): String {
        return this.substring(0, insertAt) + string + this.substring(insertAt, this.length)
    }

    private var seconds = 0

    // Is the stopwatch running?
    private var running = false

    private val wasRunning = false

    fun onClickStart(view: View?) {
        running = true
    }

    // Stop the stopwatch running
    // when the Stop button is clicked.
    // Below method gets called
    // when the Stop button is clicked.
    fun onClickStop() {
        running = false
    }

    // Reset the stopwatch when
    // the Reset button is clicked.
    // Below method gets called
    // when the Reset button is clicked.
    fun onClickReset(view: View?) {
        running = false
        seconds = 0
    }

    val handler = Handler(Looper.getMainLooper())
    private fun runTimer() {

        running = true
        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60

                // Format the seconds into hours, minutes,
                // and seconds.
                val time = String.format(
                    Locale.getDefault(),
                    "%d:%02d:%02d", hours,
                    minutes, secs
                )

                // Set the text view text.
                runOnUiThread {
                    callTimer.text = time
                }

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000)
            }
        })
    }
}