package com.tts.gueststar.interfaces

import com.tts.olosdk.models.Billingaccount

interface CreditCardInterface {
    fun chooseCreditCard(item: Billingaccount)
    fun removeCreditCard(item: Billingaccount)
}