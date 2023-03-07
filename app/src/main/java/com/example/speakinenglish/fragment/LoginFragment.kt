package com.example.speakinenglish.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.api.FireStoreApi
import com.example.api.FireStoreCallback
import com.example.api.model.User
import com.example.speakinenglish.R
import com.example.speakinenglish.activity.MainActivity
import com.example.speakinenglish.adapters.AvatarSelectedListener
import com.example.speakinenglish.adapters.AvatarsAdapter
import com.example.speakinenglish.container.AppPrefs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.fragment_login.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    lateinit var adapter: AvatarsAdapter
    lateinit var list: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        next_btn.setOnClickListener {
            if (!name_edttxt.text.equals("")){
                name = name_edttxt.text.toString()
                (activity as MainActivity).hideKeyboard()
                showLevelGenderView()
            }
            else{
                Toast.makeText(context,"Please enter a name",Toast.LENGTH_SHORT).show()
            }
        }

        next_btn2.setOnClickListener {
            if (!getGenderSelected().equals("") && !getLevelSelected().equals("")){
                gender = getGenderSelected()
                level = getLevelSelected()
                showAvatarView()
            }
            else{
                Toast.makeText(context,"Please select level and gender",Toast.LENGTH_SHORT).show()
            }
        }

        finsih.setOnClickListener {
            if (image.equals("")){
                Toast.makeText(context,"Please avatar",Toast.LENGTH_SHORT).show()
            }
            else{
                USER = FireStoreApi.addUser(
                    id = AppPrefs.deviceid.get(),
                    gender = gender,
                    level = level,
                    name = name,
                    avatar = image,
                    listener = object : FireStoreCallback {
                        override fun OnSuccessListener(snapshot: DataSnapshot?) {
                            AppPrefs.loggedIn.set(true)
                            AppPrefs.commit(context)
                        }

                        override fun OnFailureListener(e: Exception) {
                            AppPrefs.loggedIn.set(false)
                            AppPrefs.commit(context)
                        }

                    }
                )
                AppPrefs.user.set(USER!!.toJsonString(USER))
                AppPrefs.commit(context)
                (activity as MainActivity).showLogin(false)
            }
        }
    }

    fun showLevelGenderView(){
        name_block.visibility = View.GONE
        option_block.visibility = View.VISIBLE
        avatar_block.visibility = View.GONE
    }

    fun showAvatarView(){
        name_block.visibility = View.GONE
        option_block.visibility = View.GONE
        avatar_block.visibility = View.VISIBLE
        loadAvatars(gender,MainActivity.MAX_AVATARS)
    }

    fun getGenderSelected():String{
        return when{
            (genderGroup1.isChecked()) -> "male"
            (genderGroup2.isChecked()) -> "female"
            else -> ""
        }
    }

    fun getLevelSelected():String{
        return when{
            (levelGroup1.isChecked()) -> "beginner"
            (levelGroup2.isChecked()) -> "intermediate"
            (levelGroup3.isChecked()) -> "advanced"
            else -> {""}
        }
    }

    fun loadAvatars(gender:String,items:Int){
        list = ArrayList()
        if (gender.equals("male")){
            for (i in 1..items){
                list.add("https://raw.githubusercontent.com/Ashwinvalento/cartoon-avatar/master/lib/images/male/${i}.png")
            }
        }
        else if (gender.equals("female")){
            for (i in 1..items){
                list.add("https://raw.githubusercontent.com/Ashwinvalento/cartoon-avatar/master/lib/images/female/${i}.png")
            }
        }
        adapter = AvatarsAdapter(list,object :AvatarSelectedListener{
            override fun onAvatarSelected(avatar: String) {
                image = avatar
            }
        })
        avatar_recycler.layoutManager = GridLayoutManager(context,3)
        avatar_recycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    companion object{
        var name = ""
        var gender = ""
        var level = ""
        var image = ""
        var USER: User? = null
    }
}