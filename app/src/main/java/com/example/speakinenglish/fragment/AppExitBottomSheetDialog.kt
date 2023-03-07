package com.example.speakinenglish.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.example.advertise.AdsManager.requestNativeAd
import com.example.speakinenglish.R
import com.example.speakinenglish.activity.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AppExitBottomSheetDialog : BottomSheetDialogFragment() {
    var exitAppListener: MainActivity.ExitAppListener? = null

    companion object {
        fun newInstance(exitAppListener: MainActivity.ExitAppListener): AppExitBottomSheetDialog {
            val appExitBottomSheetDialog = AppExitBottomSheetDialog()
            appExitBottomSheetDialog.exitAppListener = exitAppListener
            return appExitBottomSheetDialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.bottom_sheet_app_exit, container,
            false
        )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val llAdView1: LinearLayout = view.findViewById(R.id.llAdView1)
        val tvCancel: AppCompatButton = view.findViewById(R.id.tvCancel)
        val tvExitApp: AppCompatButton = view.findViewById(R.id.tvExitApp)
        requestNativeAd(
            llAdView1,
            com.example.advertise.R.layout.main_ad_template_view,
            getString(R.string.ad_exit_native)
        )
        tvExitApp.setOnClickListener {
            exitAppListener?.onAppExit()
        }
        tvCancel.setOnClickListener {
            dismiss()
        }
    }
}