package com.tts.gueststar.ui.onlineorder

import android.content.Context
import com.tts.olosdk.RxJavaOloApiHelper
import com.tts.olosdk.interfaces.AddProductToBasketInterface
import com.tts.olosdk.interfaces.OloItemDetailsInterface
import com.tts.gueststar.ui.BasePresenterView
import com.tts.olosdk.interfaces.AddProductToBasketBatchInterface
import com.tts.olosdk.models.*

class ItemDetailsPresenter(var view: ItemDetailsView) : OloItemDetailsInterface,
     AddProductToBasketInterface, AddProductToBasketBatchInterface {
    override fun onSuccessAddProductToBasket(response: OLOBasketResponse) {
      view.onSuccessAddProductToBasket(response)
    }

    override fun onFailureAddProductToBasket(error: String) {
        view.showError(error)
    }

    override fun onFailureAddProductToBasket() {
        view.showGenericError()
    }

//    override fun onSuccessGetItemNestedModifiers(
//        response: OLOProductModifiersResponse,
//        parentId: Int
//    ) {
//        view.onSuccessGetItemNestedModifiers(response, parentId)
//    }
//
//    override fun onFailureGetItemNestedModifiers(error: String) {
//        view.showError(error)
//    }
//
//    override fun onFailureGetItemNestedModifiers() {
//        view.showGenericError()
//    }

    override fun onSuccessGetItemDetails(response: OLOProductModifiersResponse) {
        view.onSuccessGetItemModifiers(response)
    }

    override fun onFailureGetItemDetails(error: String) {
        view.showError(error)
    }

    override fun onFailureGetItemDetails() {
        view.showGenericError()
    }

    fun getProductModifiers(context: Context, productId: Long) {
        RxJavaOloApiHelper().getProductModifiers(context, productId, this)
    }

//    fun getProductNestedModifiers(context: Context, optionId: Int) {
//        RxJavaOloApiHelper().getProductNestedOptions(context, optionId, this)
//    }


    fun addProductToBasket(
        context: Context,
        productId: Long,
        quantity: Int,
        vendorid: Int,
        specialinstructions: String,
        options: String,
        basketId: String
    ) {
        RxJavaOloApiHelper().addProductToBasket(
            context,
            productId,
            quantity,
            vendorid,
            specialinstructions,
            options,
            basketId,
            this
        )
    }
    fun addProductToBasketBatch(
        context: Context,
        productId: Long,
        quantity: Int,
        vendorid: Int,
        specialinstructions: String,
        choices: List<ChoiceX>,
        basketId: String
    ) {
        RxJavaOloApiHelper().addProductToBasketBatch(
            context,
            productId,
            quantity,
            vendorid,
            specialinstructions,
            choices,
            basketId,
            this
        )
    }

    fun updateProductFromBasketBatch(
        context: Context,
        productOptionId:Long,
        productId: Long,
        quantity: Int,
        vendorid: Int,
        specialinstructions: String,
        choices: List<ChoiceX>,
        basketId: String
    ) {
        RxJavaOloApiHelper().updateProductFromBasketBatch(
            context,
            productOptionId,
            productId,
            quantity,
            vendorid,
            specialinstructions,
            choices,
            basketId,
            this
        )
    }

    fun updateProductFromBasket(
        context: Context,
        productOptionId:Long,
        productId: Long,
        quantity: Int,
        vendorid: Int,
        specialinstructions: String,
        options: String,
        basketId: String
    ) {
        RxJavaOloApiHelper().updateProductFromBasket(
            context,
            productOptionId,
            productId,
            quantity,
            vendorid,
            specialinstructions,
            options,
            basketId,
            this
        )
    }


    interface ItemDetailsView : BasePresenterView {
        fun onSuccessAddProductToBasketBatch(response: OLOBasketResponseBatch)
        fun onSuccessAddProductToBasket(response: OLOBasketResponse)
        fun onSuccessGetItemModifiers(response: OLOProductModifiersResponse)
        //fun onSuccessGetItemNestedModifiers(response: OLOProductModifiersResponse, parentId: Int)
    }

    fun onDestroy() {
        RxJavaOloApiHelper().onDestroy()
    }

    override fun onSuccessAddProductToBasketBatch(response: OLOBasketResponseBatch) {
       view.onSuccessAddProductToBasketBatch(response)
    }

    override fun onFailureAddProductToBasketBatch(error: String) {
        view.showError(error)
    }

    override fun onFailureAddProductToBasketBatch() {
        view.showGenericError()
    }
}