package com.tts.gueststar.fragments.userauth

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.squareup.picasso.Picasso
import javax.inject.Inject
import androidx.core.content.ContextCompat
import app.com.relevantsdk.sdk.models.*
import com.appboy.Appboy
import com.appboy.enums.NotificationSubscriptionType
import com.google.gson.Gson
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.fragments.FavoriteLocationFragment
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.account.PersonalSettingsPresenter
import com.tts.gueststar.utility.*
import kotlinx.android.synthetic.main.camera_option_dialog.view.*
import kotlinx.android.synthetic.main.fragment_personal_settings.*
import java.io.ByteArrayOutputStream
import java.util.*
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat

class PersonalSettingsFragment : BaseFragment(), PersonalSettingsPresenter.PersonalSettingsView,
    OnClickListener {

    private val resultLoadImage = 5
    private val resultLoadImageNew = 6
    private var cal = Calendar.getInstance()
    private var favLocationId: Int = 0
    private lateinit var homeActivity: MainActivity
    lateinit var presenter: PersonalSettingsPresenter
    private lateinit var app: MyApplication
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var dobDay: Int = 0
    private var dobMonth: Int = 0
    private var dobYear: Int = 0
    private var bitmapImage: Bitmap? = null
    private var isInfoChanged = false
    private var isPictureChanged = false
    private var byteArray: ByteArray? = null
    private var picture: String = ""
    var forCamera = false
    private var bday: Long? = null
    private var afterUpdate = false
    private var responseUser: UpdateUserProfileRequest? = null
    private var optInChecked = false
    private var changedLocationName = ""
    private var changedBirthday = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        afterUpdate = false
        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getUserProfile(
                Engine().getAuthToken(mySharedPreferences).toString(),
                homeActivity
            )
        } else {
            Engine().showMsgDialog(
                "",
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
        etPhoneNumber.clearFocus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return inflater.inflate(R.layout.fragment_personal_settings, container, false)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)
        presenter = PersonalSettingsPresenter(this)
        Engine.setNextPage = AppConstants.TAG_PERSONAL_SETTINGS
        favLocationId = Engine().getUserFavLocationId(mySharedPreferences)!!.toInt()

        var phone = ""
        for (index in Engine().getUserMobilePhone(mySharedPreferences)!!.indices) {
            if (index == 2 || index == 5)
                phone += Engine().getUserMobilePhone(mySharedPreferences)?.get(index)!! + "-"
            else
                phone += Engine().getUserMobilePhone(mySharedPreferences)?.get(index)
        }

        setListeners()
        setTextWatcherForAmountEditView(etPhoneNumber)
        txt_change.underline()

        cbOptIn.setOnCheckedChangeListener { _, checked ->
            optInChecked = checked
        }

        if (Engine().isNetworkAvailable(homeActivity)) {
            afterUpdate = false
            if (Engine().getUserProfileImage(mySharedPreferences) != "")
                Picasso.get()
                    .load(Engine().getUserProfileImage(mySharedPreferences))
                    .placeholder(R.drawable.avatar)
                    .into(profile_image)
        }
        txt_first_name.text = "Hi " + Engine().getUserFirstName(mySharedPreferences) + "!"


        presenter.checkRequirements(etPhoneNumber.text.toString(), bday)
        isInfoChanged = false
    }

    private fun checkRequiredFields() {
        presenter.checkRequirements(
            etPhoneNumber.text.toString(),
            bday
        )
    }


    fun onFavLocationPicked(name: String, id: Int) {
        isInfoChanged = true
        txt_favLocation.text = name
        changedLocationName = name
        favLocationId = id
        checkRequiredFields()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_birthday -> {
                Engine().hideSoftKeyboard(homeActivity)
                openDatePicker()

            }
            R.id.txt_favLocation -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().checkAndRequestLocationsPermissions(homeActivity))
                    homeActivity.presenter.openFragmentRight(
                        FavoriteLocationFragment(),
                        AppConstants.TAG_ACCOUNT
                    )
            }

            R.id.txt_change -> {
                Engine().hideSoftKeyboard(homeActivity)
                showMoreDialog()
            }
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.onBackPressed()
            }
            R.id.btn_update_profile -> {
                Engine().hideSoftKeyboard(homeActivity)
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()
                    if (isInfoChanged) {
                        responseUser!!.marketing_info?.marketing = optInChecked
                        //   responseUser!!.marketing_texting = optInChecked
                        when {
                            optInChecked -> {
                                Appboy.getInstance(context).currentUser?.setEmailNotificationSubscriptionType(
                                    NotificationSubscriptionType.OPTED_IN)
                            }
                            else -> {
                                Appboy.getInstance(context).currentUser?.setEmailNotificationSubscriptionType(
                                    NotificationSubscriptionType.UNSUBSCRIBED)
                            }
                        }
                        responseUser!!.phone_number = etPhoneNumber.text.toString().replace("-", "")
                        responseUser!!.favorite_location_id = favLocationId
                        responseUser!!.favorite_menu_items = null
                        if (bday != null) {
                            responseUser!!.birthday = bday
                        } else {
                            responseUser!!.birthday = null
                        }
                        presenter.updateProfile(
                            homeActivity,
                            Engine().getAuthToken(mySharedPreferences).toString(), responseUser!!
                        )

                    } else if (!isInfoChanged && isPictureChanged) {
                        presenter.updateImage(
                            homeActivity,
                            Engine().getAuthToken(mySharedPreferences).toString(),
                            picture, byteArray!!
                        )
                    }
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }

            R.id.btnBirthdayAction -> {
                tvBirthday.text = getString(R.string.hint_birthday_date)
                tvBirthday.setTextColor(
                    ContextCompat.getColor(
                        homeActivity,
                        R.color.edit_text_color_hint
                    )
                )
                bday = null
                dobDay = 0
                dobMonth = 0
                dobYear = 0
                btnBirthdayAction.setOnClickListener(null)
                btnBirthdayAction.setBackgroundResource(R.drawable.arow_down)
                checkRequiredFields()
            }
        }
    }

    private fun openDatePicker() {
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateBirthday()
            }

        val datePickerDialog = DatePickerDialog(
            homeActivity,
            dateSetListener,

            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = cal.timeInMillis
        datePickerDialog.show()
    }


    private fun setListeners() {
        btn_close.setOnClickListener(this)
        btn_update_profile.setOnClickListener(this)
        etPhoneNumber.addTextChangedListener(PhoneNumberTextWatcher())
        etPhoneNumber.filters = arrayOf(PhoneNumberFilter())
        txt_change.setOnClickListener(this)
        btn_birthday.setOnClickListener(this)
        txt_favLocation.setOnClickListener(this)
        btnBirthdayAction.setOnClickListener(this)
    }

    private fun updateBirthday() {

        isInfoChanged = true
        val myFormat = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        tvBirthday!!.text = sdf.format(cal.time)
        changedBirthday = tvBirthday!!.text.toString()
        dobDay = cal.get(Calendar.DAY_OF_MONTH)
        dobMonth = cal.get(Calendar.MONTH)
        dobMonth += 1
        dobYear = cal.get(Calendar.YEAR)
        bday = cal.timeInMillis / 1000L

        tvBirthday.setTextColor(ContextCompat.getColor(homeActivity, R.color.edit_text_color))
        btnBirthdayAction.setBackgroundResource(R.drawable.clear_icon)
        btnBirthdayAction.setOnClickListener(this)
        checkRequiredFields()
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                isInfoChanged = true
                checkRequiredFields()
            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    override fun disableButton() {
        Engine().setEnableButton(btn_update_profile, false)
    }

    override fun enableButton() {
        Engine().setEnableButton(btn_update_profile, true)
    }

    override fun showGenericError() {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(
            getString(R.string.app_name),
            getString(R.string.handle_blank_pop_up),
            homeActivity
        )
    }

    override fun showError(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
    }


    override fun onFailureUnauthorized(error: String) {
        homeActivity.presenter.dismissProgress()
        Engine().showMsgDialog(getString(R.string.app_name), error, homeActivity)
        Engine().clearDataAfterLogOut(mySharedPreferences)
        Engine.setNextPage = AppConstants.TAG_PERSONAL_SETTINGS
        homeActivity.presenter.openFragmentUp(
            getFragment(
                MainSignUpFragment()
            ), AppConstants.TAG_MAIN_SIGN_UP
        )
    }

    private fun getFragment(fragment: BaseFragment): BaseFragment {
        val bundle = Bundle()
        bundle.putBoolean(AppConstants.FROM_NAVIGATION, false)
        fragment.arguments = bundle
        return fragment
    }

    @SuppressLint("SimpleDateFormat")
    override fun successGetUserProfile(response: UserProfileResponse?) {
        txt_email.text = Engine().getUserEmail(mySharedPreferences)
        if (changedLocationName.isEmpty()) {
            if (Engine().getUserFavLocationName(mySharedPreferences)!!.isNotEmpty())
                txt_favLocation.text = Engine().getUserFavLocationName(mySharedPreferences)
        } else if (changedLocationName.isNotEmpty())
            txt_favLocation.text = changedLocationName


        cbOptIn.isChecked = response!!.user_data.marketing_info.marketing
        when {
            response.user_data.marketing_info.marketing -> {
                Appboy.getInstance(context).currentUser?.setEmailNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN)
            }
            else -> {
                Appboy.getInstance(context).currentUser?.setEmailNotificationSubscriptionType(NotificationSubscriptionType.UNSUBSCRIBED)
            }
        }
        var phone = ""
        for (index in response.user_data.phone_number.indices) {
            if (index == 2 || index == 5)
                phone += response.user_data.phone_number[index] + "-"
            else
                phone += response.user_data.phone_number[index]
        }
        etPhoneNumber.setText(phone)

        if (changedBirthday.isEmpty()) {
            try {
                if (response.user_data.birthday != null) {
                    val stamp = Timestamp(response.user_data.birthday!!)
                    val d = Date(stamp.time * 1000)
                    val f = SimpleDateFormat("MM.dd.yyyy")
                    f.timeZone = TimeZone.getTimeZone("GMT")
                    tvBirthday.setTextColor(
                        ContextCompat.getColor(
                            homeActivity,
                            R.color.edit_text_color
                        )
                    )
                    tvBirthday.text = f.format(d).toString()
                    btnBirthdayAction.setBackgroundResource(R.drawable.clear_icon)
                }
            } catch (e: Exception) {

            }
        } else {
            tvBirthday.text = changedBirthday
        }
        val marInfo = MarketingInfo(
            marketing = response.user_data.marketing_info.marketing,
            marketing_texting = response.user_data.marketing_info.marketing_texting
        )

        responseUser = UpdateUserProfileRequest(
            app_usage_purpose = response.user_data.app_usage_purpose,
            birthday = response.user_data.birthday,
            email = response.user_data.email,
            favorite_location_id = response.user_data.favorite_location.id,
            favorite_menu_items = response.user_data.favorite_menu_items,
            first_name = response.user_data.first_name,
            last_name = response.user_data.last_name,
            gender = response.user_data.gender,
            mall_employee = false,
            marketing_info = marInfo,
            phone_number = response.user_data.phone_number,
            retailer = response.user_data.retailer,
            username = response.user_data.username,
            zipcode = response.user_data.zipcode
        )

        val json = Gson().toJson(responseUser)
        MySharedPreferences.putString(
            homeActivity,
            MySharedPreferences.userProfile,
            json
        )

        homeActivity.presenter.dismissProgress()
        var birthday = AppConstants.defaultTimestamp
        if (response.user_data.birthday != null) {
            bday = response.user_data.birthday!!
            birthday = response.user_data.birthday!!
        }


        if (response.user_data.favorite_location.app_display_text != null && response.user_data.favorite_location.id != 0) {
                presenter.checkRequirements(etPhoneNumber.text.toString(), birthday)
                Engine().saveUserProfileData(
                    mySharedPreferences,
                    response.user_data.first_name,
                    response.user_data.last_name,
                    response.user_data.phone_number,
                    response.user_data.favorite_location.app_display_text,
                    response.user_data.favorite_location.id,
                    birthday,
                    response.user_data.profile_picture_url,
                    response.user_data.hashed_user_id,
                    response.user_data.hashed_email
                )
                if (afterUpdate)
                    homeActivity.onBackPressed()
        } else {
            presenter.checkRequirements(etPhoneNumber.text.toString(), birthday)
            Engine().saveUserProfileData(
                mySharedPreferences,
                response.user_data.first_name,
                response.user_data.last_name,
                response.user_data.phone_number,
                "",
                response.user_data.favorite_location.id,
                birthday,
                response.user_data.profile_picture_url,
                response.user_data.hashed_user_id,
                response.user_data.hashed_email
            )
            if (afterUpdate)
                homeActivity.onBackPressed()
        }
    }


    override fun successUpdatePicture(response: UploadUserProfilePictureResponse?) {
        afterUpdate = true
        presenter.getUserProfile(
            Engine().getAuthToken(mySharedPreferences).toString(),
            homeActivity
        )
    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    @SuppressLint("InflateParams")
    private fun showMoreDialog() {
        val bottomSheetLayout =
            requireActivity().layoutInflater.inflate(R.layout.camera_option_dialog, null)

        bottomSheetLayout.existing_photo.setOnClickListener {
            Engine().hideSoftKeyboard(homeActivity)
            if (Engine().verifyStoragePermissions(homeActivity)) {
                val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                requireActivity().startActivityForResult(i, resultLoadImage)
                mBottomSheetDialog!!.dismiss()
            }
        }

        bottomSheetLayout.new_photo.setOnClickListener {
            Engine().hideSoftKeyboard(homeActivity)
            if (Engine().checkAndRequestPermissionsCamera(homeActivity)
            ) {
                afterCameraPermissionAllowed()
            }
        }

        mBottomSheetDialog =
            BottomSheetDialog(requireActivity())
        mBottomSheetDialog!!.setContentView(bottomSheetLayout)
        mBottomSheetDialog!!.show()
    }

    fun afterCameraPermissionAllowed() {
        forCamera = true
        if (Engine().verifyStoragePermissions(
                homeActivity
            )
        ) {
            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            val fileUri = homeActivity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(homeActivity.packageManager) != null) {
                Engine.mCurrentPhotoPath = fileUri!!.toString()
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                requireActivity().startActivityForResult(intent, resultLoadImageNew)
            }
            mBottomSheetDialog!!.dismiss()
        }
    }

    fun afterPermissionAllowed() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        requireActivity().startActivityForResult(i, resultLoadImage)
        mBottomSheetDialog!!.dismiss()
    }

    fun showSelectedImage(imagePath: String, image: File) = try {
        var bitmap = BitmapFactory.decodeFile(imagePath)
        picture = imagePath
        //   val valueInPixels = resources.getDimension(R.dimen.dimen_150)
        //   bitmap = Bitmap.createScaledBitmap(bitmap, valueInPixels.toInt(), valueInPixels.toInt(), true)
        var rotate = 0
        try {
            val exif =
                ExifInterface(image.absolutePath)
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
            bitmap, 0, 0, bitmap.width,
            bitmap.height, matrix, true
        )
        etPhoneNumber.clearFocus()
        isPictureChanged = true
        bitmapImage = bitmap
        profile_image.setImageBitmap(bitmap)
        val stream: ByteArrayOutputStream? = ByteArrayOutputStream()
        bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        byteArray = stream?.toByteArray()
        //  bitmapImage?.recycle()
        checkRequiredFields()

    } catch (e: Exception) {
        e.printStackTrace()
    }

    fun showPic(bitmap: Bitmap) {
        etPhoneNumber.clearFocus()
        profile_image.setImageBitmap(bitmap)
        try {
            bitmapImage = bitmap
            val stream: ByteArrayOutputStream? = ByteArrayOutputStream()
            bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            byteArray = stream?.toByteArray()
            //  bitmapImage?.recycle()
            isPictureChanged = true
        } catch (e: Exception) {
        }
        checkRequiredFields()
    }

    override fun onSuccessUpdateProfile(response: UpdateUserProfileResponse?) {
        Engine().showMsgDialog(getString(R.string.app_name), response!!.message, homeActivity)
        if (isPictureChanged) {
            presenter.updateImage(
                homeActivity,
                Engine().getAuthToken(mySharedPreferences).toString(),
                picture, byteArray!!
            )
        } else {
            afterUpdate = true
            presenter.getUserProfile(
                Engine().getAuthToken(mySharedPreferences).toString(),
                homeActivity
            )
        }
    }
}
