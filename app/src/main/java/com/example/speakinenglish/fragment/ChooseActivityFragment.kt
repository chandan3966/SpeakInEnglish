package com.example.speakinenglish.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.example.speakinenglish.R
import com.example.speakinenglish.activity.CallerActivity
import com.example.speakinenglish.databinding.FragmentChooseActivityBinding
import com.example.speakinenglish.databinding.FragmentSpeakingHomeBinding
import kotlinx.android.synthetic.main.fragment_choose_activity.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChooseActivityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChooseActivityFragment : Fragment() {

    var gender = ""
    var level = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments
        if (args != null) {
            gender = args.getString("gender").toString()
            level = args.getString("level").toString()
        }
        clickListeners()
    }

    fun clickListeners(){
        topic.setOnClickListener {
            showCallLoadingPage("questions")
//            var intent = Intent(context,CallerActivity::class.java)
//            intent.putExtra("type","questions")
//            context?.startActivity(intent)
        }

        guess.setOnClickListener {
            showCallLoadingPage("words")
//            var intent = Intent(context,CallerActivity::class.java)
//            intent.putExtra("type","words")
//            context?.startActivity(intent)
        }

        grammar.setOnClickListener {
            showCallLoadingPage("grammar")
//            var intent = Intent(context,CallerActivity::class.java)
//            intent.putExtra("type","grammar")
//            context?.startActivity(intent)
        }
    }

    fun showCallLoadingPage(type:String){
        val ft: FragmentTransaction =
        requireActivity().supportFragmentManager.beginTransaction()
        var bundle = Bundle()
        bundle.putString("type",type)
        bundle.putString("gender",gender)
        bundle.putString("level",level)
        var fragment = FindingSomeone()
        fragment.arguments = bundle
        ft.replace(
            R.id.chooser_container,
            fragment,
            "FindingSomeone"
        )
        ft.commit()
    }
}