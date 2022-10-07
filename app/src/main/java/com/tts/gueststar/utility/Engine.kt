package com.tts.gueststar.utility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.text.InputFilter
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import com.tts.nsrsdkrelevant.cloudconnect.models.CloudConnectResponse
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.ClearLogoutDataListener
import com.tts.gueststar.interfaces.ConfirmForgotPasswordInterface
import com.tts.gueststar.interfaces.OnBackPressedListener
import com.tts.gueststar.interfaces.SaveCurentUpdateVersionForUpdateListener
import com.tts.gueststar.pushnotification.PushNotificationDialog
import com.tts.gueststar.ui.account.AccountPresenter
import java.util.HashMap
import javax.inject.Singleton

@Singleton
class Engine {

    private var alertDialogBuilder: AlertDialog.Builder? = null


    companion object {
        lateinit var savedCloudConnectSettings: CloudConnectResponse
        var setNextPage: String = AppConstants.EMPTY_TAG
        var notificationToken: String = ""
        lateinit var mCurrentPhotoPath: String
        var supportFromLogin = false
        var dontClose = false
        var context: Activity? = null
    }

    fun getNextPage(): String {
        return setNextPage
    }

    fun setNextPage(nextPage: String) {
        setNextPage = nextPage
    }

    fun getNotificationToken(): String {
        return notificationToken
    }

    fun setNotificationToken(token: String) {
        notificationToken = token
    }

    fun getSelectedCloudConnectSettings(): CloudConnectResponse {
        return savedCloudConnectSettings
    }

    fun setSelectedCloudConnectSettings(response: CloudConnectResponse) {
        savedCloudConnectSettings = response
    }

    fun getScale(context: Context, size: Float): Float {
        val result: Float
        val sPx = convertSpToPixels(size, context)
        val dPx = convertDpToPixels(size, context)
        result = sPx / dPx
        return result
    }

