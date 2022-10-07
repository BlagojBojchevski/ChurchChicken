package com.tts.gueststar.utility

import android.Manifest
import android.os.Build

class AppConstants {
    companion object {
        const val maxPasswordCharacters = 20
        const val maxEmailCharacters = 100
        const val maxStateCharacters = 3
        const val minPasswordCharacters = 7
        const val minTransferCard = 13
        const val minAnswerCharacters = 2
        const val zipCodeCharacters = 5
        const val DEVICE_TYPE = "android"
        const val REGISTER_TYPE = "1"
        const val defaultTimestamp = -2208988793
        var androidOS = Build.VERSION.RELEASE!!
        var model = Build.MODEL!!
        var manufacturer = Build.MANUFACTURER!!
        var signInConnectType = "2"
        var signUpConnectType = "1"

        const val colorYellow= "ffffff"
        var DEVICE_TOKEN = "deviceToken"
        var ACCESS_TOKEN = "accessToken"

        var PREFAUTH_TOKEN = "authtoken"
        var PREFAUTH_OLO_TOKEN = "authtokenolo"
        var CUSTOMER_ID = "customer_id"
        var CARD_NUMBER = "card_number"
        var PREFLOGINID = "prefloginid"
        var FIRST_NAME = "firstName"
        var LAST_NAME = "lastName"
        var MOBILE_PHONE = "mobilePhone"
        var FAV_LOCATION = "favLocation"
        var FAV_LOCATION_ID = "favLocationId"
        var BIRTHDAY = "birthday"
        var IMAGE_URL = "imageUrl"
        var HASHEDID = "hashedId"
        var HASEDEMAIL = "hasedEmail"

        var SENDDATAFLAG = "sendDataFlag"

        var EMPTY_TAG = "emptyTag"
        var TAG_UPDATE_PASSWORD = "tagUpdatePassword"
        var TAG_PERSONAL_SETTINGS = "tagPersonalSettings"
        var TAG_HOME = "tagHome"
        var TAG_PROMO = "tagPromoCode"
        var TAG_NOTIFICATIONS_CENTER = "tagNotificationsCenter"
        var TAG_MANAGE_PAYMENT = "tagManagePayment"
        var TAG_SUBMIT_RECEIPT= "tagSubmitReceipt"
        var TAG_REFER_FRIEND = "tagReferFriend"
        var TAG_MAIN_SIGN_UP = "tagMainSingUp"
        var TAG_MAIN_SIGN_UP2 = "tagMainSingUp2"
        var TAG_SIGN_UP = "tagSignUp"
        var TAG_ACCOUNT = "tagAccount"
        var TAG_ONLINE_ORDER = "tagOnlineOrder"
        var TAG_ADD_CREDIT_CARD = "tagAddCreditCard"
        var TAG_FAV_LOCATION = "tagFavLocation"
        var TAG_LOCATIONS = "tagLocations"
        var TAG_REWARDS = "tagRewards"
        var TAG_SELECTED_REWARD = "tagSelectedReward"
        var TAG_EARN_PAY = "tagEarnPay"
        var TAG_REDEEMED_REWARD = "tagRedeemedReward"
        var TAG_WEB_VIEW = "webViewFragment"
        var TAG_CONTACT_US = "contactUsFragment"
        var TAG_SUPPORT_LOCATIONS = "supportLocationsFragment"
        var TAG_SURVEY = "surveyFragment"
        var TAG_ACTIVITY_HISTORY = "tagActivityHistory"
        var TAG_SURVEY_THANK_YOU = "tagSurveyThankYou"
        var TAG_TRANSFER_CARD = "tagTransferCard"
        var TAG_PAY_BILL = "tagPayBill"

        var FROM_NAVIGATION = "fromNavigation"
        var FROM_CONTACT_US = "fromContactUs"
        var BITMAP = "bitmap"

        var RESTAURANT_RESPONSE = "restaurantresponse"
        var DELIVERY_TYPE = "deliveryType"
        var EXTERNAL_ID = "externalId"
        var DELIVERY_ADDRESSES = "deliveryAddresses"
        var SELECTED_CATEGORY = "deliveryAddresses"
        var SELECTED_ITEM = "deliveryItem"
        var EDIT_ITEM = "editItem"
        var IMAGE_PATH = "imagePath"
        var FROM_EDIT = "fromEdit"


        var SELECTED_NOTIFICATION = "selectedNotification"
        var SELECTED_LOCATION = "selectedLocation"
        var SELECTED_REWARD = "selectedReward"
        var USER_POINTS = "userPoints"

        const val PUSH_NOTIFICATION_TAG = "Starbird Message"
        var SELECTED_REWARD_NAME = "selectedRewardName"
        var SELECTED_REWARD_IMAGE = "selectedRewardImage"
        var SELECTED_REWARD_POINTS = "selectedRewardPoints"

        var isFromCameraScanner = false
        var scannedCode = ""

        var EXTRA_IS_APP_SUPPORT = "isAppSupport"
        var EXTRA_IS_FROM_HOME_FRAGMENT = "isFromHomeFragment"
        var EXTRA_IS_FROM_HISTORY= "isFromHistory"
        var EXTRA_IS_FROM_SUMMARY = "isFromSummary"

        var EXTRA_IS_FROM_DELIVERY_ADDRESSES = "isFromDeliveryAddresses"
        var EXTRA_IS_FROM_CURBSIDE_DELIVERY = "isFromCurbSIdeDelivery"
        var EXTRA_IS_FROM_REWARDS= "isFromRewards"
        var EXTRA_IS_FROM_SELECTED_REWARD = "isFromSelectedReward"
        var EXTRA_SURVEY = "extraSurvey"
        var EXTRA_LOCATION_ID_SURVEY = "extraLocationIdSurvey"
        var EXTRA_OFFER_ID = "extraOfferId"
        var EXTRA_THANK_YOU_TEXT = "extraThankYouText"
        var EXTRA_IS_FROM_REWARDS_ACTIVITY_HISTORY = "isFromRewardsActivityHistory"

        var AFTER_CONTACT_US_LOCATION_PERMISSIONS = ""

        var TAG_APP_SUPPORT = "tagAppSupport"
        var TAG_PROVIDE_FEEDBACK = "tagSendFeedback"

        var WEB_VIEW_URL = "webViewUrl"
        var WEB_VIEW_TITLE = "webViewTag"
        var dinpromedium = "dinpromedium.otf"
        var omnesregular = "omnesregular.otf"

        var mail = "mail"
        var password = "password"

        const val EXTRA_RECEIPT = "extraReceipt"
        const val EXTRA_CODE = "extraCode"
        const val EXTRA_BALANCE = "extraBalance"
        const val EXTRA_IS_ALREADY_PAYED = "isAlreadyPayed"
        const val EXTRA_RECEIPT_DETAILS = "extraReceiptDetails"

        const val IS_FROM_ONLINE_ORDER = "isFromOnlineOrder"

        const val ACCESS_LOCATION_REQUESTS = 1212
        const val ACCESS_CAMERA_REQUEST = 1313
        const val REQUEST_EXTERNAL_STORAGE = 1
        val PERMISSIONS_STORAGE =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}