package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.OloRestaurantMenuInterface
import com.tts.olosdk.models.OLORestaurantMenuResponse
import com.tts.gueststar.ui.BasePresenterView

class MainMenuPresenter(var view: MainMenuView) : OloRestaurantMenuInterface {
    override fun onSuccessGetRestaurantMenu(response: OLORestaurantMenuResponse) {
        view.onSuccessGetRestaurants(response)
    }

    override fun onFailureGetRestaurantMenu(error: String) {
        view.showError(error)
    }

    override fun onFailureGetRestaurantMenu() {
        view.showGenericError()
    }

    fun getRestaurantMenu(context: Context, id: Int) {
        RxJavaOloApiHelper().getRestaurantMenu(context, id, this)
    }

    interface MainMenuView : BasePresenterView {
        fun onSuccessGetRestaurants(response: OLORestaurantMenuResponse)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }
}