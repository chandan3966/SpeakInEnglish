package com.example.speakinenglish.util

object RandomGenerate {

    fun getArray(max: Int, items:Int) : ArrayList<Int>{
        var arrayList = ArrayList<Int>()
        for (i in 0..items){
            arrayList.add((0 until max).random())
        }
        return arrayList
    }
}