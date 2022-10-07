
package com.tts.gueststar.ui.account
import android.content.Context
import app.com.relevantsdk.sdk.interfaces.ApiListener
import app.com.relevantsdk.sdk.models.DeactivateAccountResponse
import app.com.relevantsdk.sdk.models.DeactivateAccountWarnMessageResponse
import app.com.relevantsdk.sdk.models.NotificationCountResponse
import app.com.relevantsdk.sdk.models.SignOutResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.ui.BasePresenterView
import rxjavasdk.api.RxJavaApiHandler

class AccountPresenter(var view: AccountView) : ApiListener.SignOutInterface,
    ApiListener.GetNotificationCountInterface, ApiListener.DeactivateAccountWarnInterface,
ApiListener.DeactivateAccountInterface{
    override fun onSuccessNotificationCount(response: NotificationCountResponse) {
        view.onSuccessNotificationCount(response)
    }

    override fun onFailureUnauthorized(error: String) {
        view.onFailureUnauthorized(error)
    }

    override fun onFailure() {
        view.showGenericError()
    }

    override fun onSuccess(response: DeactivateAccountWarnMessageResponse?) {
        response?.let {
            view.onSuccessGetWarning(it)
        }

    }

    override fun onSuccess(response: DeactivateAccountResponse?) {
        response?.let {
            view.onSuccessDeleteAccount(response)
        }
    }

    override fun onFailure(error: String) {
        view.showError(error)
    }

    override fun onSuccess(response: SignOutResponse?) {
        if (response!!.status)
            view.onSuccessLogOut(response)
        else {
            if (response.message.isNotEmpty())
                view.showError(response.message)
            else
                view.showGenericError()
        }
    }

    fun logOut(auth_token: String, context: Context) {
        RxJavaApiHandler().signOut(
            context, BuildConfig.APPKEY,
            auth_token, this
        )
    }

    fun deactivateAccountWarnMessage(auth_token: String, context: Context){
        RxJavaApiHandler().deactivateAccountWarnMessage(
            context,BuildConfig.APPKEY,
            auth_token,this
        )
    }

    fun deleteAccount(auth_token: String, context: Context){
        RxJavaApiHandler().deleteAccount(
            context,BuildConfig.APPKEY,
            auth_token,this
        )
    }

    fun getNotificationCount(context: Context, auth_token: String) {
        RxJavaApiHandler().getNotificationCount(context, BuildConfig.APPKEY, auth_token, this)
    }


    interface AccountView : BasePresenterView {
        fun onSuccessLogOut(response: SignOutResponse)
        fun onFailureUnauthorized(error: String)
        fun onSuccessNotificationCount(response: NotificationCountResponse)
        fun onSuccessGetWarning(response: DeactivateAccountWarnMessageResponse)
        fun onSuccessDeleteAccount(response: DeactivateAccountResponse)
    }

    fun onDestroy(){
      RxJavaApiHandler().onDestroy()
    }
}