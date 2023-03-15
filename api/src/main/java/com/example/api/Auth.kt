package com.example.api

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


object Auth {
    private final val TAG = "Auth"
    lateinit var auth: FirebaseAuth
    var newuser: FirebaseUser? = null

    fun signinAnonymously(activity : Activity){
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null)
            return
        auth.signInAnonymously()
            .addOnCompleteListener(activity, object : OnCompleteListener<AuthResult?> {
                override fun onComplete(task: Task<AuthResult?>) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success")
                        val user: FirebaseUser? = auth.getCurrentUser()
                        newuser = user
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException())
                    }
                }
            })
    }
}