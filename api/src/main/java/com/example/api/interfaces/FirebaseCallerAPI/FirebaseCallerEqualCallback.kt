package com.example.api.interfaces.FirebaseCallerAPI

import com.example.api.model.QuestionSession

interface FirebaseCallerEqualCallback{
    fun OnEqualListener(value: Long,otherValue:Long,question: QuestionSession)
    fun NotEqualListener(value: Long,otherValue:Long,question: QuestionSession)
}