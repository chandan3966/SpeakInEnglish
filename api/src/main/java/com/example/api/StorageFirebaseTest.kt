package com.example.api

import android.net.Uri
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File


object StorageFirebaseTest {
    var storage = FirebaseStorage.getInstance()
    var storageRef = storage.reference

    fun upload(file: Uri,fileName:String,listener: UploadCallback){
        val storageImagesRef = storageRef.child("images/"+fileName)

        storageImagesRef.putFile(file).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(p0: java.lang.Exception) {

            }
        }).addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot?> {
            try {
                it.storage.downloadUrl.addOnSuccessListener(OnSuccessListener<Uri> {
                    try {
                        listener.uploadComplete(it)
                    }
                    catch (e:Exception){

                    }
                })
            }
            catch (e:Exception){

            }
        })

        storageImagesRef.putFile(file)
            .addOnSuccessListener(object :OnSuccessListener<UploadTask.TaskSnapshot>{
                override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                    try {
                        listener.uploadComplete(p0?.storage?.downloadUrl?.result)
                    }
                    catch (e:Exception){

                    }
                }
            })
            .addOnFailureListener {
                listener.uploadFailure(it)
            }

    }

    fun download(fileName:String,listener: DownloadCallback){
        val storageImagesRef = storageRef.child("images/"+fileName+".jpg")

        try {
            storageImagesRef.downloadUrl.addOnSuccessListener(
                object : OnSuccessListener<Uri> {
                    override fun onSuccess(p0: Uri?) {
                        listener.downloadComplete(p0)
                    }
                })
                .addOnFailureListener {
                    listener.downloadFailure(it)
                }
        } catch (e:Exception) {

        }

    }


}

interface UploadCallback{
    fun uploadComplete(downloadUrl:Uri?)
    fun uploadFailure(it:Exception)
}

interface DownloadCallback{
    fun downloadComplete(downloadUrl:Uri?)
    fun downloadFailure(it:Exception)
}