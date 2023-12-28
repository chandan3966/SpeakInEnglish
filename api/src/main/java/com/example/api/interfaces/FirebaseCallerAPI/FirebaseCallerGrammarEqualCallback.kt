package com.example.api.interfaces.FirebaseCallerAPI

import com.example.api.model.QuestionSession

interface FirebaseCallerGrammarEqualCallback{
    fun OnEqualListener(value: Long,otherValue:Long,valueAns: Boolean,otherValueAns:Boolean,question: QuestionSession)
    fun NotEqualListener(value: Long,otherValue:Long,valueAns: Boolean,otherValueAns:Boolean,question: QuestionSession)
}