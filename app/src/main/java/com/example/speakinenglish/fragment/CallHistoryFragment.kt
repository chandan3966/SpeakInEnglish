package com.example.speakinenglish.fragment

import android.app.Activity.RESULT_OK
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.advertise.AdsManager
import com.example.api.DownloadCallback
import com.example.api.StorageFirebaseTest
import com.example.api.UploadCallback
import com.example.speakinenglish.R
import kotlinx.android.synthetic.main.fragment_call_history.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CallHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallHistoryFragment : Fragment() {

    val PICK_IMAGE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_call_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AdsManager.loadBannerAd(adView1)

        choose.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, PICK_IMAGE)
        }

        upload.setOnClickListener {
            uploadFile(file!!, "Test.jpg")
        }

        downloadBtn.setOnClickListener {
            downloadFile( "Test")
        }
    }

    var download: Uri? = null
    fun uploadFile(file:Uri,filename:String){
        StorageFirebaseTest.upload(file,filename,object :UploadCallback{
            override fun uploadComplete(downloadUrl: Uri?) {
                download = downloadUrl
                Toast.makeText(requireContext(),"Uploaded",Toast.LENGTH_SHORT).show()
            }

            override fun uploadFailure(it: Exception) {

            }

        })
    }

    fun downloadFile(filename:String){
        StorageFirebaseTest.download(filename,object :DownloadCallback{
            override fun downloadComplete(downloadUrl: Uri?) {
                if (downloadUrl != null) {
                    downloadLocalFile(downloadUrl)
                }
            }

            override fun downloadFailure(it: Exception) {
            }

        })
    }

    var file: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            file = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), file)
            Glide.with(requireContext()).load(bitmap).into(imageView)
        }
    }


    fun downloadLocalFile(url:Uri){
        var downloadManager =  requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(url)
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )
        request.setTitle("Data Download")
        request.setDescription("Android Data download using DownloadManager.")

        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"downloadfileName")
        request.setMimeType("*/*")
        downloadManager.enqueue(request)
    }

}