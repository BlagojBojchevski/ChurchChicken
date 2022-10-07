package com.tts.gueststar.ui.support

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.SkipSurveyRequest
import app.com.relevantsdk.sdk.models.SkipSurveyResponse
import app.com.relevantsdk.sdk.models.SubmitSurveyRequest
import app.com.relevantsdk.sdk.models.SubmitSurveyResponse
import com.tts.gueststar.ui.BasePresenterView
import rxjavasdk.api.RxJavaApiHandler

class SurveyPresenter(val view: SurveyView) : ApiListener.SubmitSurveyInterface, ApiListener.SkipSurveyInterface {


    fun checkRequiredFields(answersList: MutableList<SubmitSurveyRequest.Answer>, commentsIdList: List<Int>) {
        var unansweredQuestions = 0
        answersList.forEachIndexed { _, answer ->
            if (!commentsIdList.contains(answer.question_id)) {
                if (answer.answer_id.size > 0) {
                } else {
                    unansweredQuestions++
                }
            }

            if (unansweredQuestions > 0) {
                view.disableSubmitButton()
            } else {
                view.enableSubmitButton()
            }
        }
    }


    fun submitSurvey(
        context: Context,
        request: SubmitSurveyRequest,
        appkey: String,
        auth_token: String,
        surveyId: Int
    ) {
        RxJavaApiHandler().submitSurvey(context, appkey, auth_token, surveyId, request, this)
    }

    fun skipSurvey(context: Context, request: SkipSurveyRequest, appkey: String, auth_token: String) {
        RxJavaApiHandler().skipSurvey(context, appkey, auth_token, request, this)

    }

    interface SurveyView : BasePresenterView {
        fun enableSubmitButton()
        fun disableSubmitButton()
        fun unauthorized(error: String)
        fun onSuccessAnswerSurvey()
        fun onSuccessSkipSurvey()

    }

    override fun onSuccess(response: SubmitSurveyResponse) {
        view.onSuccessAnswerSurvey()
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onFailureUnauthorized(error: String) {
        view.unauthorized(error)
    }

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onSuccess(response: SkipSurveyResponse) {
        view.onSuccessSkipSurvey()
    }

    fun onDestroy(){
        RxJavaApiHandler().onDestroy()
    }


}