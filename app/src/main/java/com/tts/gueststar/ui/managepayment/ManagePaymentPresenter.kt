package com.tts.gueststar.ui.managepayment

import android.content.Context
import com.google.gson.Gson
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.CloudConnectInterface
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.UserPaymentInfoInterface
import com.tts.nsrsdkrelevant.cloudconnect.models.CloudConnectResponse
import com.tts.nsrsdkrelevant.cloudconnect.models.GetNCRUserPaymentInfoResponse
import com.tts.nsrsdkrelevant.cloudconnect.models.Payment_
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import ncruser.interfaces.AddCreditCardInterface
import ncruser.interfaces.DeleteCreditCardInterface
import ncruser.models.NCRInsertCreditCardRequest
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper

class ManagePaymentPresenter(var view: ManagePaymentView) :
    UserPaymentInfoInterface, CloudConnectInterface, AddCreditCardInterface,
    DeleteCreditCardInterface {
    override fun onSuccessDeleteCreditCard() {
      view.closeManagePayment()
    }

    override fun onFailureDeleteCreditCard(error: String) {
        view.showError(error)
    }

    override fun onFailureDeleteCreditCardUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onFailureDeleteCreditCard() {
        view.showGenericError()
    }

    override fun onFailureAddCreditCardUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onShowSuccessAddCreditCard() {
        view.callGetUserPaymentInfo()
    }

    override fun onFailureAddCreditCard(error: String) {
        view.showError(error)
    }

    override fun onFailureAddCreditCard() {
        view.showGenericError()
    }


    override fun onFailureCloudConnect() {
        view.showGenericError()
    }

    override fun onFailureCloudConnect(error: String) {
        view.showError(error)
    }

    override fun onSuccessCloudConnect(response: CloudConnectResponse) {
        if (response.status) {
            view.saveCloudConnectSettings(response)
        } else if (!response.status) {
            if (response.notice != null)
                view.showError(response.notice!!)
            else
                view.showGenericError()
        } else {
            view.showGenericError()
        }
    }

    override fun onFailurePaymentInfo() {
        view.showGenericError()
    }

    override fun onFailurePaymentInfo(error: String) {
        view.onShowAddPayment()
    }

    override fun onFailurePaymentInfoUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onSuccessPaymentInfo(response: GetNCRUserPaymentInfoResponse) {
        if (response.status) {
            if (response.response.Payments.isNotEmpty()) {
                view.showCardSummary(response.response.Payments)
            } else {
                view.onShowAddPayment()
            }
        } else {
            view.onShowAddPayment()
        }
    }

    fun addCreditCard(context: Context, request: NCRInsertCreditCardRequest, customerId: String) {
        RxJavaCloudConnectApiHelper().addCreditCard(
            context, Gson().toJson(request), customerId
            , this
        )
    }

    fun deleteCreditCard(context: Context, accountId: String, customerId: String) {
        RxJavaCloudConnectApiHelper().deleteCreditCard(
            context, accountId, customerId
            , this
        )
    }

    fun getCloudConnectSettings(context: Context) {
        RxJavaCloudConnectApiHelper().getCloudConnectSettings(
            context, BuildConfig.APPKEY
            , this
        )
    }


    fun getUserPaymentInfo(context: Context, auth_token: String) {
        RxJavaCloudConnectApiHelper().getUserPaymentInfo(
            context, BuildConfig.APPKEY, auth_token, "en"
            , this
        )
    }

    interface ManagePaymentView : BasePresenterView {
        fun saveCloudConnectSettings(response: CloudConnectResponse)
        fun showCardSummary(payments: List<Payment_>)
        fun disableButton()
        fun onFailureUnauthorized(error: String)
        fun enableButton()
        fun onShowAddPayment()
        fun onShowSuccessAdded()
        fun closeManagePayment()
        fun callGetUserPaymentInfo()

    }

    fun onDestroy() {
        RxJavaCloudConnectApiHelper().onDestroy()
    }
}