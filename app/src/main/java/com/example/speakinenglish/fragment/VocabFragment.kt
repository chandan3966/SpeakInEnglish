package com.example.speakinenglish.fragment

import android.content.Context
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.advertise.AdsManager
import com.example.api.externalapis.VocabularyApi
import com.example.api.model.VocabInternalRes
import com.example.api.model.VocabResponse
import com.example.speakinenglish.R
import com.example.speakinenglish.activity.MainActivity
import com.example.speakinenglish.adapters.VocabularyAdapter
import com.google.firebase.firestore.local.LruGarbageCollector.Results
import kotlinx.android.synthetic.main.fragment_call_history.*
import kotlinx.android.synthetic.main.fragment_vocab.*
import kotlinx.android.synthetic.main.fragment_vocab.adView1
import retrofit2.Callback
import retrofit2.Response


class VocabFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vocab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AdsManager.loadBannerAd(adView1)

        wordArea.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // If the event is a key-down event on the "enter" button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    (activity as MainActivity).hideKeyboard()
                    progressBar.visibility = View.VISIBLE
                    vocabRecycler.visibility = View.GONE
                    notFound.visibility = View.GONE
                    try {
                        val response = VocabularyApi.ApiBuilder(requireContext()).getWordInfo(wordArea.text.toString())
                        response.enqueue(object : Callback<List<VocabInternalRes>>{
                            override fun onResponse(
                                call: retrofit2.Call<List<VocabInternalRes>>,
                                response: Response<List<VocabInternalRes>>
                            ) {
                                progressBar.visibility = View.GONE
                                notFound.visibility = View.GONE
                                if (response.isSuccessful){
                                    vocabRecycler.visibility = View.VISIBLE
                                    var adapter = VocabularyAdapter(requireContext(), response.body() as ArrayList<Any>)
                                    vocabRecycler.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
                                    vocabRecycler.adapter = adapter
                                    adapter.notifyDataSetChanged()
                                }
                                else {
                                    notFound.visibility = View.VISIBLE
                                    vocabRecycler.visibility = View.GONE
                                }

                            }

                            override fun onFailure(
                                call: retrofit2.Call<List<VocabInternalRes>>,
                                t: Throwable
                            ) {
                                progressBar.visibility = View.GONE
                                notFound.visibility = View.VISIBLE
                                vocabRecycler.visibility = View.GONE
                                Log.d("VocabAPI", "onFailure: "+t.localizedMessage)
                            }

                        })
                    }catch (Ex:Exception){
                    }
                    return true
                }
                return false
            }
        })

    }
}