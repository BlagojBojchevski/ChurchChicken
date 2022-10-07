package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.DeleteBillingAccounttInterface
import com.tts.olosdk.interfaces.GetBillingAccountsInterface
import com.tts.olosdk.models.OLOUserBillingAccountsResponse
import com.tts.gueststar.ui.BasePresenterView

class GetBillingAccountsPresenter(var view: BillingAccountsView) :
    GetBillingAccountsInterface, DeleteBillingAccounttInterface {
    override fun onSuccessDeleteBillingAccount() {
        view.onSuccessDelteCreditcard()
    }

    override fun onFailureDeleteBillingAccount(error: String) {
        view.showError(error)
    }

    override fun onFailureDeleteBillingAccount() {
        view.showGenericError()
    }

    override fun onSuccessGetBillingAccounts(response: OLOUserBillingAccountsResponse) {
        view.onSuccessGetUserBillingAccounts(response)
    }

    override fun onFailureGetBillingAccounts(error: String) {
        view.showError(error)
    }

    override fun onFailureGetBillingAccounts() {
        view.showGenericError()
    }

    fun getUserBillingAccounts(context: Context, oloAuthToken: String, basketID: String) {
        RxJavaOloApiHelper().getUserBillingAccounts(context, oloAuthToken, basketID, this)
    }

    fun deleteUserBillingAccount(context: Context, oloAuthToken: String, accoundId:Long) {
        RxJavaOloApiHelper().deleteUserBillingAccount(context, oloAuthToken, accoundId, this)
    }

    interface BillingAccountsView : BasePresenterView {
        fun onSuccessDelteCreditcard()
        fun onSuccessGetUserBillingAccounts(response: OLOUserBillingAccountsResponse)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }
}