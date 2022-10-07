package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.AddCustomFieldsInterface
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.gueststar.ui.BasePresenterView

class CarDetailsPresenter(var view: CarDetailsView) : AddCustomFieldsInterface{
    override fun onSuccessAddCustomFields(response: OLOBasketResponse) {
      view.onSuccessAddFields(response)
    }

    override fun onFailureAddCustomFields(error: String) {
        view.showError(error)
    }

    override fun onFailureAddCustomFields() {
        view.showGenericError()
    }


    fun addCustomFieldsToBasket(context: Context, id: Int, value: String, basketId: String) {
        RxJavaOloApiHelper().addCustomFieldsToBasket(context, id, value, basketId, this)
    }

    fun checkRequirements(
        make: String, model: String, coloe: String
    ) {
        if (make.length > 1 && model.length > 1 && coloe.length > 1) {
            view.enableButton()
        } else {
            view.disableButton()
        }
    }

    fun checkRequirementsTest(
        make: String, model: String, coloe: String
    ): Boolean {
        return make.length > 1 && model.length > 1 && coloe.length > 1
    }

    interface CarDetailsView : BasePresenterView {
        fun onSuccessAddFields(response: OLOBasketResponse)
        fun disableButton()
        fun enableButton()
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }
}