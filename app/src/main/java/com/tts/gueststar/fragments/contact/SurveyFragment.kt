package com.tts.gueststar.fragments.contact

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import app.com.relevantsdk.sdk.models.GetSurveyResponse
import app.com.relevantsdk.sdk.models.SkipSurveyRequest
import app.com.relevantsdk.sdk.models.SubmitSurveyRequest
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.SurveyMultipleChoiceAdapter
import com.tts.gueststar.fragments.userauth.MainSignUpFragment
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.support.SurveyPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_survey.*
import kotlinx.android.synthetic.main.inflater_survey_slider.view.*
import kotlinx.android.synthetic.main.survey_edit_text.view.*
import kotlinx.android.synthetic.main.survey_radio_button.view.*
import kotlinx.android.synthetic.main.survey_spinner_multiple_choice.view.*
import kotlinx.android.synthetic.main.survey_spinner_single_choice.view.*
import kotlinx.android.synthetic.main.survey_text_view.view.*
import kotlinx.android.synthetic.main.suvery_multiple_choice.view.*
import javax.inject.Inject
import kotlin.math.ceil


class SurveyFragment : BaseFragment(), SurveyPresenter.SurveyView, View.OnClickListener {

    private var survey: GetSurveyResponse? = null
    private var locationId: Int = 0
    private var mInflater: LayoutInflater? = null
    private lateinit var homeActivity: MainActivity
    private lateinit var presenter: SurveyPresenter
    private var answersList: MutableList<SubmitSurveyRequest.Answer> =
        emptyList<SubmitSurveyRequest.Answer>().toMutableList()
    private var commentQuestionsIds: MutableList<Int> = emptyList<Int>().toMutableList()
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    private var offerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            survey = arguments!!.getParcelable(AppConstants.EXTRA_SURVEY)
            locationId = arguments!!.getInt(AppConstants.EXTRA_LOCATION_ID_SURVEY)
            offerId = arguments!!.getInt(AppConstants.EXTRA_OFFER_ID)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mInflater = inflater
        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return inflater.inflate(R.layout.fragment_survey, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = SurveyPresenter(this)
        survey!!.survey.questions.forEachIndexed { _, question ->
            answersList.add(
                SubmitSurveyRequest.Answer(
                    question_id = question.id,
                    answer_text = "",
                    answer_id = emptyList<Int>().toMutableList()
                )
            )
            if (question.question_type == 1) {
                commentQuestionsIds.add(question.id)
            }
        }
        loadSurveyData(survey!!.survey, commentQuestionsIds)

        disableSubmitButton()

        setListeners()
        etSkipSurvey.underline()
    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setListeners() {
        btn_back.setOnClickListener(this)
        etSkipSurvey.setOnClickListener(this)
        btnSubmit.setOnClickListener(this)
    }

    @SuppressLint("InflateParams")
    fun loadSurveyData(surveyArray: GetSurveyResponse.Survey, commentIds: List<Int>) {

        layoutSurvey.removeAllViews()
        surveyArray.questions.forEachIndexed { k, question ->
            val layoutName = mInflater!!.inflate(R.layout.survey_text_view, null)
            val tvName = layoutName.surveyTextView
            tvName.text = question.text
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val paddingPixel = resources.getDimension(R.dimen.dimen_2)
            val density = resources.displayMetrics.density
            val paddingDp = (paddingPixel * density).toInt()
            layoutParams.setMargins(0, paddingDp, 0, 0)
            layoutSurvey.addView(layoutName, layoutParams)

            if (question.question_type == 1) {
                val layoutComments = mInflater!!.inflate(R.layout.survey_edit_text, null)
                val etComments = layoutComments.surveyEditText

                tvName.visibility = View.GONE

                etComments.hint = question.text

                etComments.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        answersList.forEachIndexed { _, answer ->
                            if (answer.question_id == question.id) {
                                answer.answer_id.clear()
                                answer.answer_text = s.toString()
                            }
                        }

                        presenter.checkRequiredFields(answersList, commentIds)
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                    }

                })



                layoutSurvey.addView(layoutComments)

            }

            if (question.question_type == 2) {
                val layoutMultipleChoices =
                    mInflater!!.inflate(R.layout.suvery_multiple_choice, null)
                val rvChoices = layoutMultipleChoices.rvSurveyMultipleChoice

                val adapter = SurveyMultipleChoiceAdapter(
                    homeActivity,
                    question.question_choices,
                    question.id,
                    answersList,
                    presenter,
                    commentIds
                )


                rvChoices.layoutManager =
                    LinearLayoutManager(homeActivity.applicationContext)
                rvChoices.adapter = adapter


                layoutSurvey.addView(layoutMultipleChoices)

            }

