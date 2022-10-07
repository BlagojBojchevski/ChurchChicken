package com.tts.gueststar.ui

import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.ValidateUserTokenRequest
import app.com.relevantsdk.sdk.models.ValidateUserTokenResponse
import com.appboy.Appboy
import com.tts.nsrsdkrelevant.cloudconnect.interfaces.CloudConnectInterface
import com.tts.nsrsdkrelevant.cloudconnect.models.CloudConnectResponse
import com.tts.gueststar.BuildConfig
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.RemoveCouponInterface
import com.tts.olosdk.models.OLOBasketResponse
import rxjavacloudconnect.api.RxJavaCloudConnectApiHelper
import rxjavasdk.api.RxJavaApiHandler


class MainPresenter(private var view: MainView) : MainPresenterIml,
    ApiListener.ValidateUserSessionInterface, CloudConnectInterface, RemoveCouponInterface {
    override fun onSuccessRemoveCoupon(response: OLOBasketResponse) {
      view.successRemoveCoupon(response)
    }

    override fun onFailureRemoveCoupon(error: String) {
        view.showError(error)
    }

    override fun onFailureRemoveCoupon() {
    view.showGenericError()
    }

    override fun onSuccessCloudConnect(response: CloudConnectResponse) {
        when {
            response.status -> {
                view.saveCloudConnectSettings(response)
            }
            !response.status -> {
                if (response.notice != null)
                    view.showError(response.notice!!)
                else
                    view.showGenericError()
            }
            else -> view.showGenericError()
        }
    }

    override fun onFailureCloudConnect(error: String) {
        view.showError(error)
    }

    override fun onFailureCloudConnect() {
        view.showGenericError()
    }

    override fun onSuccess(response: ValidateUserTokenResponse?) {
        if (!response!!.user_activity_status) {
            view.showSessionExpired(response.validate_message)
        } else {

        }

        if (response.update_available) {
            if (response.is_required) {
                view.showRequiredUpdate(response.update_message)
            } else {
                view.showOptionalUpdate(response.app_version, response.update_message)
            }
        }
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onFailure() {
        view.showGenericError()
    }
    fun validateToken(
        context: Context,
        notif_token: String,
        notif_type: String,
        auth_token: String
    ) {
        val request = ValidateUserTokenRequest(
            device_token = notif_token,
            notification_type = notif_type
        )
        RxJavaApiHandler().validateUserSession(
            context,
            request,
            BuildConfig.APPKEY,
            auth_token,
            this
        )
    }

    fun deleteCouponFromBasket(context: Context, basketId: String) {
        RxJavaOloApiHelper().deleteCouponFromBasket(context, basketId, this)
    }

    fun getCloudConnectSettings(context: Context) {
        RxJavaCloudConnectApiHelper().getCloudConnectSettings(context, BuildConfig.APPKEY, this)
    }

    override fun showProgress() {
        view.showProgress()
    }

    override fun dismissProgress() {
        view.dismissProgress()
    }

    override fun setUpView() {
        view.setUpView()
    }

    override fun openFragmentUp(fragment: BaseFragment, tag: String) {
        view.openFragmentUp(fragment, tag)
    }

    override fun openFragment(fragment: BaseFragment, tag: String) {
        view.openFragment(fragment, tag)
    }

    override fun openFragmentRight(fragment: BaseFragment, tag: String) {
        view.openFragmentRight(fragment, tag)
    }


    override fun openFragmentRightNew(fragment: BaseFragment, tag: String) {
        view.openFragmentRightNew(fragment, tag)
    }


    override fun openFragmentRightDown(fragment: BaseFragment, tag: String) {
        view.openFragmentRightDown(fragment, tag)
    }

    interface MainView : BasePresenterView {
        fun openFragment(fragment: BaseFragment, tag: String)
        fun openFragmentUp(fragment: BaseFragment, tag: String)
        fun openFragmentRight(fragment: BaseFragment, tag: String)
        fun openFragmentRightNew(fragment: BaseFragment, tag: String)
        fun openFragmentRightDown(fragment: BaseFragment, tag: String)
        fun setUpView()
        fun showProgress()
        fun dismissProgress()
        //        fun onSuccessValidateToken(response: ValidateTokenResponse)
        fun saveCloudConnectSettings(response: CloudConnectResponse)

        fun showSessionExpired(message: String)
        fun showRequiredUpdate(update_message: String)
        fun showOptionalUpdate(app_version: String, update_message: String)
        fun successRemoveCoupon(response:OLOBasketResponse)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
        RxJavaCloudConnectApiHelper().onDestroy()
    }

}