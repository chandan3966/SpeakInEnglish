package com.example.speakinenglish.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.RatingBar
import com.example.speakinenglish.R
import com.example.speakinenglish.interfaces.EndCallDialogInterface

class CustomDialogClass(context: Context,listener: EndCallDialogInterface) : Dialog(context), View.OnClickListener {

    var d: Dialog? = null
    var report: Button? = null
    var submit: Button? = null
    var rating: RatingBar? = null
    var newlistener: EndCallDialogInterface = listener
    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dialog)
        report = findViewById<View>(R.id.btn_report) as Button
        submit = findViewById<View>(R.id.btn_end) as Button
        rating = findViewById<View>(R.id.ratingBar) as RatingBar
        rating!!.numStars = 5
        report!!.setOnClickListener(this)
        submit!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.getId()) {
            R.id.btn_report -> {
                newlistener.onReported()
            }
            R.id.btn_end -> {
                newlistener.onEnded( rating!!.rating)
            }
            else -> {}
        }
        dismiss()
    }
}
