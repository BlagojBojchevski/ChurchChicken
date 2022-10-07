package com.tts.gueststar.interfaces

interface StoreFeedbackInterface {
    fun getSurvey(surveyId: Int, locationId: Int, offerId: Int)
}