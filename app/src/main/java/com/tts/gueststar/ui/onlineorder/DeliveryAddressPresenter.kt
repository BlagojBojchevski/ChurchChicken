package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.gueststar.utility.AppConstants
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.*
import com.tts.olosdk.models.AddDeliveryAddressResponse
import com.tts.olosdk.models.OLOBasketResponse
import com.tts.olosdk.models.OloBasketTrasnfer
import com.tts.olosdk.models.UserDeliveryAddressesResponse
import com.tts.gueststar.ui.BasePresenterView


class DeliveryAddressPresenter(var view: DeliveryAddressView) : AddDeliveryAddressInterface,
    DeliveryAddressSettingsInterface, CreatebasketInterface, AddDeliveryModeInterface,
    UserDeliveryAddressesInterface, UserDeleteAddressesInterface, OloTransferBasketInterface {
    override fun onSuccessTransferBasket(response: OloBasketTrasnfer) {
        view.onSuccessTransferBasket(response)
    }

    override fun onFailureTransferBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureTransferBasket() {
        view.showGenericError()
    }

    override fun onSuccessUserDeleteAddresses() {
        view.onSuccessDeleteAddress()
    }

    override fun onFailureUserDeleteAddresses(error: String) {
        view.showError(error)
    }

    override fun onFailureUserDeleteAddresses() {
        view.showGenericError()
    }

    override fun onSuccessUserDeliveryAddresses(response: UserDeliveryAddressesResponse) {
        view.onSuccessGetUserDeliveryAddresses(response)
    }

    override fun onFailureUserDeliveryAddresses(error: String) {
        view.showError(error)
    }

    override fun onFailureUserDeliveryAddresses() {
        view.showGenericError()
    }

    override fun onSuccessAddDeliveryMode(response: OLOBasketResponse) {
        view.onSuccessAddDeliveryMode(response)
    }

    override fun onFailureAddDeliveryMode(error: String) {
        view.showError(error)
    }

    override fun onFailureAddDeliveryMode() {
        view.showGenericError()
    }

    override fun onSuccessCreateBasket(response: OLOBasketResponse) {
        view.onSuccessCreateBasket(response)
    }

    override fun onFailureCreateBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureCreateBasket() {
        view.showGenericError()
    }

    override fun onSuccessDeliveryAddressSettings() {
        //      view.onSuccessDeliverySettings()
    }

    override fun onFailureDeliveryAddressSettings(error: String) {
        view.showError(error)
    }

    override fun onFailureDeliveryAddressSettings() {
        view.showGenericError()
    }

    override fun onSuccessAddDeliveryAddress(response: AddDeliveryAddressResponse) {
        view.onSuccessAddDeliveryAddress(response)
    }

    override fun onFailureAddDeliveryAddress(error: String) {
        view.showError(error)
    }

    override fun onFailureAddDeliveryAddress() {
        view.showGenericError()
    }

    fun getUserDeliveryAddresses(context: Context, oloAuthToken: String) {
        RxJavaOloApiHelper().getUserDeliveryAddresses(context, oloAuthToken, this)
    }

    fun deleteUserDeliveryAddresses(context: Context, oloAuthToken: String, addressId: Int) {
        RxJavaOloApiHelper().deleteUserDeliveryAddresses(context, oloAuthToken, addressId, this)
    }

    fun transferBasket(context: Context, vendorId: Int, basketId: String) {
        RxJavaOloApiHelper().transferBasket(context, vendorId, basketId, this)
    }

    fun addDeliveryAddress(
        context: Context,
        isDispatch: Boolean,
        street: String,
        buildingName: String,
        city: String,
        zipcode: String,
        otherInstructions: String,
        basketId: String
    ) {
        RxJavaOloApiHelper().addDeliveryAddress(
            context,
            isDispatch,
            street,
            buildingName,
            city,
            zipcode,
            otherInstructions,
            basketId,
            this
        )
    }

    fun addDeliveryAddressWithId(
        context: Context,
        isDispatch: Boolean,
        id: Int,
        street: String,
        buildingName: String,
        city: String,
        zipcode: String,
        otherInstructions: String,
        basketId: String
    ) {
        RxJavaOloApiHelper().addDeliveryAddressWithId(
            context,
            isDispatch,
            id,
            street,
            buildingName,
            city,
            zipcode,
            otherInstructions,
            basketId,
            this
        )
    }

    fun addDeliveryModeToBasket(context: Context, deliveryMode: String, basketId: String) {
        RxJavaOloApiHelper().addDeliveryModeToBasket(context, deliveryMode, basketId, this)
    }

    fun createBasket(context: Context, vendorId: Int, oloAuthToken: String) {
        RxJavaOloApiHelper().createBasket(context, vendorId, oloAuthToken, this)
    }

    fun addressFieldsValidateion(
        address: String, buildingName: String, city: String, zipcode: String
    ) {
        if (address.length < AppConstants.minAnswerCharacters || city.length < AppConstants.minAnswerCharacters || zipcode.length < AppConstants.zipCodeCharacters)
            view.disableButton()
        else
            view.enableButton()
    }


    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }


    interface DeliveryAddressView : BasePresenterView {
        fun onSuccessCreateBasket(response: OLOBasketResponse)
        fun onSuccessDeleteAddress()
        fun onSuccessAddDeliveryMode(response: OLOBasketResponse)
        fun onSuccessTransferBasket(response: OloBasketTrasnfer)
        fun onSuccessGetUserDeliveryAddresses(response: UserDeliveryAddressesResponse)
        fun onSuccessAddDeliveryAddress(response: AddDeliveryAddressResponse)
        fun disableButton()
        fun enableButton()
    }
}