package com.example.speakinenglish.activity

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.appyhigh.adutils.models.PreloadNativeAds
import com.example.advertise.AdsManager
import com.example.advertise.callbacks.AdCallbacks
import com.example.api.*
import com.example.api.model.User
import com.example.speakinenglish.BuildConfig
import com.example.speakinenglish.R
import com.example.speakinenglish.adapters.HomePagerAdapter
import com.example.speakinenglish.container.AppPref
import com.example.speakinenglish.databinding.ActivityMainBinding
import com.example.speakinenglish.fragment.*
import com.example.speakinenglish.fragment.LoginFragment.Companion.USER
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import kotlinx.android.synthetic.main.main_layout.*
import java.util.*


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
        Auth.signinAnonymously(this@MainActivity)
        AppPref.getInstance(applicationContext)
        AppPref.put(applicationContext,AppPref.deviceid,Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID))
        MainActivity.listener = this
        mainUser = Gson().fromJson(AppPref.getString(applicationContext,AppPref.user),User::class.java)

        if (!AppPref.getString(applicationContext,AppPref.user).equals("")){
            val gson = Gson()
            USER = gson.fromJson(AppPref.getString(applicationContext,AppPref.user), User::class.java)
        }
        Log.d("Main", "onCreate: "+AppPref.getString(applicationContext,AppPref.deviceid))
        val preloadingNativeAdList = hashMapOf<String, PreloadNativeAds>()
        preloadingNativeAdList.put(
            "ad_unit_home",
            PreloadNativeAds(
                getString(R.string.ad_home_native),
                "ad_unit_home",
                AdsManager.ADType.MEDIUM,
                loadTimeOut = 4000
            )
        )
        preloadingNativeAdList.put(
            "ad_unit_finding",
            PreloadNativeAds(
                getString(R.string.ad_finding_native),
                "ad_unit_finding",
                AdsManager.ADType.MEDIUM,
                loadTimeOut = 4000
            )
        )
        preloadingNativeAdList.put(
            "ad_unit_exit",
            PreloadNativeAds(
                getString(R.string.ad_exit_native),
                "ad_unit_exit",
                AdsManager.ADType.MEDIUM,
                loadTimeOut = 4000
            )
        )
        var testDeviceIds:List<String>? = null
        if (BuildConfig.DEBUG){
            testDeviceIds = Arrays.asList(Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID))
        }

        AdsManager.initialize(
            application,
            testDevice = if (testDeviceIds != null) testDeviceIds?.get(0) else null,
            preloadingNativeAdList = preloadingNativeAdList,
            fetchingCallback = object : AdsManager.FetchingCallback {
                override fun OnComplete() {
                    AdsManager.requestInterstitial(object : AdCallbacks{
                        override fun AdClicked() {

                        }

                        override fun AdClosed() {
                            isUserlogin()
                        }

                        override fun AdFailed() {
//                            isUserlogin()
                            adLoaded = false
                        }

                        override fun AdLoad() {
                            adLoaded = true
                        }
                    }, getString(R.string.ad_splash_interstitial))


                    AdsManager.attachAppOpenAdManager(
                        getString(R.string.ad_appopen),
                        "ad_appopen",
                        backgroundThreshold = 5000
                    )
                }
            }
        )


        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 1 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var long = remoteConfig.getLong("ad_interval")
                    MAX_AVATARS = remoteConfig.getLong("max_avatars").toInt()
                    AppPref.put(applicationContext,AppPref.words,remoteConfig.getLong("words").toInt())
                    AppPref.put(applicationContext,AppPref.questions,remoteConfig.getLong("questions").toInt())
                    AppPref.put(applicationContext,AppPref.grammar,remoteConfig.getLong("grammar").toInt())
                    if (long != null){
                        ad_fetch_interval = long
                    }
                    Log.d("remoteconfig", "onCreate: "+ad_fetch_interval)

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
        if (AppPref.getString(applicationContext,AppPref.questionsCache).equals(""))
            FirestoreQuestionApi
                .getQuestion(object : FirestoreQuestionApi.QuestionCallback{
                        override fun OnSuccessListener(objects: Any) {
                            if (objects is java.util.ArrayList<*>){
                                AppPref.put(applicationContext,AppPref.questionsCache,Gson().toJson(objects as java.util.ArrayList<String>))
                            }
                        }

                        override fun OnCancelled(error: com.google.firebase.database.DatabaseError) {

                        }

                    })
        if (AppPref.getString(applicationContext,AppPref.wordsCache).equals(""))
            FirestoreQuestionApi
                .getWords(object : FirestoreQuestionApi.QuestionCallback{
                        override fun OnSuccessListener(objects: Any) {
                            if (objects is java.util.ArrayList<*>){
                                AppPref.put(applicationContext,AppPref.wordsCache,Gson().toJson(objects as java.util.ArrayList<String>))
                            }
                        }

                        override fun OnCancelled(error: DatabaseError) {

                        }

                    })
        if (AppPref.getString(applicationContext,AppPref.grammarCache).equals(""))
            FirestoreQuestionApi
                .getGrammarQuestion(object : FirestoreQuestionApi.QuestionCallback{
                        override fun OnSuccessListener(objects: Any) {
                            if (objects is ArrayList<*>){
                                AppPref.put(applicationContext,AppPref.grammarCache,Gson().toJson(objects as java.util.ArrayList<String>))
                            }
                        }

                        override fun OnCancelled(error: DatabaseError) {

                        }

                    })
    }

    override fun onBackPressed() {
        if (!binding.mainHolder.isVisible){
            val f: Fragment? =
                supportFragmentManager.findFragmentByTag("FindingSomeone")
            if (f is FindingSomeone){
                (f as FindingSomeone).timer.cancel()
            }
            binding.container.visibility = View.GONE
            if (mainUser == null)
                USER.let { it?.id?.let { it1 -> FirebaseCallerAPI.onDestroy(it1) } }
            else
                mainUser?.let { FirebaseCallerAPI.onDestroy(it.id) }
            showMain()
            changePage(0)
        }
        else if (!binding.splash.root.isVisible){
            showExitPopup()
        }
    }

    private fun setUpViewPager() {
        try {
            fragmentList = ArrayList<Fragment>()
            fragmentList.add(SpeakingHome())
            fragmentList.add(CallHistoryFragment())
            fragmentList.add(VocabFragment())
            binding.main.vpHome.adapter = HomePagerAdapter(this, fragmentList)
            binding.main.vpHome.setCurrentItem(0, false)
            binding.main.vpHome.isUserInputEnabled = false
            binding.main.vpHome.offscreenPageLimit = 4

            binding.main.bottomNav.speak.setOnClickListener {
                changePage(0)
            }

//            binding.main.bottomNav.history.setOnClickListener {
//                changePage(1)
//            }

            binding.main.bottomNav.dictionary.setOnClickListener {
                changePage(2)
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
        if (AppPref.getBoolean(applicationContext,AppPref.loggedIn) == true){
            showLogin(false)
        }
        else {
            AppPref.getString(applicationContext,AppPref.deviceid)?.let {
                FireStoreApi.hasUser(it,object :FireStoreCallback{
                    override fun OnSuccessListener(snapshot: DataSnapshot?) {
                        if (snapshot?.getValue(User::class.java) == null){
                            showLogin(true)
                            return
                        } else{
                            showLogin(false)
                            USER = snapshot?.getValue(User::class.java)
                            AppPref.put(applicationContext,AppPref.user,USER!!.toJsonString(USER))
                            AppPref.put(applicationContext,AppPref.loggedIn,true)
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
        if (AppPref.getBoolean(applicationContext,AppPref.loggedIn) == true)
            showLogin(false)
    }



}

interface CallerCallback{
    fun backListener()
}