    private fun convertDpToPixels(dp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp,
            context.resources.displayMetrics
        )
    }

    fun getDeviceToken(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.DEVICE_TOKEN, "")
    }

    fun saveDeviceToken(
        mySharedPreferences: SharedPreferences,
        token: String
    ) {
        putStringData(mySharedPreferences, AppConstants.DEVICE_TOKEN, token)
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(activity: Activity): String {
        var androidId: String
        Thread().run {
            androidId = Settings.Secure.getString(
                activity.contentResolver, Settings.Secure.ANDROID_ID
            )
        }

        return androidId
    }

    fun showDialog(
        activity: Activity?,
        dialog: PushNotificationDialog
    ) {
        if (activity == null) {
            return
        }
        activity.runOnUiThread {
            if (activity is MainActivity) {
                activity.showDialog(dialog)
            }
        }
    }

    fun convertState(state: String): String? {
        val states = HashMap<String, String>()
        states["Alabama"] = "AL"
        states["Alaska"] = "AK"
        states["Alberta"] = "AB"
        states["American Samoa"] = "AS"
        states["Arizona"] = "AZ"
        states["Arkansas"] = "AR"
        states["Armed Forces (AE)"] = "AE"
        states["Armed Forces Americas"] = "AA"
        states["Armed Forces Pacific"] = "AP"
        states["British Columbia"] = "BC"
        states["California"] = "CA"
        states["Colorado"] = "CO"
        states["Connecticut"] = "CT"
        states["Delaware"] = "DE"
        states["District Of Columbia"] = "DC"
        states["Florida"] = "FL"
        states["Georgia"] = "GA"
        states["Guam"] = "GU"
        states["Hawaii"] = "HI"
        states["Idaho"] = "ID"
        states["Illinois"] = "IL"
        states["Indiana"] = "IN"
        states["Iowa"] = "IA"
        states["Kansas"] = "KS"
        states["Kentucky"] = "KY"
        states["Louisiana"] = "LA"
        states["Maine"] = "ME"
        states["Manitoba"] = "MB"
        states["Maryland"] = "MD"
        states["Massachusetts"] = "MA"
        states["Michigan"] = "MI"
        states["Minnesota"] = "MN"
        states["Mississippi"] = "MS"
        states["Missouri"] = "MO"
        states["Montana"] = "MT"
        states["Nebraska"] = "NE"
        states["Nevada"] = "NV"
        states["New Brunswick"] = "NB"
        states["New Hampshire"] = "NH"
        states["New Jersey"] = "NJ"
        states["New Mexico"] = "NM"
        states["New York"] = "NY"
        states["Newfoundland"] = "NF"
        states["North Carolina"] = "NC"
        states["North Dakota"] = "ND"
        states["Northwest Territories"] = "NT"
        states["Nova Scotia"] = "NS"
        states["Nunavut"] = "NU"
        states["Ohio"] = "OH"
        states["Oklahoma"] = "OK"
        states["Ontario"] = "ON"
        states["Oregon"] = "OR"
        states["Pennsylvania"] = "PA"
        states["Prince Edward Island"] = "PE"
        states["Puerto Rico"] = "PR"
        states["Quebec"] = "QC"
        states["Rhode Island"] = "RI"
        states["Saskatchewan"] = "SK"
        states["South Carolina"] = "SC"
        states["South Dakota"] = "SD"
        states["Tennessee"] = "TN"
        states["Texas"] = "TX"
        states["Utah"] = "UT"
        states["Vermont"] = "VT"
        states["Virgin Islands"] = "VI"
        states["Virginia"] = "VA"
        states["Washington"] = "WA"
        states["West Virginia"] = "WV"
        states["Wisconsin"] = "WI"
        states["Wyoming"] = "WY"
        states["Yukon Territory"] = "YT"

        return states[titleizeState(state)]
    }



    private fun titleizeState(state: String): String {
        var cap = true
        val out = state.toCharArray()
        var i: Int
        val len = state.length
        i = 0
        while (i < len) {
            if (Character.isWhitespace(out[i])) {
                cap = true
                i++
                continue
            }
            if (cap) {
                out[i] = Character.toUpperCase(out[i])
                cap = false
            }
            i++
        }
        return String(out)
    }

    fun getStringData(mySharedPreferences: SharedPreferences, key: String): String? {
        return mySharedPreferences.getString(key, "")
    }

    fun showMsgDialogSessionExpired(title: String, message: String, context: Activity) {

        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    alertDialogBuilder = null
                    dialog.cancel()
                    (context as ClearLogoutDataListener).clearLogoutData()
                }
                val alert = alertDialogBuilder!!.create()
                if (title != "") {
                    alert.setTitle(title)
                }
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isCameraAvailable(context: Context): Boolean {
        val pm = context.packageManager
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    fun checkMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= 23
    }

    fun getDeviceName(mySharedPreferences: SharedPreferences): String {
        var texts = "\n\n\n\n\n\n\n___________________________\n"
        try {
            val version = BuildConfig.VERSION_NAME
            val androidOS = Build.VERSION.RELEASE
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL

            if (model.startsWith(manufacturer)) {
                texts += this.capitalize(model)
            } else {
                texts = texts + capitalize(manufacturer) + " " + model
            }
            texts = "$texts $androidOS \nApp Version $version"
            val email = getUserEmail(mySharedPreferences)
            if (!email.equals(""))
                texts = "$texts  \nEmail used $email"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return texts.replace("\n", "<br/>")
    }

    fun appSupportEmailBody(mySharedPreferences: SharedPreferences, locationName: String): String {
        var texts = "\n\n\n\n\n\n\n_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _\n"
        try {
            val version = BuildConfig.VERSION_NAME
            val androidOS = Build.VERSION.RELEASE

            texts = if (locationName.isEmpty()) {
                "$texts - Android $androidOS \n- App Version $version"
            } else {
                "$texts \n- Location info: $locationName \n\n - Android $androidOS \n" +
                        "- App Version $version"
            }

            val email = getUserEmail(mySharedPreferences)
            if (!email.equals(""))
                texts = "$texts  \n- Email signed up with:\n $email"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return texts
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

    fun hideSoftKeyboard(context: Activity) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(
                context.currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        } catch (e: NullPointerException) {
//            val inputMethodManager =
//                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            e.printStackTrace()
        }
    }

    fun getUserEmail(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.PREFLOGINID, "")
    }

    fun getUserProfileImage(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.IMAGE_URL, "")
    }

    fun getUserFirstName(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.FIRST_NAME, "")
    }

    fun getUserLastName(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.LAST_NAME, "")
    }

    fun getUserFavLocationName(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.FAV_LOCATION, "")
    }

    fun getUserFavLocationId(mySharedPreferences: SharedPreferences): Int? {
        return mySharedPreferences.getInt(AppConstants.FAV_LOCATION_ID, 0)
    }

    fun getUserMobilePhone(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.MOBILE_PHONE, "")
    }

    fun getUserBirthday(mySharedPreferences: SharedPreferences): Long? {
        return mySharedPreferences.getLong(AppConstants.BIRTHDAY, AppConstants.defaultTimestamp)
    }

    fun getAuthToken(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.PREFAUTH_TOKEN, "")
    }

    fun getHashedUserId(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.HASHEDID, "")
    }

    fun getHashedEmail(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.HASEDEMAIL, "")
    }

    fun getDataSendedFlag(mySharedPreferences: SharedPreferences): Boolean {
        return mySharedPreferences.getBoolean(AppConstants.SENDDATAFLAG, false)
    }

    fun getOloAuthToken(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.PREFAUTH_OLO_TOKEN, "")
    }

    fun getAccessToken(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.ACCESS_TOKEN, "")
    }

    fun getCustomerId(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.CUSTOMER_ID, "")
    }

    fun getCardNumber(mySharedPreferences: SharedPreferences): String? {
        return mySharedPreferences.getString(AppConstants.CARD_NUMBER, "")
    }

    fun checkIfLogin(mySharedPreferences: SharedPreferences): Boolean {
        val authToken = getAuthToken(mySharedPreferences)
        return authToken!!.isNotEmpty()
    }

    fun checkIfDataIsSendToBraze(mySharedPreferences: SharedPreferences): Boolean {
        return getDataSendedFlag(mySharedPreferences)
    }

    private fun putBooleanData(mySharedPreferences: SharedPreferences, key: String, data: Boolean) {
        mySharedPreferences.edit().putBoolean(key, data).apply()
    }

    private fun convertSpToPixels(sp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp,
            context.resources.displayMetrics
        )
    }

    fun getPxFromSp(context: Context, size: Float): Float {
        return convertSpToPixels(size, context)
    }

    fun getPxFromDp(context: Context, size: Float): Float {
        return convertDpToPixels(size, context)
    }

    fun isOreoDevice(): Boolean {
        if (Build.VERSION.SDK_INT ==Build.VERSION_CODES.O) {
            return true
        }
        return false
    }

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null && activeNetwork.isConnected) {
                val networkType = activeNetwork.type
                networkType == ConnectivityManager.TYPE_WIFI || networkType == ConnectivityManager.TYPE_MOBILE
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    fun checkEnableGPS(context: Context): Boolean {
        try {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )

        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        return false
    }

    fun getDeviceDensityString(context: Context): String {
        when (context.resources.displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW ->
                return "ldpi"
            DisplayMetrics.DENSITY_MEDIUM ->
                return "mdpi"
            DisplayMetrics.DENSITY_TV, DisplayMetrics.DENSITY_HIGH ->
                return "hdpi"
            DisplayMetrics.DENSITY_260, DisplayMetrics.DENSITY_280, DisplayMetrics.DENSITY_300, DisplayMetrics.DENSITY_XHIGH ->
                return "xhdpi"
            DisplayMetrics.DENSITY_340, DisplayMetrics.DENSITY_360, DisplayMetrics.DENSITY_400, DisplayMetrics.DENSITY_420, DisplayMetrics.DENSITY_440, DisplayMetrics.DENSITY_XXHIGH ->
                return "xxhdpi"
            DisplayMetrics.DENSITY_560, DisplayMetrics.DENSITY_XXXHIGH ->
                return "xxxhdpi"

        }
        return ""
    }

    /* To hide Keyboard */
    fun hideKeyboard(context: Context, currentFocus: View) {
        try {
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    var spaceFilter: InputFilter = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            if (Character.isSpaceChar(source[i])) {
                return@InputFilter ""
            }
        }
        null
    }


    var spaceFilterAfterTwoChars: InputFilter = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            if (Character.isSpaceChar(source[i])) {
                return@InputFilter ""
            }
        }
        null
    }


    var alphabetsFilter: InputFilter = InputFilter { source, _, _, _, _, _ ->
        if (source == "") { // for backspace
            return@InputFilter source
        }
        if (source.toString().matches("[a-zA-Z ]+".toRegex())) {
            source
        } else ""
    }

    fun filterValidPassword(amountEditText: EditText): Boolean {
        return amountEditText.text.length >= AppConstants.minPasswordCharacters
    }

    fun filterValidTransferCard(amountEditText: EditText): Boolean {
        return amountEditText.text.length >= AppConstants.minTransferCard
    }

    fun filterSecurityAnswer(amountEditText: EditText): Boolean {
        return amountEditText.text.length >= AppConstants.minAnswerCharacters
    }

    fun filterState(amountEditText: EditText): Boolean {
        return amountEditText.text.length <= AppConstants.maxStateCharacters
    }

    fun filterValidEmail(amountEditText: EditText): Boolean {
        return amountEditText.text.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(amountEditText.text).matches()
    }

    fun setEnableButton(btnLayout: Button, b: Boolean?) {
        btnLayout.isEnabled = b!!
        if (b)
            setEnableButton(btnLayout)
        else
            setDisableButton(btnLayout)
    }

    private fun setDisableButton(btnLayout: Button) {
        btnLayout.alpha = 0.5.toFloat()
    }

    private fun setEnableButton(btnLayout: Button) {
        btnLayout.alpha = 1.toFloat()
    }

     fun putStringData(mySharedPreferences: SharedPreferences, key: String, data: String) {
        mySharedPreferences.edit().putString(key, data).apply()
    }

    private fun putIntData(mySharedPreferences: SharedPreferences, key: String, data: Int) {
        mySharedPreferences.edit().putInt(key, data).apply()
    }

    private fun putLongData(mySharedPreferences: SharedPreferences, key: String, data: Long) {
        mySharedPreferences.edit().putLong(key, data).apply()
    }


    fun clearDataAfterLogOut(mySharedPreferences: SharedPreferences) {
        putStringData(mySharedPreferences, AppConstants.PREFAUTH_TOKEN, "")
        putStringData(mySharedPreferences, AppConstants.PREFAUTH_OLO_TOKEN, "")
        putStringData(mySharedPreferences, AppConstants.CUSTOMER_ID, "")
        putStringData(mySharedPreferences, AppConstants.CARD_NUMBER, "")
        putStringData(mySharedPreferences, AppConstants.PREFLOGINID, "")
        putStringData(mySharedPreferences, AppConstants.HASHEDID, "")
        putStringData(mySharedPreferences, AppConstants.HASEDEMAIL, "")
        putBooleanData(mySharedPreferences, AppConstants.SENDDATAFLAG, false)
    }


    fun saveData(
        mySharedPreferences: SharedPreferences,
        token: String,
        oloAuthToken: String,
        cardNumber: String,
        customerId: String,
        email: String,
        hashedUserId: String,
        hashedEmail: String

    ) {
        putStringData(mySharedPreferences, AppConstants.PREFAUTH_TOKEN, token)
        putStringData(mySharedPreferences, AppConstants.PREFAUTH_OLO_TOKEN, oloAuthToken)
        putStringData(mySharedPreferences, AppConstants.CUSTOMER_ID, customerId)
        putStringData(mySharedPreferences, AppConstants.CARD_NUMBER, cardNumber)
        putStringData(mySharedPreferences, AppConstants.PREFLOGINID, email)
        putStringData(mySharedPreferences, AppConstants.HASHEDID, hashedUserId)
        putStringData(mySharedPreferences, AppConstants.HASEDEMAIL, hashedEmail)

    }
    fun saveSendFlag(
        mySharedPreferences: SharedPreferences,
        sendData: Boolean

    ) {
        putBooleanData(mySharedPreferences, AppConstants.SENDDATAFLAG, sendData)

    }

    fun saveUserProfileData(
        mySharedPreferences: SharedPreferences,
        firstName: String,
        lastName: String,
        mobilePhone: String,
        favLocation: String,
        favLocationId: Int,
        birthday: Long,
        imageUrl: String,
        hashedUserId: String,
        hashedEmail: String
    ) {
        putStringData(mySharedPreferences, AppConstants.FIRST_NAME, firstName)
        putStringData(mySharedPreferences, AppConstants.LAST_NAME, lastName)
        putStringData(mySharedPreferences, AppConstants.MOBILE_PHONE, mobilePhone)
        putStringData(mySharedPreferences, AppConstants.FAV_LOCATION, favLocation)
        putIntData(mySharedPreferences, AppConstants.FAV_LOCATION_ID, favLocationId)
        putStringData(mySharedPreferences, AppConstants.IMAGE_URL, imageUrl)
        putLongData(mySharedPreferences, AppConstants.BIRTHDAY, birthday)
        putStringData(mySharedPreferences, AppConstants.HASHEDID, hashedUserId)
        putStringData(mySharedPreferences, AppConstants.HASEDEMAIL, hashedEmail)

    }


    fun showMsgDialogNoTitleClose(message: String, context: Activity) {
        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    alertDialogBuilder = null
                    dialog.cancel()
                    context.onBackPressed()
                }
                val alert = alertDialogBuilder!!.create()
                alert.setTitle("")
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showMsgDialogNoTitleCloseFragment(message: String, context: Activity) {
        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    alertDialogBuilder = null
                    dialog.cancel()
                    (context as OnBackPressedListener).onBackPressed()
                }
                val alert = alertDialogBuilder!!.create()
                alert.setTitle("")
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showMsgDialogNoTitle(message: String, context: Activity) {
        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    alertDialogBuilder = null
                    dialog.cancel()
                }
                val alert = alertDialogBuilder!!.create()
                alert.setTitle("")
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showMsgDialog(title: String, message: String, context: Activity) {
        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    alertDialogBuilder = null
                    dialog.cancel()
                }
                val alert = alertDialogBuilder!!.create()
                if (title != "") {
                    alert.setTitle(title)
                } else {
                    alert.setTitle(R.string.app_name)
                }
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showDeleteWarnMsgDialog(title: String, message: String, context: Activity,authToken:String, presenter: AccountPresenter) {
        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!
                    .setMessage(message)
                    .setPositiveButton(
                    "Yes"
                ) { dialog, _ ->
                        presenter.deleteAccount(authToken,context)
                }
                    .setNegativeButton(
                        "No"
                    ) { dialog, _ ->
                        alertDialogBuilder = null
                        dialog.cancel()
                    }
                val alert = alertDialogBuilder!!.create()
                if (title != "") {
                    alert.setTitle(title)
                } else {
                    alert.setTitle(R.string.app_name)
                }
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showMsgDialogForgetPass(title: String, message: String, context: Activity) {
        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    alertDialogBuilder = null
                    dialog.cancel()
                    (context as ConfirmForgotPasswordInterface).onSuccessSubmittedForgotPassword()
                }
                val alert = alertDialogBuilder!!.create()
                if (title != "") {
                    alert.setTitle(title)
                }
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkAndRequestPermissionsCamera(activity: Activity): Boolean {
        val locationPermission =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        val listPermissionsNeeded = ArrayList<String>()
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity, listPermissionsNeeded.toTypedArray(), AppConstants.ACCESS_CAMERA_REQUEST
            )
            return false
        }
        return true
    }

    fun checkAndRequestLocationsPermissions(activity: Activity): Boolean {
        val locationPermission =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        val listPermissionsNeeded = ArrayList<String>()
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                listPermissionsNeeded.toTypedArray(),
                AppConstants.ACCESS_LOCATION_REQUESTS
            )
            return false
        }
        return true
    }

    fun verifyStoragePermissions(activity: Activity): Boolean {
        // Check if we have write permission
        val permission =
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionRead =
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED && permissionRead != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                AppConstants.PERMISSIONS_STORAGE,
                AppConstants.REQUEST_EXTERNAL_STORAGE
            )
            return false
        }
        return true
    }

