package com.tts.gueststar.fragments.contact

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.MainActivity
import com.tts.gueststar.R
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.utility.AppConstants
import kotlinx.android.synthetic.main.fragment_survey_thank_you.*

class SurveyThankYouFragment : BaseFragment(), View.OnClickListener {


    private lateinit var homeActivity: MainActivity
    private var text: String? = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            text = arguments!!.getString(AppConstants.EXTRA_THANK_YOU_TEXT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_survey_thank_you, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvSurveyConfirmationText.text = text
        btnSurveyCancel.setOnClickListener(this)
    }

    private fun returnToContactUs() {
        homeActivity.supportFragmentManager.popBackStack(
            AppConstants.TAG_SURVEY_THANK_YOU,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        homeActivity.supportFragmentManager.popBackStack(
            AppConstants.TAG_SURVEY,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        homeActivity.supportFragmentManager.popBackStack(
            AppConstants.TAG_SUPPORT_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnSurveyCancel -> {
                returnToContactUs()
            }
        }
    }

}
