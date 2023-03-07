package com.example.speakinenglish.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.advertise.AdsManager
import com.example.speakinenglish.R
import com.example.speakinenglish.activity.MainActivity
import com.example.speakinenglish.databinding.FragmentSpeakingHomeBinding
import com.example.speakinenglish.fragment.LoginFragment.Companion.USER
import kotlinx.android.synthetic.main.fragment_speaking_home.*


class SpeakingHome : Fragment() {
    private var binding: FragmentSpeakingHomeBinding? = null

    var permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val requestCode = 1


    fun askPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode)
    }

    fun isPermissionsGranted(): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSpeakingHomeBinding.inflate(inflater,container,false);
        return inflater.inflate(R.layout.fragment_speaking_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name.text = USER?.name
        userLevel.text = "Level:"+ USER?.level
        AdsManager.requestNativeAd(
            view.findViewById(R.id.llAdView2),
            com.example.advertise.R.layout.main_ad_template_view,
            getString(R.string.ad_home_native)
        )

        view.findViewById<Button>(R.id.start_talk).setOnClickListener {
            if (isPermissionsGranted()){
                showChooserActivity()
            }
            else{
                askPermissions()
            }
        }
        radioManager()
    }

    fun radioManager(){
        radioButton2.setOnClickListener {
            genderGroup1.isChecked = true
        }
        radioButton3.setOnClickListener {
            genderGroup1.isChecked = true
        }
        genderGroup2.setOnClickListener {
            radioButton1.isChecked = true
        }
        genderGroup3.setOnClickListener {
            radioButton1.isChecked = true
        }
    }


    fun getGenderSelected():String{
        return when{
            (genderGroup1.isChecked()) -> "any"
            (genderGroup2.isChecked()) -> "female"
            (genderGroup3.isChecked()) -> "male"
            else -> ""
        }
    }

    fun getLevelSelected():String{
        return when{
            (radioButton1.isChecked()) -> "any"
            (radioButton2.isChecked()) -> "beginner"
            (radioButton3.isChecked()) -> "advanced"
            else -> {""}
        }
    }


    fun showChooserActivity(){
        if (radioButton1.isChecked && genderGroup1.isChecked){
            Toast.makeText(context,"Please choose any one category",Toast.LENGTH_SHORT).show()
        }
        else{
            (activity as MainActivity).hideMain()
            (activity as MainActivity).binding.container.visibility = View.VISIBLE
            val ft: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            val fragment = ChooseActivityFragment()
            var bundle = Bundle()
            bundle.putString("level",getLevelSelected())
            bundle.putString("gender",getGenderSelected())
            fragment.arguments = bundle
            ft.replace(
                R.id.chooser_container,
                fragment,
                "ChooseActivityFragment"
            )
            ft.commit()
        }

    }

}