package com.example.api.util

object Utils {

    fun getLevel(level:String):List<String>{
        var list = ArrayList<String>()
        if (level.equals("any")){
            list = arrayListOf("beginner", "intermediate", "advanced")
        }
        else{
            if (level.equals("beginner")){
                list = arrayListOf("beginner")
            }
            else if(level.equals("advanced")){
                list = arrayListOf("intermediate", "advanced")
            }
        }
        return list
    }

    fun getGender(gender:String):List<String>{
        var list = ArrayList<String>()
        if (gender.equals("any")){
            list = arrayListOf("male", "female")
        }
        else{
            if (gender.equals("male")){
                list = arrayListOf("male")
            }
            else if(gender.equals("female")){
                list = arrayListOf("female")
            }
        }
        return list
    }
}