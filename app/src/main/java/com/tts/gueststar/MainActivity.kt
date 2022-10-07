package com.tts.gueststar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.util.AndroidRuntimeException
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import app.com.relevantsdk.sdk.models.LocationsResponse
import com.appboy.AppboyLifecycleCallbackListener
import com.appboy.support.AppboyLogger
import com.appboy.ui.inappmessage.AppboyInAppMessageManager
import com.tts.gueststar.fragments.*
import com.tts.gueststar.fragments.contact.ContactUsFragment
import com.tts.gueststar.fragments.contact.SupportLocationsFragment
import com.tts.gueststar.fragments.rewards.RewardsFragment
import com.tts.gueststar.fragments.userauth.*
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.MainPresenter
import com.tts.gueststar.interfaces.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.HashMap
import javax.inject.Inject
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.RemoteMessage
import com.tts.nsrsdkrelevant.cloudconnect.models.CloudConnectResponse
import com.tts.gueststar.fragments.contact.SurveyThankYouFragment
import com.tts.gueststar.fragments.managepayment.ManagePaymentFragment
import com.tts.gueststar.fragments.notificationcenter.NotificationCenterFragment
import com.tts.gueststar.fragments.onlineorder.*
import com.tts.gueststar.pushnotification.ActivatePushNotifications
import com.tts.gueststar.pushnotification.NotificationFactory
import com.tts.gueststar.pushnotification.NotificationTypes
import com.tts.gueststar.utility.*
import com.tts.olosdk.models.OLOBasketResponse
import java.lang.NullPointerException

