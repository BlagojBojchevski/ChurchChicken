package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.*
import com.tts.olosdk.models.Billingfield
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OLOUserFavoriteBasketsResponse
import com.tts.olosdk.models.OLOgetBillingSchemesResponse
import com.tts.gueststar.ui.BasePresenterView

class OrderCheckoutPresenter(var view: OrderCheckoutView) : ApplyTipInterface,
    ApplyCouponInterface, RemoveCouponInterface,
    GetBillingSchemesInterface, AddToFavoriteInterface, SubmitBasketInterface, GetBasketInterface {
    override fun onSuccessSubmitBasket(response: OLOBasketResponse) {
        view.onSuccessSubmitBasket(response)
    }

    override fun onFailureSubmitBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureSubmitBasket() {
        view.showGenericError()
    }

    override fun onSuccessAddToFavorite(response: OLOUserFavoriteBasketsResponse) {
        view.onSuccessAddFavorite(response)
    }

    override fun onFailureAddToFavorite(error: String) {
        view.showError(error)
    }

    override fun onFailureAddToFavorite() {
        view.showGenericError()
    }

    override fun onSuccessGetBillingSchemes(response: OLOgetBillingSchemesResponse) {
        view.onSuccessGetBillingSchemes(response)
    }

    override fun onFailureGetBillingSchemes(error: String) {
        view.showError(error)
    }

    override fun onFailureGetBillingSchemes() {
        view.showGenericError()
    }

    override fun onSuccessRemoveCoupon(response: OLOBasketResponse) {
        view.onSuccessRemoveCoupon(response)
    }

    override fun onFailureRemoveCoupon(error: String) {
        view.showError(error)
    }

    override fun onFailureRemoveCoupon() {
        view.showGenericError()
    }

    override fun onSuccessApplyCoupon(response: OLOBasketResponse) {
        view.onSuccessApplyCoupon(response)
    }

    override fun onFailureApplyCoupon(error: String) {
        view.showError(error)
    }

    override fun onFailureApplyCoupon() {
        view.showGenericError()
    }

    override fun onSuccessApplyTip(response: OLOBasketResponse) {
        view.onSuccessApplyTip(response)
    }

    override fun onFailureApplyTip(error: String) {
        view.showError(error)
    }

    override fun onFailureApplyTip() {
        view.showGenericError()
    }

    fun addFavoriteBasket(
        context: Context,
        basketId: String,
        name: String,
        oloAuthToken: String,
        isDefault: Boolean
    ) {
        RxJavaOloApiHelper().addFavoriteBasket(
            context,
            name,
            basketId,
            oloAuthToken,
            isDefault,
            this
        )
    }


    fun submitBasket(
        context: Context,
        accountId: Long? = null,
        oloAuthToken: String,
        billingmethod: String,
        emailaddress: String,
        firstname: String,
        lastname: String,
        cardnumber: String? = null,
        cvv: String? = null,
        zip: String? = null,
        year: Int? = null,
        month: Int? = null,
        billingField: Billingfield? = null,
        billingschemeid: String? = null,
        number: String,
        basketId: String,
        saveOnFile: Boolean? = null
    ) {
        RxJavaOloApiHelper().submitBasket(
            context,
            accountId,
            oloAuthToken,
            billingmethod,
            emailaddress,
            firstname,
            lastname,
            cardnumber,
            cvv,
            zip,
            year,
            month,
            number, billingField, billingschemeid,
            basketId,
            saveOnFile,
            this
        )
    }


    fun applyCouponToBasket(context: Context, basketId: String, couponcode: String) {
        RxJavaOloApiHelper().applyCouponToBasket(context, basketId, couponcode, this)
    }

    fun deleteCouponFromBasket(context: Context, basketId: String) {
        RxJavaOloApiHelper().deleteCouponFromBasket(context, basketId, this)
    }

    fun applyTipToBasket(context: Context, basketId: String, amount: Double) {
        RxJavaOloApiHelper().applyTipToBasket(context, basketId, amount, this)
    }

    fun getBillingSchemes(context: Context, basketId: String) {
        RxJavaOloApiHelper().getBillingSchemes(context, basketId, this)
    }


    fun getBasket(context: Context, basketId: String) {
        RxJavaOloApiHelper().getBasket(context, basketId, this)
    }

    interface OrderCheckoutView : BasePresenterView {
        fun onSuccessAddFavorite(response: OLOUserFavoriteBasketsResponse)
        fun onSuccessGetBillingSchemes(response: OLOgetBillingSchemesResponse)
        fun onSuccessApplyCoupon(response: OLOBasketResponse)
        fun onSuccessRemoveCoupon(response: OLOBasketResponse)
        fun onSuccessApplyTip(response: OLOBasketResponse)
        fun onSuccessSubmitBasket(response: OLOBasketResponse)
        fun onSuccessGetBasket(response: OLOBasketResponse)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }

    override fun onSuccessGetBasket(response: OLOBasketResponse) {
        view.onSuccessGetBasket(response)
    }

    override fun onFailureGetBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureGetBasket() {
        view.showGenericError()
    }
}