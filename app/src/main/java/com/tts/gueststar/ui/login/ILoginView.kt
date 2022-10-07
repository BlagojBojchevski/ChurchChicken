package com.tts.gueststar.ui.login

import app.com.relevantsdk.sdk.models.OauthTokenResponse
import app.com.relevantsdk.sdk.models.SignInResponse
import app.com.relevantsdk.sdk.models.UserProfileResponse
import com.tts.olosdk.models.OLOGetOrCreateResponse
import com.tts.gueststar.ui.BasePresenterView

interface ILoginView : BasePresenterView {
    fun onSuccessGetOrcreate(response: OLOGetOrCreateResponse)
    fun onSuccessOloSettings()
    fun disableLogin()
    fun enableLogin()
    fun successfulLogin(response: SignInResponse)
    fun successGetUserProfile(response: UserProfileResponse?)
    fun failedGetUserProfile()
    fun onSuccessGetOuathToken(response: OauthTokenResponse)
    fun onFailedOauthToken(error: String)
    fun onFailedOauthToken()
    fun onFailedOauthTokenUnauthorized(error: String)
}