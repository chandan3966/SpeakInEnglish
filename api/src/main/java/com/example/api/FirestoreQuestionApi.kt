package com.example.api

import com.example.api.model.Grammar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

object FirestoreQuestionApi {

    val reference = FirebaseDatabase.getInstance().getReference("generaldata")

    fun getGrammarQuestion(listener: QuestionCallback){
        reference.child("grammar")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var output = ArrayList<Grammar>()
                    var finallist = ArrayList<String>()
                    var finalkey = ArrayList<String>()
                    if (snapshot.child("grammar_key").childrenCount > 0){
                        for (i in snapshot.child("grammar_key").children)
                            finalkey.add(i.value.toString())
                    }
                    if (snapshot.child("grammar_question").childrenCount > 0){
                        for (i in snapshot.child("grammar_question").children)
                            finallist.add(i.value.toString())
                    }
                    for (i in 0..finallist.size-1){
                        output.add(Grammar(finalkey.get(i),finallist.get(i)))
                    }
                    listener.OnSuccessListener(output)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.OnCancelled(error)
                }

            })
    }

    fun getQuestion(listener: QuestionCallback){
        reference.child("questions")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var finallist = ArrayList<String>()
                    if (snapshot.childrenCount>0){
                        for (i in snapshot.children){
                            finallist.add(i.value.toString())
                        }
                    }

                    listener.OnSuccessListener(finallist)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.OnCancelled(error)
                }

            })
    }

    fun getWords(listener: QuestionCallback){
        reference.child("words")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var finallist = ArrayList<String>()
                    if (snapshot.childrenCount>0){
                        for (i in snapshot.children){
                            finallist.add(i.value.toString())
                        }
                    }
                    listener.OnSuccessListener(finallist)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.OnCancelled(error)
                }

            })
    }

    fun singleQuestion(quesNo: Int,listener: QuestionCallback){
        reference.child("words")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener.OnSuccessListener(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.OnCancelled(error)
                }

            })
    }

    interface QuestionCallback{
        fun OnSuccessListener(objects : Any)
        fun OnCancelled(error: DatabaseError)
    }
}