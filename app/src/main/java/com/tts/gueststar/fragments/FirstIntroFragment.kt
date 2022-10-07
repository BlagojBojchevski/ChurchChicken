package com.tts.gueststar.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.tts.gueststar.R
import com.tts.gueststar.TutorialActivity

class FirstIntroFragment : Fragment() {
    private var page: Int = 0
    private lateinit var activity: TutorialActivity
    private lateinit var txtHowToEarn: TextView
    private lateinit var txtHowToEarnDesc: TextView
    private lateinit var txtHowToRedeem: TextView
    private lateinit var txtHowToRedeemDesc: TextView
    private lateinit var txtHowToPay: TextView
    private lateinit var txtHowToPayDesc: TextView
    private lateinit var txtGotIt: TextView
    private lateinit var btnGotIt: RelativeLayout


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as TutorialActivity
    }

    fun newInstance(page: Int): FirstIntroFragment {
        val fragment = FirstIntroFragment()
        fragment.page = page
        return fragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View? = null
        when (page) {
            1 -> {
                view = inflater.inflate(R.layout.tutorial_first_screen, container, false)
                txtHowToEarn = view?.findViewById(R.id.txt_how_to_earn)!!
                txtHowToEarnDesc = view.findViewById(R.id.txt_how_to_earn_desc)!!
            }
            2 -> {
                view = inflater.inflate(R.layout.tutorial_second_screen, container, false)
                txtHowToRedeem = view?.findViewById(R.id.txt_how_to_redeem)!!
                txtHowToRedeemDesc = view.findViewById(R.id.txt_how_to_redeem_desc)!!
            }
            3 -> {
                view = inflater.inflate(R.layout.tutorial_four_screen, container, false)
                txtHowToPay = view?.findViewById(R.id.txt_how_to_pay)!!
                txtHowToPayDesc = view.findViewById(R.id.txt_how_to_pay_desc)!!
                txtGotIt = view.findViewById(R.id.btn_got_it)!!
                btnGotIt = view.findViewById(R.id.button_got_it)!!
                btnGotIt.setOnClickListener { activity.finishTutorial() }
            }
        }
        return view
    }
}