            if (question.question_type == 3) {
                val layoutSingleSpinner =
                    mInflater!!.inflate(R.layout.survey_spinner_single_choice, null)
                val spinner = layoutSingleSpinner.spinnerSingleChoice
                val choicesList: ArrayList<String> = ArrayList()

                choicesList.add("Select")
                question.question_choices.forEachIndexed { _, questionChoice ->
                    choicesList.add(questionChoice.label)
                }


                val spinnerAdapter = object : ArrayAdapter<String>(
                    homeActivity,
                    R.layout.support_simple_spinner_dropdown_item,
                    choicesList
                ) {
                    override fun isEnabled(position: Int): Boolean {
                        return position != 0
                    }

                    override fun getDropDownView(
                        position: Int, convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        val tv = view as TextView
                        if (position == 0) {
                            // Set the hint text color gray
                            tv.setTextColor(Color.GRAY)
                        } else {
                            tv.setTextColor(Color.BLACK)
                        }
                        return view
                    }
                }

                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = spinnerAdapter

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        answersList.forEachIndexed { _, answer ->
                            if (position != 0) {
                                if (answer.question_id == question.id) {
                                    answer.answer_id.clear()
                                    answer.answer_id.add(question.question_choices[position - 1].id)
                                }
                            }
                        }

                        presenter.checkRequiredFields(answersList, commentIds)

                    }

                }

                layoutSurvey.addView(layoutSingleSpinner)
            }


            if (question.question_type == 4) {
                val ratingLayoutContainer =
                    mInflater!!.inflate(R.layout.inflater_survey_slider, null)
                val ratingBarContainer = ratingLayoutContainer.starsContainer

                val ratingLayout = mInflater!!.inflate(R.layout.survey_radio_button, null)
                val ratingBar = ratingLayout.ratingBar

                ratingBar.numStars = question.question_choices.size
                ratingBar.stepSize = 0.5F
                ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                    ratingBar.rating = ceil(rating.toDouble()).toFloat()
                    answersList.forEachIndexed { _, answer ->
                        if (answer.question_id == question.id) {
                            answer.answer_id.clear()
                            answer.answer_id.add(question.question_choices[rating.toInt() - 1].id)
                        }
                    }

                    presenter.checkRequiredFields(answersList, commentIds)
                }

                ratingBarContainer.addView(ratingLayout)
                layoutSurvey.addView(ratingLayoutContainer)
            }

            if (question.question_type == 5) {

                val layoutMultipleChoiceSpinner =
                    mInflater!!.inflate(R.layout.survey_spinner_multiple_choice, null)
                val spinner = layoutMultipleChoiceSpinner.spinnerMultipleChoice

                val answerList = Array(question.question_choices.size) { "it = $it" }
                val valuesList = Array(question.question_choices.size) { "it = $it" }
                val tvHint = layoutMultipleChoiceSpinner.hintText

                question.question_choices.forEachIndexed { i, questionChoice ->
                    answerList[i] = questionChoice.label
                    valuesList[i] = questionChoice.id.toString()
                }

                spinner.setItems(
                    answerList,
                    valuesList,
                    surveyArray.questions,
                    "5",
                    k,
                    tvHint,
                    question.id,
                    answersList,
                    presenter,
                    commentIds
                )

                layoutSurvey.addView(layoutMultipleChoiceSpinner)
            }

        }


    }

    fun setSelectedChoices(
        questionId: Int,
        chosen: List<Int>,
        answersList: MutableList<SubmitSurveyRequest.Answer>,
        presenter: SurveyPresenter,
        commentIds: List<Int>
    ) {

        answersList.forEachIndexed { _, answer ->
            if (answer.question_id == questionId) {
                answer.answer_id.clear()
                answer.answer_id = chosen.toMutableList()
            }
        }

        presenter.checkRequiredFields(answersList, commentIds)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnSubmit -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    val request = SubmitSurveyRequest(answersList, 0, locationId, offerId, 0, 0)
                    presenter.submitSurvey(
                        homeActivity,
                        request,
                        BuildConfig.APPKEY,
                        Engine().getAuthToken(mySharedPreferences).toString(),
                        survey!!.survey.id
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }

            }

            R.id.btn_back -> {
                homeActivity.onBackPressed()
            }

            R.id.etSkipSurvey -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    val request = SkipSurveyRequest(0, survey!!.survey.id)
                    presenter.skipSurvey(
                        homeActivity,
                        request,
                        BuildConfig.APPKEY,
                        Engine().getAuthToken(mySharedPreferences).toString()
                    )
                } else {
                    Engine().showMsgDialog(
                        "",
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

        }
    }


    override fun showGenericError() {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog("", getString(R.string.handle_blank_pop_up), homeActivity)
    }

    override fun showError(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog("", error, homeActivity)
    }

    override fun unauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
        Engine.setNextPage = AppConstants.TAG_SURVEY
        homeActivity.presenter.openFragmentUp(
            getFragment(
                MainSignUpFragment()
            ), AppConstants.TAG_MAIN_SIGN_UP
        )
    }

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
    }

    override fun enableSubmitButton() {
        Engine().setEnableButton(btnSubmit, true)
    }

    override fun disableSubmitButton() {
        Engine().setEnableButton(btnSubmit, false)
    }

    override fun onSuccessAnswerSurvey() {
        homeActivity.presenter.dismissProgress()
        Engine().hideSoftKeyboard(homeActivity)
        homeActivity.presenter.openFragmentRight(
            getFragment(
                SurveyThankYouFragment(),
                getString(R.string.for_your_feedback)
            ), AppConstants.TAG_SURVEY_THANK_YOU
        )
    }

    override fun onSuccessSkipSurvey() {
        homeActivity.presenter.dismissProgress()
        Engine().hideSoftKeyboard(homeActivity)
        homeActivity.presenter.openFragmentRight(
            getFragment(
                SurveyThankYouFragment(),
                getString(R.string.next_time)
            ), AppConstants.TAG_SURVEY_THANK_YOU
        )
    }

    private fun getFragment(fragment: BaseFragment, thankYouText: String): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTRA_THANK_YOU_TEXT, thankYouText)
        fragment.arguments = bundle
        return fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }

}
