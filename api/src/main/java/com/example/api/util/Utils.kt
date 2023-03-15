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

    fun generateLevel(level:String):Double{
        return if (level.equals("beginner")){
            0.0
        } else if (level.equals("advanced") || level.equals("intermediate")){
            1.0
        }
        else {
            2.0
        }
    }

    fun generateGender(gender:String):Double{
        return if (gender.equals("male")){
            0.0
        } else if (gender.equals("female")){
            1.0
        }
        else {
            2.0
        }
    }
}