//    fun showDialog(
//        activity: Activity?,
//        dialog: PushNotificationDialog
//    ) {
//        if (activity == null) {
//            return
//        }
//        activity.runOnUiThread {
//            if (activity is MainActivity) {
//                activity.showDialog(dialog)
//            }
//        }
//    }

//    fun showMsgDialogSessionExpired(title: String, message: String, context: Activity) {
//
//        try {
//            if (alertDialogBuilder == null) {
//                alertDialogBuilder = AlertDialog.Builder(context)
//                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
//                    "OK"
//                ) { dialog, _ ->
//                    alertDialogBuilder = null
//                    dialog.cancel()
//                    (context as ClearLogoutDataListener).clearLogoutData()
//                }
//                val alert = alertDialogBuilder!!.create()
//                if (title != "") {
//                    alert.setTitle(title)
//                }
//                alert.show()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }

    fun showRequiredUpdateDialog(title: String, message: String, context: Activity) {

        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    alertDialogBuilder = null
                    dialog.cancel()
                    try {
                        val uri = Uri.parse("market://details?id=" + context.packageName)
                        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(goToMarket)
                    } catch (e: ActivityNotFoundException) {
                        showMsgDialog("", "Couldn't launch the market", context)
                    }
                }
                val alert = alertDialogBuilder!!.create()
                if (title != "") {
                    alert.setTitle(title)
                }
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun showOptionalDialog(title: String, message: String, version: String, context: Activity) {

        try {
            if (alertDialogBuilder == null) {
                alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder!!.setMessage(message).setCancelable(false).setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    alertDialogBuilder = null
                    dialog.cancel()
                    try {
                        val uri = Uri.parse("market://details?id=" + context.packageName)
                        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(goToMarket)
                    } catch (e: ActivityNotFoundException) {
                        showMsgDialog("", "Couldn't launch the market", context)
                    }
                }
                    .setNegativeButton(
                        "Not now"
                    ) { dialog, _ ->
                        alertDialogBuilder = null
                        dialog.cancel()
                        (context as SaveCurentUpdateVersionForUpdateListener).saveCurentUpdateVersionForUpdate(
                            version
                        )
                    }
                val alert = alertDialogBuilder!!.create()
                if (title != "") {
                    alert.setTitle(title)
                }
                alert.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveAppUpdateVersion(mySharedPreferences: SharedPreferences, version: String) {
        putBooleanData(mySharedPreferences, version, true)
    }

    fun getAppUpdateVersion(mySharedPreferences: SharedPreferences, version: String): Boolean {
        return mySharedPreferences.getBoolean(version, false)
    }
}