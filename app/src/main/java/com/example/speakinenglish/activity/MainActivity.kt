package com.example.speakinenglish.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.advertise.AdsManager
import com.example.advertise.callbacks.AdCallbacks
import com.example.api.FireStoreApi
import com.example.api.FireStoreCallback
import com.example.api.FirebaseCallerAPI
import com.example.api.FirestoreQuestionApi
import com.example.api.model.Grammar
import com.example.api.model.User
import com.example.speakinenglish.BuildConfig
import com.example.speakinenglish.R
import com.example.speakinenglish.adapters.HomePagerAdapter
import com.example.speakinenglish.container.AppPrefs
import com.example.speakinenglish.databinding.ActivityMainBinding
import com.example.speakinenglish.fragment.*
import com.example.speakinenglish.fragment.LoginFragment.Companion.USER
import com.example.speakinenglish.util.RandomGenerate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import kotlinx.android.synthetic.main.main_layout.*


class MainActivity : AppCompatActivity() , CallerCallback {
    private val remoteConfig = Firebase.remoteConfig
    private var ad_fetch_interval:Long = 4500
    var fragmentList = ArrayList<Fragment>()
    lateinit var binding: ActivityMainBinding
    var adLoaded:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppPrefs.load(applicationContext)
        AppPrefs.deviceid.set(
            Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID))
        AppPrefs.commit(applicationContext)
        MainActivity.listener = this
        mainUser = Gson().fromJson(AppPrefs.user.get(),User::class.java)

        if (!AppPrefs.user.get().equals("")){
            val gson = Gson()
            USER = gson.fromJson(AppPrefs.user.get(), User::class.java)
        }
        Log.d("Main", "onCreate: "+AppPrefs.deviceid.get())


        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 1 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var long = remoteConfig.getLong("ad_interval")
                    MAX_AVATARS = remoteConfig.getLong("max_avatars").toInt()
                    AppPrefs.words.set(remoteConfig.getLong("words").toInt())
                    AppPrefs.commit(applicationContext)
                    AppPrefs.questions.set(remoteConfig.getLong("questions").toInt())
                    AppPrefs.commit(applicationContext)
                    AppPrefs.grammar.set(remoteConfig.getLong("grammar").toInt())
                    AppPrefs.commit(applicationContext)
                    if (long != null){
                        ad_fetch_interval = long
                    }
                    Log.d("remoteconfig", "onCreate: "+ad_fetch_interval)
                    AdsManager.requestInterstitial(object : AdCallbacks{
                        override fun AdClicked() {

                        }

                        override fun AdClosed() {
                            isUserlogin()
                        }

                        override fun AdFailed() {
                            isUserlogin()
                        }

                        override fun AdLoad() {
                            adLoaded = true
                        }
                    }, getString(R.string.ad_splash_interstitial))

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(Runnable {
                        binding.splash.button.visibility = View.VISIBLE
                    }, ad_fetch_interval)
                }
            }

        binding.splash.button.setOnClickListener {
            if (adLoaded)
                AdsManager.showInterstitial(this)
            else{
                isUserlogin()
            }
        }
        fetchQuestions()
    }

    fun fetchQuestions(){
        if (AppPrefs.questionsCache.get().equals(""))
            FirestoreQuestionApi
                .getQuestion(object : FirestoreQuestionApi.QuestionCallback{
                        override fun OnSuccessListener(objects: Any) {
                            if (objects is java.util.ArrayList<*>){
                                AppPrefs.questionsCache.set(Gson().toJson(objects as java.util.ArrayList<String>))
                                AppPrefs.commit(applicationContext)
                                Log.d("Main", "OnSuccessListener: "+AppPrefs.questionsCache.get())
                            }
                        }

                        override fun OnCancelled(error: com.google.firebase.database.DatabaseError) {

                        }

                    })
        if (AppPrefs.wordsCache.get().equals(""))
            FirestoreQuestionApi
                .getWords(object : FirestoreQuestionApi.QuestionCallback{
                        override fun OnSuccessListener(objects: Any) {
                            if (objects is java.util.ArrayList<*>){
                                AppPrefs.wordsCache.set(Gson().toJson(objects as java.util.ArrayList<String>))
                                AppPrefs.commit(applicationContext)
                                Log.d("Main", "OnSuccessListener: "+AppPrefs.wordsCache.get())
                            }
                        }

                        override fun OnCancelled(error: DatabaseError) {

                        }

                    })
        if (AppPrefs.grammarCache.get().equals(""))
            FirestoreQuestionApi
                .getGrammarQuestion(object : FirestoreQuestionApi.QuestionCallback{
                        override fun OnSuccessListener(objects: Any) {
                            if (objects is ArrayList<*>){
                                AppPrefs.grammarCache.set(Gson().toJson(objects as ArrayList<Grammar>))
                                AppPrefs.commit(applicationContext)
                                Log.d("Main", "OnSuccessListener: "+AppPrefs.grammarCache.get())
                            }
                        }

                        override fun OnCancelled(error: DatabaseError) {

                        }

                    })
    }

    override fun onBackPressed() {
        if (!binding.mainHolder.isVisible){
            binding.container.visibility = View.GONE
            mainUser?.let { FirebaseCallerAPI.onDestroy(it.id) }
            showMain()
            changePage(0)
        }
        else if (!binding.splash.root.isVisible){
            AdsManager.requestInterstitial(object : AdCallbacks{
                override fun AdClicked() {

                }

                override fun AdClosed() {
                    showExitPopup()
                }

                override fun AdFailed() {
                    showExitPopup()
                }

                override fun AdLoad() {
                    AdsManager.showInterstitial(this@MainActivity)
                }
            },getString(R.string.ad_exit_interstitial))
        }
    }

    private fun setUpViewPager() {
        try {
            fragmentList = ArrayList<Fragment>()
            fragmentList.add(SpeakingHome())
            fragmentList.add(CallHistoryFragment())
            fragmentList.add(VocabFragment())
            fragmentList.add(TestingFirestore())
            binding.main.vpHome.adapter = HomePagerAdapter(this, fragmentList)
            binding.main.vpHome.setCurrentItem(0, false)
            binding.main.vpHome.isUserInputEnabled = false
            binding.main.vpHome.offscreenPageLimit = 4

            binding.main.bottomNav.speak.setOnClickListener {
                changePage(0)
            }

            binding.main.bottomNav.history.setOnClickListener {
                changePage(1)
            }

            binding.main.bottomNav.dictionary.setOnClickListener {
                changePage(2)
            }

            binding.main.bottomNav.testing.setOnClickListener {
                changePage(3)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showLogin(login:Boolean){
        if (login){
            binding.splash.root.visibility = View.GONE
            binding.main.root.visibility = View.GONE
            binding.loginHolder.visibility = View.VISIBLE
            binding.container.visibility = View.GONE
            val ft: FragmentTransaction =
                supportFragmentManager.beginTransaction()
            ft.replace(
                R.id.login_container,
                LoginFragment(),
                "LoginFragment"
            )
            ft.commit()
        }
        else{
            binding.splash.root.visibility = View.GONE
            binding.main.root.visibility = View.VISIBLE
            binding.loginHolder.visibility = View.GONE
            binding.container.visibility = View.GONE
            showMain()
            changePage(0)
            setUpViewPager()
        }
    }

    private fun isUserlogin(){
        if (AppPrefs.loggedIn.get()){
            showLogin(false)
        }
        else {
            FireStoreApi.hasUser(AppPrefs.deviceid.get(),object :FireStoreCallback{
                override fun OnSuccessListener(snapshot: DataSnapshot?) {
                    if (snapshot?.getValue(User::class.java) == null){
                        showLogin(true)
                        return
                    }
                    else{
                        showLogin(false)
                        USER = snapshot?.getValue(User::class.java)
                        AppPrefs.user.set(USER!!.toJsonString(USER))
                        AppPrefs.commit(applicationContext)
                        AppPrefs.loggedIn.set(true)
                        AppPrefs.commit(applicationContext)
                        return
                    }
                }

                override fun OnFailureListener(e: Exception) {
                    e.printStackTrace()
                    return
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val currentFragment = fragmentList[vpHome.currentItem]
        if (currentFragment is SpeakingHome){
            if (requestCode == (currentFragment as SpeakingHome).requestCode){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    (currentFragment as SpeakingHome).showChooserActivity()
                }
            }
        }

    }

    fun changePage(pos: Int) {
        try {
            binding.main.vpHome.setCurrentItem(pos, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideMain(){
        binding.mainHolder.visibility = View.GONE
    }

    fun showMain(){
        binding.mainHolder.visibility = View.VISIBLE
    }

    fun showExitPopup(){
        val appExitBottomSheet = AppExitBottomSheetDialog.newInstance(onAppExit)
        appExitBottomSheet.show(supportFragmentManager, "appExitBottomSheet")
    }

    fun hideKeyboard(){
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    interface ExitAppListener {
        fun onAppExit()
    }

    private val onAppExit = object : ExitAppListener {
        override fun onAppExit() {
            finish()
        }
    }

    companion object{
        var MAX_AVATARS = 90
        var listener:CallerCallback? = null
        var mainUser:User? = null
    }

    override fun backListener() {
        if (AppPrefs.loggedIn.get())
            showLogin(false)
    }



}

interface CallerCallback{
    fun backListener()
}
