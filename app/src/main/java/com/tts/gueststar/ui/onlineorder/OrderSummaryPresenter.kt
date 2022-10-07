package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.*
import com.tts.olosdk.models.*
import com.tts.gueststar.ui.BasePresenterView

class OrderSummaryPresenter(var view: OrderSummaryView) : DeleteProductFromBasketInterface,
    GetRestaurantCalendarInterface, DeleteTimeWantedtInterface, AddTimewantedInterface,
    ValidateBasketInterface, OloRestaurantMenuInterface, DeleteLoaltyRewardsInterface {

    override fun onSuccessGetRestaurantMenu(response: OLORestaurantMenuResponse) {
        view.onSuccessGetRestaurantMenu(response)
    }

    override fun onFailureGetRestaurantMenu(error: String) {
        view.showError(error)
    }

    override fun onFailureGetRestaurantMenu() {
        view.showGenericError()
    }

    override fun onSuccessValidateBasket(response: OLOBasketResponse) {
        view.onSuccessValidateBasket(response)
    }

    override fun onFailureValidateBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureValidateBasket() {
        view.showGenericError()
    }

    override fun onSuccessAddTimewanted(response: OLOBasketResponse) {
        view.onSuccessAddTimewanted(response)
    }

    override fun onFailureAddTimewanted(error: String) {
        view.showError(error)
    }

    override fun onFailureAddTimewanted() {
        view.showGenericError()
    }

    override fun onSuccessDeleteTimeWanted(response: OLOBasketResponse) {
        view.onSuccessDeleteWantedTime(response)
    }

    override fun onFailureDeleteTimeWanted(error: String) {
        view.showError(error)
    }

    override fun onFailureDeleteTimeWanted() {
        view.showGenericError()
    }

    override fun onSuccessGetRestaurantCalendar(response: OLORestaurantCalendarResponse) {
        view.onSuccessGetRestaurantCalendar(response)
    }

    override fun onFailureGetRestaurantCalendar(error: String) {
        view.showError(error)
    }

    override fun onFailureGetRestaurantCalendar() {
        view.showGenericError()
    }

    override fun onSuccessDeleteProductFromBasket(response: OLOBasketResponse) {
        view.onSuccessDeleteProduct(response)
    }

    override fun onFailureDeleteProductFromBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureDeleteProductFromBasket() {
        view.showGenericError()
    }

    fun deleteProductFromBasket(context: Context, basketId: String, produuctid: Long) {
        RxJavaOloApiHelper().deleteProductFromBasket(context, produuctid, basketId, this)
    }

    fun deleteTimeWantedFromBasket(context: Context, basketId: String) {
        RxJavaOloApiHelper().deleteTimeWantedFromBasket(context, basketId, this)
    }

    fun getRestaurantCalendar(context: Context, produuctid: Int, from: String, to: String) {
        RxJavaOloApiHelper().getRestaurantCalendar(context, produuctid, from, to, this)
    }

    fun validateBasket(context: Context, basketId: String) {
        RxJavaOloApiHelper().validateBasket(context, basketId, this)
    }

    fun getRestaurantMenu(context: Context, id: Int) {
        RxJavaOloApiHelper().getRestaurantMenu(context, id, this)
    }

    fun deleteLoyaltyRewardFromBasket(context: Context, basketId: String, rewardId: String) {
        RxJavaOloApiHelper().deleteLoyaltyRewardFromBasket(context, basketId, rewardId, this)
    }

    fun addTimeWantedToBasket(
        context: Context, basketId: String, day: Int,
        month: Int,
        year: Int,
        hour: Int,
        minutes: Int
    ) {
        RxJavaOloApiHelper().addTimeWantedToBasket(
            context,
            basketId,
            day,
            month,
            year,
            hour,
            minutes,
            this
        )
    }


    interface OrderSummaryView : BasePresenterView {
        fun onSuccessGetRestaurantMenu(response: OLORestaurantMenuResponse)
        fun onSuccessValidateBasket(response: OLOBasketResponse)
        fun onSuccessAddTimewanted(response: OLOBasketResponse)
        fun onSuccessDeleteProduct(response: OLOBasketResponse)
        fun onSuccessDeleteWantedTime(response: OLOBasketResponse)
        fun onSuccessGetRestaurantCalendar(response: OLORestaurantCalendarResponse)
        fun onSuccessDeleteReward(response: OLOBasketResponse)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }

    override fun onSuccessDeleteLoyaltyRewards(response: OLOBasketResponse) {
        view.onSuccessDeleteReward(response)
    }

    override fun onFailureDeleteLoyaltyRewards(error: String) {
        view.showError(error)
    }

    override fun onFailureDeleteLoyaltyRewards() {
        view.showGenericError()
    }
}