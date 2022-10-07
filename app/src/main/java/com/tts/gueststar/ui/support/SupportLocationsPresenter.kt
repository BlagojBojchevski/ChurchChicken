package com.tts.gueststar.ui.support

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.GetSurveyIdResponse
import app.com.relevantsdk.sdk.models.GetSurveyResponse
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.tts.gueststar.ui.BasePresenterView
import rxjavasdk.api.RxJavaApiHandler

class SupportLocationsPresenter(
    val context: Context,
    val view: SupportLocationsView,
    private val isAppSupport: Boolean
) : ApiListener.LocationsInterface,
    ApiListener.GetSurveryInterface,
    ApiListener.GetSurveyIdInterface {


    fun getLocations(context: Context, appkey: String, lat: Double, lng: Double, search: String) {
        RxJavaApiHandler().getLocations(context, appkey, lat, lng, search, this)
    }

    fun getLocationsNoSearch(
        context: Context,
        appkey: String,
        lat: Double,
        lng: Double
    ) {
        RxJavaApiHandler().getLocationsNoSearch(context, appkey, lat, lng, this)
    }

    fun getSurvey(context: Context, appkey: String, auth_token: String, surveyId: Int) {
        RxJavaApiHandler().getSurvey(context, appkey, auth_token, surveyId, this)
    }

    fun getSurveyId(context: Context, appkey: String, auth_token: String) {
        RxJavaApiHandler().getSurveyId(context, appkey, auth_token, this)
    }

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onSuccess(response: LocationsResponse?) {
        if (isAppSupport) {
            view.loadAppSupportLocations(response)
        } else {
            view.loadSendFeedbackLocations(response)
        }
    }

    override fun onSuccess(response: GetSurveyIdResponse) {
        view.getSurvey(response.survey_id)
    }

    override fun onSuccess(response: GetSurveyResponse) {
        view.onSuccessGetSurvey(response)
    }

    override fun onFailureUnauthorized(error: String) {
        view.unauthorized(error)
    }

    interface SupportLocationsView : BasePresenterView {
        fun loadAppSupportLocations(response: LocationsResponse?)
        fun loadSendFeedbackLocations(response: LocationsResponse?)
        fun unauthorized(error: String)
        fun onSuccessGetSurvey(response: GetSurveyResponse)
        fun getSurvey(surveyId: Int)
    }

    fun onDestroy() {
        RxJavaApiHandler().onDestroy()
    }


}