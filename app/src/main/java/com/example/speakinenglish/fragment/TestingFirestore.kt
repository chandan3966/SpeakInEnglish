package com.example.speakinenglish.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.api.FirestoreTest
import com.example.speakinenglish.R
import kotlinx.android.synthetic.main.fragment_testing_firestore.*


class TestingFirestore : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_testing_firestore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add.setOnClickListener {
            FirestoreTest.addOrUpdateData()
        }
        read.setOnClickListener {
            FirestoreTest.readData()
        }
        update.setOnClickListener {
            FirestoreTest.addOrUpdateData(hashMapOf(
                "first" to "Firestore",
                "last" to "updated",
                "born" to 1234
            ))
        }
        delete.setOnClickListener {
            FirestoreTest.deleteData()
        }

    }
}