class MainActivity : AppCompatActivity(), MainPresenter.MainView, SetBottomNavigationIcon,
    FirebaseTokenListener, SaveCurentUpdateVersionForUpdateListener, ClearLogoutDataListener,
    View.OnClickListener,
    OpenHyperlinkListener,
    OnFavLocationPicked, ConfirmForgotPasswordInterface,
    OpenFragmentListener {

    override fun successRemoveCoupon(response: OLOBasketResponse) {
        presenter.dismissProgress()
        OrderHelper.basket = response
        super.onBackPressed()
    }


    override fun saveCurentUpdateVersionForUpdate(version: String) {
        Engine().saveAppUpdateVersion(mySharedPreferences, version)
    }

    override fun showRequiredUpdate(update_message: String) {
        Engine().showRequiredUpdateDialog(getString(R.string.app_name), update_message, this)
    }

    override fun showOptionalUpdate(app_version: String, update_message: String) {
        if (!Engine().getAppUpdateVersion(mySharedPreferences, app_version)) {
            Engine().showOptionalDialog(
                getString(R.string.app_name),
                update_message,
                app_version,
                this
            )
        }
    }

    override fun saveCloudConnectSettings(response: CloudConnectResponse) {
        Engine().setSelectedCloudConnectSettings(response)
    }


    override fun openHyperlink(urlpm: String) {
        if (urlpm.length > 2) {
            var url = urlpm
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://$url"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }


    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var nm: NotificationManager? = null
    lateinit var presenter: MainPresenter
    lateinit var app: MyApplication
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private var dialog: AlertDialog? = null
    private var mGPSController: GPSController? = null
    private var showRateApp: Dialog? = null
    private var locationFromMenu = false

    override fun onStart() {
        super.onStart()
        // Change log level to VERBOSE
        AppboyLogger.setLogLevel(Log.VERBOSE);
        // Suppress logs
    //    AppboyLogger.setLogLevel(AppboyLogger.SUPPRESS);
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = application as MyApplication
        app.getMyComponent().inject(this)
        FirebaseApp.initializeApp(applicationContext)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        Engine.context = this
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        showRateApp = Dialog(this@MainActivity)
        presenter = MainPresenter(this)
        presenter.run {
            setUpView()
            if (Engine().checkIfLogin(mySharedPreferences))
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
            else {
                Engine.setNextPage = AppConstants.TAG_HOME
                openFragment(MainSignUpFragment(), AppConstants.TAG_MAIN_SIGN_UP)
            }
        }




        onNavigationIconChange(0)
        setListeners()
        handlePushNotification()

        AppboyLogger.setLogLevel(Log.VERBOSE);
        // Suppress logs
      //  AppboyLogger.setLogLevel(AppboyLogger.SUPPRESS);
    }


    private fun handlePushNotification() {
        val intent = intent
        val type = intent.getStringExtra("type")
        val title = intent.getStringExtra("title")
        var message: String? = intent.getStringExtra("body")
        val url = intent.getStringExtra("url")


        if (type != null && type.isNotEmpty()) {
            if (type != NotificationTypes.MESSAGE.type) {
                handleType(type)
            }
        }
        if (message != null && message.isNotEmpty()) {
            if (type == NotificationTypes.URL.type && url != null && url.isNotEmpty()) {
                message = message + "\n\n" + url
                showPushDialog(title, message, url, false)
            } else if (type == NotificationTypes.MESSAGE.type || type == NotificationTypes.SESSION_EXPIRED.type)
                showPushDialog(title, message, url, false)
            else
                showPushDialog(title, message, url, true)
        }
    }

    private fun showPushDialog(title: String?, message: String, url: String?, isOpenPage: Boolean) {
        val notificationDialog = androidx.appcompat.app.AlertDialog.Builder(this)
        notificationDialog.setMessage(message).setCancelable(false).setPositiveButton(
            "OK"
        ) { dialog, _ ->
            dialog.cancel()
            if (url != null && url.isNotEmpty())
                openUrlInBrowser(url)
            else if (isOpenPage)
                openFragment(RewardsFragment(), AppConstants.TAG_REWARDS)
        }
        val alert = notificationDialog.create()
        alert.setTitle(title)
        alert.show()
    }

    private fun openUrlInBrowser(url_: String) {
        var url = url_
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://$url"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun setListeners() {
        item_home.setOnClickListener(this)
        item_rewards.setOnClickListener(this)
        item_order.setOnClickListener(this)
        item_account.setOnClickListener(this)
    }

    override fun hideBottomNavigation(hide: Boolean) {
        if (hide)
            bottom_navigation.visibility = View.GONE
        else bottom_navigation.visibility = View.VISIBLE
    }

    override fun openFragment(fragment: BaseFragment, tag: String) {
        loadFragment(fragment, tag)
    }

    override fun openFragmentUp(fragment: BaseFragment, tag: String) {
        loadFragmentUp(fragment, tag)
    }

    override fun openFragmentRight(fragment: BaseFragment, tag: String) {
        loadFragmentRight(fragment, tag)
    }

    override fun openFragmentRightDown(fragment: BaseFragment, tag: String) {
        loadFragmentRightDown(fragment, tag)
    }

    override fun openFragmentRightNew(fragment: BaseFragment, tag: String) {
        loadFragmentRightHold(fragment, tag)
    }

    override fun setUpView() {
    }

    override fun showProgress() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.view_custom_progress_dialog)
        dialog = builder.create()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
        dialog?.show()
    }

    override fun dismissProgress() {
        dialog?.dismiss()
    }

    override fun showGenericError() {

    }

    override fun showError(error: String) {

    }

    override fun onNavigationIconChange(position: Int) {
        when (position) {
            0 -> {
                img_item_home.alpha = 1F
                img_item_account.alpha = 0.5F
                img_item_rewards.alpha = 0.5F
                img_item_order.alpha = 0.5F
            }
            1 -> {
                img_item_rewards.alpha = 1F
                img_item_account.alpha = 0.5F
                img_item_home.alpha = 0.5F
                img_item_order.alpha = 0.5F
            }
            2 -> {
                img_item_order.alpha = 1F
                img_item_account.alpha = 0.5F
                img_item_rewards.alpha = 0.5F
                img_item_home.alpha = 0.5F
            }
            3 -> {
                img_item_account.alpha = 1F
                img_item_home.alpha = 0.5F
                img_item_rewards.alpha = 0.5F
                img_item_order.alpha = 0.5F
            }
        }
    }

    fun setMessage(
        message: RemoteMessage.Notification?,
        pushNotificationId: Int,
        type: Map<String, String>
    ) {
        if (type.isNotEmpty()) {
            val typeM = type["type"]
            if (type.isNotEmpty()) {
                if (typeM != NotificationTypes.MESSAGE.type) {
                    handleType(typeM!!)
                }
            }
        }

        if (message != null) {
            clearNotificationStatus(pushNotificationId)
            val notificationFactory = NotificationFactory()
            val notification = notificationFactory.getNotification(this, message, type)
            notification.showDialog(Engine.context!!, message)
        }
    }

    private fun clearNotificationStatus(pushNotificationId: Int) {
        try {
            nm!!.cancel(AppConstants.PUSH_NOTIFICATION_TAG, pushNotificationId)
            nm!!.cancelAll()
        } catch (e: Exception) {
            Log.e("Error: ", e.toString())
        }

    }

    fun showDialog(id: DialogFragment) {

        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(id.toString() + "")
        if (prev != null) {
            supportFragmentManager.beginTransaction().remove(prev).commit()
            ft.remove(prev)
        }
        Handler().post {
            try {
                if (prev == null) {
                    ft.add(id, id.toString() + "")
                    ft.commitAllowingStateLoss()
                }
            } catch (ex: Exception) {
                Log.e("ShowDialog", "Activity is in background", ex)
            }
        }
    }

    private fun handleType(typeM: String) {
        if (typeM == NotificationTypes.POINTS.type || typeM == NotificationTypes.REWARDS.type) {
            //     presenter.openFragment(RewardsFragment(), AppConstants.TAG_REWARDS)
        } else if (typeM == NotificationTypes.URL.type) {
        } else if (typeM == NotificationTypes.MESSAGE.type) {
        } else {
            showSessionExpired("")
        }
    }

    override fun showSessionExpired(message: String) {
        if (Engine().getAuthToken(mySharedPreferences)!!.isNotEmpty()) {
            Engine().showMsgDialogSessionExpired(getString(R.string.app_name), message, this)
        }
    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager
            .findFragmentById(R.id.frame_container)
    }

    private fun getVisibleFragment(fragment: BaseFragment): Fragment? {
        if (getCurrentFragment() == fragment)
            return fragment
        return null
    }

    private fun loadFragment(fragment: BaseFragment, tag: String) {
        if (getVisibleFragment(fragment) == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_container, fragment, tag)
            fragmentTransaction.addToBackStack(tag)
            fragmentTransaction.commit()
        }
    }

    private fun loadFragmentUp(fragment: BaseFragment, tag: String) {
        if (getVisibleFragment(fragment) == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_up,
                0,
                0,
                R.anim.slide_in_down
            )
            fragmentTransaction.replace(R.id.frame_container, fragment, tag)
            fragmentTransaction.addToBackStack(tag)
            fragmentTransaction.commit()
        }
    }

    private fun loadFragmentRight(fragment: BaseFragment, tag: String) {
        if (getVisibleFragment(fragment) == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.anim.push_right_in,
                0,
                0,
                R.anim.push_left_out
            )
            fragmentTransaction.replace(R.id.frame_container, fragment, tag)
            fragmentTransaction.addToBackStack(tag)
            fragmentTransaction.commit()
        }
    }

    private fun loadFragmentRightHold(fragment: BaseFragment, tag: String) {
        if (getVisibleFragment(fragment) == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.anim.push_right_in,
                0,
                0,
                R.anim.hold
            )
            fragmentTransaction.replace(R.id.frame_container, fragment, tag)
            fragmentTransaction.addToBackStack(tag)
            fragmentTransaction.commit()
        }
    }

    private fun loadFragmentRightDown(fragment: BaseFragment, tag: String) {
        if (getVisibleFragment(fragment) == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.anim.push_right_in,
                0,
                0,
                R.anim.slide_in_down
            )
            fragmentTransaction.replace(R.id.frame_container, fragment, tag)
            fragmentTransaction.addToBackStack(tag)
            fragmentTransaction.commit()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.item_home -> {
                Engine.setNextPage = AppConstants.TAG_HOME
                locationFromMenu = false
                onNavigationIconChange(0)
                val currentFragment = getCurrentFragment()
                val fragmentA = supportFragmentManager.findFragmentByTag(AppConstants.TAG_HOME)
                if (fragmentA != null) {
                    clearStack()
                } else {
                    if (currentFragment !is HomeFragment) {
                        clearStack()
                        if (Engine().checkIfLogin(mySharedPreferences)) {
                            Handler().postDelayed({
                                openFragment(HomeFragment(), AppConstants.TAG_HOME)
                            }, 100)
                        } else {
                            Engine.setNextPage = AppConstants.TAG_HOME
                            openFragment(MainSignUpFragment(), AppConstants.TAG_MAIN_SIGN_UP)
                        }
                    }
                }
            }
            R.id.item_rewards -> {
                onNavigationIconChange(1)
                locationFromMenu = false
                Engine().setNextPage(AppConstants.TAG_REWARDS)
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    val currentFragment = getCurrentFragment()
                    val fragmentA =
                        supportFragmentManager.findFragmentByTag(AppConstants.TAG_REWARDS)
                    if (fragmentA != null) {
                        clearStackRewards()
                    } else {
                        if (currentFragment !is RewardsFragment) {
                            clearStackRewards()
                            openFragment(
                                getFragmentRewards(RewardsFragment(), false),
                                AppConstants.TAG_REWARDS
                            )
                        }
                    }
                } else {
                    openFragment(MainSignUpFragment(), AppConstants.TAG_MAIN_SIGN_UP)
                }
            }
            R.id.item_order -> {
                locationFromMenu = true
                val currentFragment = getCurrentFragment()
                if (currentFragment !is LocationsFragment && currentFragment !is DeliveryAddresesFragment && currentFragment !is EnterDeliveryAddressFragment && currentFragment !is EnterCarDetailsFragment  && currentFragment !is SelectOrderModeFragment) {
                    if (Engine().checkAndRequestLocationsPermissions(this)) {
                        clearStackLocations()
//                        Handler().postDelayed({
                        openFragment(LocationsFragment(), AppConstants.TAG_LOCATIONS)
//                        }, 100)

                        onNavigationIconChange(2)
                    }
                }

            }
            R.id.item_account -> {
                locationFromMenu = false
                onNavigationIconChange(3)
                val randomNum = (0..10).random()
                if (randomNum == 7) {
                    if (!MySharedPreferences.getBoolean(
                            applicationContext,
                            MySharedPreferences.rateAppShown
                        )
                    ) {
                        showRateAppDialog()
                    }
                }
                val currentFragment = getCurrentFragment()
                val fragmentA = supportFragmentManager.findFragmentByTag(AppConstants.TAG_ACCOUNT)
                if (fragmentA != null) {
                    clearStackAccount()
                } else {
                    if (currentFragment !is AccountFragment) {
                        clearStackAccount()
                        openFragment(AccountFragment(), AppConstants.TAG_ACCOUNT)
                    }
                }
            }
        }
    }

    private fun showRateAppDialog() {
        try {
            showRateApp = Dialog(this@MainActivity)
            showRateApp!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            showRateApp!!.setCancelable(false)
            showRateApp!!.setContentView(R.layout.dialog_rate_app)
            showRateApp?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val btnYes = showRateApp!!.findViewById(R.id.btn_yes) as TextView
            val btnNo = showRateApp!!.findViewById(R.id.btn_no) as TextView
            val btnAskMeLater = showRateApp!!.findViewById(R.id.btn_ask_me_again) as TextView
            val btnDontAskAgain = showRateApp!!.findViewById(R.id.btn_dont_ask_again) as TextView

            btnYes.setOnClickListener {
                MySharedPreferences.putBoolean(
                    applicationContext,
                    MySharedPreferences.rateAppShown,
                    true
                )
                val appPackageName = packageName
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (anfe: android.content.ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
                showRateApp!!.dismiss()
            }
            btnNo.setOnClickListener {
                val uriText =
                    "mailto:" + getString(R.string.EMAILCONTACT_US) + "?subject=" + getString(R.string.EMAILSUBJECTFAQ) + "&body=" + Engine().getDeviceName(
                        mySharedPreferences
                    )
                val uri = Uri.parse(uriText)
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = uri
                startActivity(Intent.createChooser(emailIntent, "Email"))
                showRateApp!!.dismiss()
            }
            btnAskMeLater.setOnClickListener {
                showRateApp!!.dismiss()
            }
            btnDontAskAgain.setOnClickListener {
                MySharedPreferences.putBoolean(
                    applicationContext,
                    MySharedPreferences.rateAppShown,
                    true
                )
                showRateApp!!.dismiss()
            }

            showRateApp!!.show()
        } catch (er: AndroidRuntimeException) {
        }


    }

    private fun clearStackRewards() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SUBMIT_RECEIPT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_PAY_BILL,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP2,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ACCOUNT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_TRANSFER_CARD,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    fun clearStackOnlineOrder() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    fun clearStackOnlineOrderAndLocations() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        supportFragmentManager.popBackStack(
            AppConstants.TAG_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP2,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        val fragmentA = supportFragmentManager.findFragmentByTag(AppConstants.TAG_HOME)
        if (fragmentA == null) {
            openFragment(HomeFragment(), AppConstants.TAG_HOME)
        }
    }

    fun clearStackAddCreditCard() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ADD_CREDIT_CARD,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun clearStackLocations() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SUBMIT_RECEIPT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_PAY_BILL,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_REWARDS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ACCOUNT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_TRANSFER_CARD,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun clearStackAccount() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SUBMIT_RECEIPT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_PAY_BILL,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP2,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
//        supportFragmentManager.popBackStack(
//            AppConstants.TAG_MAIN_SIGN_UP,
//            FragmentManager.POP_BACK_STACK_INCLUSIVE
//        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_REWARDS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_TRANSFER_CARD,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun clearStack() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SUBMIT_RECEIPT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP2,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_PAY_BILL,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ACCOUNT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_REWARDS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_TRANSFER_CARD,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }


    private fun clearStackAfterForgotPass() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SUBMIT_RECEIPT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_PAY_BILL,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP2,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_REWARDS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_TRANSFER_CARD,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun clearStackSignin() {
//        supportFragmentManager.popBackStack(
//            AppConstants.TAG_MAIN_SIGN_UP,
//            FragmentManager.POP_BACK_STACK_INCLUSIVE
//        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP2,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
//        supportFragmentManager.popBackStack(
//            AppConstants.TAG_SIGN_UP,
//            FragmentManager.POP_BACK_STACK_INCLUSIVE
//        )
    }

    private fun clearStackUpdatePassword() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SUBMIT_RECEIPT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_PAY_BILL,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP2,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        supportFragmentManager.popBackStack(
            AppConstants.TAG_TRANSFER_CARD,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun clearStackMainSignUp() {
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SUBMIT_RECEIPT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_PAY_BILL,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_LOCATIONS,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_MAIN_SIGN_UP2,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_SIGN_UP,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ACCOUNT,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_ONLINE_ORDER,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.popBackStack(
            AppConstants.TAG_TRANSFER_CARD,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
    }

    override fun clearLogoutData() {
        Engine().clearDataAfterLogOut(mySharedPreferences)

        val fragmentA = supportFragmentManager.findFragmentByTag(AppConstants.TAG_MAIN_SIGN_UP)
        if (fragmentA == null) {
            val fragmentRewards = supportFragmentManager.findFragmentByTag(AppConstants.TAG_REWARDS)
            if (fragmentRewards != null) {
                clearStack()
            }
            presenter.openFragmentUp(
                getFragment(MainSignUpFragment()),
                AppConstants.TAG_MAIN_SIGN_UP
            )
        } else {
            onBackPressed()
            presenter.openFragmentUp(
                getFragment(MainSignUpFragment()),
                AppConstants.TAG_MAIN_SIGN_UP
            )
        }
    }

    override fun openFragment() {
        when (Engine().getNextPage()) {
            AppConstants.TAG_HOME -> {
                clearStack()
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
            }

            AppConstants.TAG_MAIN_SIGN_UP -> {
                clearStackMainSignUp()
                openFragment(MainSignUpFragment(), AppConstants.TAG_MAIN_SIGN_UP)
            }

            AppConstants.TAG_MAIN_SIGN_UP2 -> {
                clearStackAfterForgotPass()
                openFragment(MainSignUpFragment(), AppConstants.TAG_MAIN_SIGN_UP2)
            }

            AppConstants.TAG_UPDATE_PASSWORD -> {
                clearStackAfterForgotPass()
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
                openFragment(AccountFragment(), AppConstants.TAG_ACCOUNT)
                openFragmentUp(UpdatePasswordFragment(), AppConstants.TAG_ACCOUNT)
            }

            AppConstants.TAG_PERSONAL_SETTINGS -> {
                clearStackAfterForgotPass()
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
                openFragment(AccountFragment(), AppConstants.TAG_ACCOUNT)
                openFragmentUp(PersonalSettingsFragment(), AppConstants.TAG_ACCOUNT)
            }

            AppConstants.TAG_PROMO -> {
                clearStackUpdatePassword()
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
                openFragment(AccountFragment(), AppConstants.TAG_ACCOUNT)
                openFragmentUp(PromoCodeFragment(), AppConstants.TAG_ACCOUNT)
            }

            AppConstants.TAG_ONLINE_ORDER -> {
                clearStackSignin()
                //  openFragment(HomeFragment(), AppConstants.TAG_HOME)
                OrderHelper.fromLogin = true
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment != null) {
//                        OrderSummaryFragment().afterLogin()
                        if (fragment is OrderSummaryFragment) {
                            fragment.afterLogin()
                        }
                    }
                }
            }

            AppConstants.TAG_REFER_FRIEND -> {
                clearStackUpdatePassword()
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
                openFragment(AccountFragment(), AppConstants.TAG_ACCOUNT)
                openFragmentUp(ReferFriendFragment(), AppConstants.TAG_ACCOUNT)
            }

            AppConstants.TAG_MANAGE_PAYMENT -> {
                clearStackAfterForgotPass()
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
                openFragment(AccountFragment(), AppConstants.TAG_ACCOUNT)
                openFragmentUp(ManagePaymentFragment(), AppConstants.TAG_REWARDS)
            }


            AppConstants.TAG_SURVEY -> {
                Engine.dontClose = true
                onBackPressed()
                onBackPressed()
            }

            AppConstants.TAG_ACTIVITY_HISTORY -> {
                clearStackAfterForgotPass()
                openFragmentUp(ActivityHistoryFragment(), AppConstants.TAG_ACCOUNT)
            }

            AppConstants.TAG_REWARDS -> {
                clearStackAfterForgotPass()
                openFragmentUp(RewardsFragment(), AppConstants.TAG_REWARDS)
            }
            AppConstants.TAG_NOTIFICATIONS_CENTER -> {
                clearStackAfterForgotPass()
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
                openFragment(AccountFragment(), AppConstants.TAG_ACCOUNT)
                openFragmentUp(NotificationCenterFragment(), AppConstants.TAG_ACCOUNT)
            }

            AppConstants.TAG_SUPPORT_LOCATIONS -> {
                clearStackAfterForgotPass()
                openFragmentRight(
                    SupportLocationsFragment(),
                    AppConstants.TAG_SUPPORT_LOCATIONS
                )
            }

            AppConstants.EMPTY_TAG -> {
                clearStack()
                openFragment(HomeFragment(), AppConstants.TAG_HOME)
            }
        }
    }

    override fun onBackPressed() {
        try {
            Engine().hideSoftKeyboard(this)
        } catch (er: NullPointerException) {
        }
        when {
            getCurrentFragment() is HomeFragment -> finish()
            getCurrentFragment() is MainSignUpFragment -> {
                if (bottom_navigation.visibility == View.VISIBLE)
                    finish()
                else {
                    if (Engine.supportFromLogin) {
                        if (Engine.dontClose) {
                            super.onBackPressed()
                        } else {
                            Engine.supportFromLogin = false
                            super.onBackPressed()
                            super.onBackPressed()
                        }
                    } else
                        super.onBackPressed()
                }
            }

            getCurrentFragment() is CheckoutFragment -> {
                if (OrderHelper.basket!!.coupon == null)
                    super.onBackPressed()
                else {
                    if (Engine().isNetworkAvailable(this)) {
                        presenter.showProgress()
                        presenter.deleteCouponFromBasket(this, OrderHelper.basketId!!)
                    } else {
                        Engine().showMsgDialog(
                            "",
                            getString(R.string.error_network_connection),
                            this
                        )
                    }

                }
            }

            getCurrentFragment() is AddNewCreditCardFragment -> {
                clearStackAddCreditCard()
            }

            getCurrentFragment() is AddNewGiftCardFragment -> {
                clearStackAddCreditCard()
            }

            getCurrentFragment() is OrderThankYouFragment -> {
                clearStackOnlineOrderAndLocations()
            }


            getCurrentFragment() is SurveyThankYouFragment -> {
                supportFragmentManager.popBackStack(
                    AppConstants.TAG_SURVEY_THANK_YOU,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                supportFragmentManager.popBackStack(
                    AppConstants.TAG_SURVEY,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                supportFragmentManager.popBackStack(
                    AppConstants.TAG_SUPPORT_LOCATIONS,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }

            getCurrentFragment() is AccountFragment -> {
                img_item_home.alpha = 1F
                img_item_account.alpha = 0.5F
                img_item_rewards.alpha = 0.5F
                img_item_order.alpha = 0.5F
                super.onBackPressed()
            }

//            getCurrentFragment() is RewardsFragment -> {
//                img_item_home.alpha = 1F
//                img_item_account.alpha = 0.5F
//                img_item_rewards.alpha = 0.5F
//                img_item_order.alpha = 0.5F
//                super.onBackPressed()
//            }
            getCurrentFragment() is LocationsFragment -> {
                img_item_home.alpha = 1F
                img_item_account.alpha = 0.5F
                img_item_rewards.alpha = 0.5F
                img_item_order.alpha = 0.5F
                if (Engine().checkIfLogin(mySharedPreferences)) {
                    clearStackAfterForgotPass()
                    openFragment(HomeFragment(), AppConstants.TAG_HOME)
                } else {
                    super.onBackPressed()
                }
            }

            else -> super.onBackPressed()
        }
    }

    override fun onTokenReceived(token: String) {
        if (token.isNotEmpty()) {
            if (token != Engine.notificationToken) {
                Engine().setNotificationToken(token)
                presenter.validateToken(
                    applicationContext,
                    token,
                    "FCM",
                    Engine().getAuthToken(mySharedPreferences).toString()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppboyInAppMessageManager.getInstance().registerInAppMessageManager(this)
        locationFromMenu = false
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        ActivatePushNotifications(this).notificationToken()
        if (Engine().isNetworkAvailable(applicationContext)) {
            var notifToken: String
            notifToken = Engine().getNotificationToken()
            if (notifToken.isEmpty()) {
                notifToken = Engine().getDeviceToken(mySharedPreferences)!!
            }
            presenter.validateToken(
                applicationContext,
                notifToken,
                "FCM",
                Engine().getAuthToken(mySharedPreferences).toString()
            )
            //   presenter.getCloudConnectSettings(applicationContext)
        }
        mGPSController = GPSController.getinstance(this)
        startGPS()
//        try {
//            val info = getPackageManager().getPackageInfo(
//                "com.relevantmobile.olgaskitchen",
//                PackageManager.GET_SIGNATURES
//            ) // Your
//            // package
//            // name
//            // here
//            for (signature in info.signatures) {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                val sign = Base64.encodeToString(md.digest(), Base64.DEFAULT)
//                Log.v("KeyHash:", sign)
//            }
//        } catch (e: PackageManager.NameNotFoundException) {
//        } catch (e: NoSuchAlgorithmException) {
//        }
    }

    private fun startGPS() {
        if (mGPSController != null)
            mGPSController!!.startGPS()
    }

    private fun stopGPS() {
        if (mGPSController != null) {
            mGPSController!!.stopLocationListening()
        }
    }

    fun getGPSController(): GPSController? {
        return mGPSController
    }

    override fun onStop() {
        super.onStop()
        stopGPS()
    }

    override fun onFavLocationPicked(
        location: LocationsResponse.Location,
        locationName: String,
        locationId: Int
    ) {
        onBackPressed()
        for (fragment in supportFragmentManager.fragments) {
            if (fragment != null) {
                if (fragment is SignUpFragment) {
                    fragment.onFavLocationPicked(locationName, locationId)
                }
                if (fragment is PersonalSettingsFragment) {
                    fragment.onFavLocationPicked(locationName, locationId)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.REQUEST_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    // contacts-related task you need to do.
                    for (fragment in supportFragmentManager.fragments) {
                        if (fragment != null) {
                            if (fragment is PersonalSettingsFragment) {
                                if (fragment.forCamera) {
                                    fragment.afterCameraPermissionAllowed()
                                } else {
                                    fragment.afterPermissionAllowed()
                                }
                            }
                        }
                    }
                } else {
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setMessage(getString(R.string.storage_permissions_denied))
                        .setPositiveButton(getString(R.string.text_ok)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                    alertDialog.show()
                }
                return
            }

            AppConstants.ACCESS_CAMERA_REQUEST -> {
                val perms = HashMap<String, Int>()
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED

                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED) {
                        for (fragment in supportFragmentManager.fragments) {
                            if (fragment != null) {
                                when (fragment) {
                                    is PersonalSettingsFragment -> fragment.afterCameraPermissionAllowed()
                                }
                            }
                        }
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.CAMERA
                            )
                        ) {
                            val alertDialog = AlertDialog.Builder(this)
                            alertDialog.setMessage(R.string.camera_permissions_message)
                                .setPositiveButton(getString(R.string.text_ok)) { dialog, _ ->
                                    dialog.dismiss()
                                    Engine().checkAndRequestPermissionsCamera(this)
                                }
                                .setNegativeButton(getString(R.string.dont_allow_text)) { dialog, _ -> dialog.dismiss() }
                                .setCancelable(false)

                        } else {
                            val alertDialog = AlertDialog.Builder(this)
                            alertDialog.setMessage(getString(R.string.camera_permissions_denied))
                                .setPositiveButton(getString(R.string.text_ok)) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .setCancelable(false)
                            alertDialog.show()
                        }
                    }
                }
            }

            AppConstants.ACCESS_LOCATION_REQUESTS -> {
                val perms = HashMap<String, Int>()
                perms[Manifest.permission.ACCESS_FINE_LOCATION] =
                    PackageManager.PERMISSION_GRANTED
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED) {
                        startGPS()
                        if (getCurrentFragment() != null) {
                            when (getCurrentFragment()) {

                                is PersonalSettingsFragment -> presenter.openFragmentRight(
                                    FavoriteLocationFragment(),
                                    AppConstants.TAG_ACCOUNT
                                )

                                is SignUpFragment -> presenter.openFragmentRight(
                                    FavoriteLocationFragment(),
                                    AppConstants.TAG_FAV_LOCATION
                                )

                                is OrderSummaryFragment -> {
                                    presenter.openFragmentRight(
                                        ChangeOrderLocationFragment(),
                                        AppConstants.TAG_ONLINE_ORDER
                                    )
                                }

                                is HomeFragment -> {
                                    if (locationFromMenu) {
                                        clearStackLocations()
                                        openFragment(
                                            LocationsFragment(),
                                            AppConstants.TAG_LOCATIONS
                                        )
                                    } else {
                                        presenter.openFragmentRight(
                                            getFragmentRewards(
                                                FavoriteLocationFragment(),
                                                true
                                            ), AppConstants.TAG_FAV_LOCATION
                                        )
                                    }
                                }

                                is ContactUsFragment -> {
                                    if (AppConstants.AFTER_CONTACT_US_LOCATION_PERMISSIONS == AppConstants.TAG_APP_SUPPORT) {
                                        presenter.openFragmentRight(
                                            getFragmentAfterContactUsLocationPermissions(
                                                SupportLocationsFragment(),
                                                true
                                            ), AppConstants.TAG_SUPPORT_LOCATIONS
                                        )
                                    } else if (AppConstants.AFTER_CONTACT_US_LOCATION_PERMISSIONS == AppConstants.TAG_PROVIDE_FEEDBACK) {
                                        presenter.openFragmentRight(
                                            getFragmentAfterContactUsLocationPermissions(
                                                SupportLocationsFragment(),
                                                false
                                            ), AppConstants.TAG_SUPPORT_LOCATIONS
                                        )
                                    }
                                }
                                else -> presenter.openFragment(
                                    LocationsFragment(),
                                    AppConstants.TAG_LOCATIONS
                                )
                            }
                        }
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        ) {
                            if (getCurrentFragment() != null) {
                                when (getCurrentFragment()) {

                                    is SignUpFragment -> presenter.openFragmentRight(
                                        FavoriteLocationFragment(),
                                        AppConstants.TAG_FAV_LOCATION
                                    )

                                    is OrderSummaryFragment -> {
                                        presenter.openFragmentRight(
                                            ChangeOrderLocationFragment(),
                                            AppConstants.TAG_ONLINE_ORDER
                                        )
                                    }

                                    is PersonalSettingsFragment -> presenter.openFragmentRight(
                                        FavoriteLocationFragment(),
                                        AppConstants.TAG_ACCOUNT
                                    )

                                    is HomeFragment -> {
                                        if (locationFromMenu) {
                                            clearStackLocations()
                                            openFragment(
                                                LocationsFragment(),
                                                AppConstants.TAG_LOCATIONS
                                            )
                                        } else {
                                            presenter.openFragmentRight(
                                                getFragmentRewards(
                                                    FavoriteLocationFragment(),
                                                    true
                                                ), AppConstants.TAG_FAV_LOCATION
                                            )
                                        }
                                    }

                                    is ContactUsFragment -> {
                                        if (AppConstants.AFTER_CONTACT_US_LOCATION_PERMISSIONS == AppConstants.TAG_APP_SUPPORT) {
                                            presenter.openFragmentRight(
                                                getFragmentAfterContactUsLocationPermissions(
                                                    SupportLocationsFragment(),
                                                    true
                                                ), AppConstants.TAG_SUPPORT_LOCATIONS
                                            )
                                        } else if (AppConstants.AFTER_CONTACT_US_LOCATION_PERMISSIONS == AppConstants.TAG_PROVIDE_FEEDBACK) {
                                            presenter.openFragmentRight(
                                                getFragmentAfterContactUsLocationPermissions(
                                                    SupportLocationsFragment(),
                                                    false
                                                ), AppConstants.TAG_SUPPORT_LOCATIONS
                                            )
                                        }
                                    }
                                    else -> presenter.openFragment(
                                        LocationsFragment(),
                                        AppConstants.TAG_LOCATIONS
                                    )

                                }
                            }
                        } else {
                            if (getCurrentFragment() != null) {
                                when (getCurrentFragment()) {

                                    is PersonalSettingsFragment -> presenter.openFragmentRight(
                                        FavoriteLocationFragment(),
                                        AppConstants.TAG_ACCOUNT
                                    )
                                    is SignUpFragment -> presenter.openFragmentRight(
                                        FavoriteLocationFragment(),
                                        AppConstants.TAG_FAV_LOCATION
                                    )
                                    is OrderSummaryFragment -> {
                                        presenter.openFragmentRight(
                                            ChangeOrderLocationFragment(),
                                            AppConstants.TAG_ONLINE_ORDER
                                        )
                                    }

                                    is HomeFragment -> {
                                        if (locationFromMenu) {
                                            clearStackLocations()
                                            openFragment(
                                                LocationsFragment(),
                                                AppConstants.TAG_LOCATIONS
                                            )
                                        } else {
                                            presenter.openFragmentRight(
                                                getFragmentRewards(
                                                    FavoriteLocationFragment(),
                                                    true
                                                ), AppConstants.TAG_FAV_LOCATION
                                            )
                                        }
                                    }
                                    is ContactUsFragment -> {
                                        if (AppConstants.AFTER_CONTACT_US_LOCATION_PERMISSIONS == AppConstants.TAG_APP_SUPPORT) {
                                            presenter.openFragmentRight(
                                                getFragmentAfterContactUsLocationPermissions(
                                                    SupportLocationsFragment(),
                                                    true
                                                ), AppConstants.TAG_SUPPORT_LOCATIONS
                                            )
                                        } else if (AppConstants.AFTER_CONTACT_US_LOCATION_PERMISSIONS == AppConstants.TAG_PROVIDE_FEEDBACK) {
                                            presenter.openFragmentRight(
                                                getFragmentAfterContactUsLocationPermissions(
                                                    SupportLocationsFragment(),
                                                    false
                                                ), AppConstants.TAG_SUPPORT_LOCATIONS
                                            )
                                        }
                                    }
                                    else -> presenter.openFragment(
                                        LocationsFragment(),
                                        AppConstants.TAG_LOCATIONS
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        // Unregisters the AppboyInAppMessageManager.
        AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(this)
    }

    private fun getFragmentAfterContactUsLocationPermissions(
        fragment: BaseFragment,
        isFromAppSupport: Boolean
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.EXTRA_IS_APP_SUPPORT, isFromAppSupport)
        fragment.arguments = bundle
        return fragment
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            5 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data!!.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                val cursor =
                    contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                cursor!!.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val picturePath = cursor.getString(columnIndex)
                cursor.close()
                // String picturePath contains the path of selected Image
                val uriString = selectedImage.toString()
                val myFileImage: File?
                myFileImage = try {
                    File(getPath(selectedImage)!!)
                } catch (er: Exception) {
                    File(uriString)
                }
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment != null) {
                        if (fragment is PersonalSettingsFragment) {
                            try {
                                if (myFileImage != null) {
                                    fragment.showSelectedImage(picturePath, myFileImage)
                                }
                            } catch (er: IllegalStateException) {
                            }
                        }
                    }
                }
            }

            6 -> if (resultCode == Activity.RESULT_OK) {
                var bitmap: Bitmap?
                val cursor = contentResolver.query(
                    Uri.parse(Engine.mCurrentPhotoPath),
                    Array(1) { MediaStore.Images.ImageColumns.DATA },
                    null, null, null
                )
                cursor?.moveToFirst()
                val photoPath = cursor?.getString(0)
                cursor?.close()
                val f = File(photoPath)
                try {
                    bitmap = BitmapFactory.decodeFile(f.absolutePath)
                    var rotate = 0
                    try {
                        val exif =
                            ExifInterface(f.absolutePath)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                            ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                            ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val matrix = Matrix()
                    matrix.postRotate(rotate.toFloat())
                    bitmap = Bitmap.createBitmap(
                        bitmap!!, 0, 0, bitmap.width,
                        bitmap.height, matrix, true
                    )
                    for (fragment in supportFragmentManager.fragments) {
                        if (fragment != null) {
                            when (fragment) {
                                is PersonalSettingsFragment -> fragment.showPic(bitmap)
                            }
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }
    }

    @SuppressLint("Recycle")
    fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null) ?: return null
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(columnIndex)
        cursor.close()
        return s
    }

    private fun getFragmentRewards(
        fragment: BaseFragment,
        isFromHomePage: Boolean
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, isFromHomePage)
        fragment.arguments = bundle
        return fragment
    }

    override fun onSuccessSubmittedForgotPassword() {
        onBackPressed()
    }

    fun clearOrderHelper() {
        OrderHelper.fromHome = false
        OrderHelper.fromHistory = false
        OrderHelper.fromSummary = false
        OrderHelper.restaurantDetails = null
        OrderHelper.isavailable = null
        OrderHelper.advanceonly = null
        OrderHelper.advanceorderdays = 0
        OrderHelper.vendorId = 0
        OrderHelper.basketId = ""
        OrderHelper.deliveryMode = ""
        OrderHelper.location = null
        OrderHelper.locationAddress = ""
        OrderHelper.showCalories = false
        OrderHelper.basket = null
        OrderHelper.supportsbaskettransfers = false
        OrderHelper.supportsSpecialCaracters = false
        OrderHelper.specialInstrutionsQuantity = 0
        OrderHelper.selectedModifiersIds = mutableListOf()
        OrderHelper.selectedOptionsIds = mutableListOf()
        OrderHelper.carInfo = ""
        OrderHelper.deliveryAddress = ""
        OrderHelper.saveOnFile = false
        OrderHelper.currentAddedAccount = null
        OrderHelper.currentAddedGiftCard = null
        OrderHelper.currentAddedGiftCardBalance = null
        OrderHelper.giftcardBillingSchemes = null
        OrderHelper.pickupTime = null
        OrderHelper.selectedModifiersDedault = mutableListOf()
        OrderHelper.groupQuantityModifiers = HashMap()
        OrderHelper.quantityModifiers = HashMap()
        OrderHelper.checkedRadioGruopWithModifiers = HashMap()
        OrderHelper.checkedCheckBoxWithModifiers = HashMap()
        OrderHelper.currentPrice = 0.0
        OrderHelper.currentCalories = 0
        OrderHelper.restaurantResponse = null
        OrderHelper.fromEdit = false
        OrderHelper.fromEditForEdit = false
        OrderHelper.TMPselectedModifiersIds = mutableListOf()
        OrderHelper.acceptsordersbeforeopening = false
        OrderHelper.acceptsordersuntilclosing = false
        OrderHelper.open_time = ""
        OrderHelper.mCurrentDate = null
        OrderHelper.calendarRange = null
    }
}
