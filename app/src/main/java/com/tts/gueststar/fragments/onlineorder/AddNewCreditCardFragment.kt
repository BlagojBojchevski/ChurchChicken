package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.databinding.FragmentAddNewCreditCardBinding
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.utility.CardType
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.Account
import kotlinx.android.synthetic.main.fragment_add_new_credit_card.*
import java.util.*
import javax.inject.Inject


@Suppress("DEPRECATION")
class AddNewCreditCardFragment : BaseFragment(), View.OnClickListener {

    private lateinit var homeActivity: MainActivity

    private var _binding: FragmentAddNewCreditCardBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    private var selectedPaymentType = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewCreditCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        Engine().setEnableButton(btn_save_credit_card, false)
        binding.btnClose.setOnClickListener(this)
        binding.btnSaveCreditCard.setOnClickListener(this)
        binding.btnExpirationDateUpdate.setOnClickListener(this)

        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        initializeAddCreditCard()
//        view.viewTreeObserver.addOnGlobalLayoutListener {
//            val r = Rect()
//            //r will be populated with the coordinates of your view that area still visible.
//            view.getWindowVisibleDisplayFrame(r)
//            val heightDiff = view.rootView.height - (r.bottom - r.top)
//            if (heightDiff > 460) {
//                try {
//                    binding.saveCreditCard.visibility = View.GONE
//                } catch (er: IllegalStateException) {
//                }
//            } else {
//                try {
//                    binding.saveCreditCard.visibility = View.VISIBLE
//                } catch (er: IllegalStateException) {
//                }
//            }
//        }

        if (!OrderHelper.saveCreditOnFile) {
            binding.layoutSaveOnFile.visibility = View.INVISIBLE
            binding.cbSaveCreditCard.isClickable = false
            binding.cbSaveCreditCard.isEnabled = false
        }
    }

    override fun onPause() {
        super.onPause()
        homeActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)

        binding.btnClose.setImageResource(R.drawable.arrow_white_left)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                Engine().hideSoftKeyboard(homeActivity)
                homeActivity.clearStackAddCreditCard()
            }
            R.id.btn_expiration_date_update -> {
                initDatePicker()
            }
            R.id.btn_save_credit_card -> {
                Engine().hideSoftKeyboard(homeActivity)
                var date = binding.expirationDateUpdate.text.toString()
                date = date.replace("/", "-")
                val creditNumber = binding.etCreditCardUpdate.text.toString()
                val cardSuffix = creditNumber.substring(creditNumber.length - 4)

                OrderHelper.saveOnFile = cbSaveCreditCard.isChecked
                OrderHelper.currentAddedAccount = Account(
                    null,
                    getString(R.string.type_credit_card),
                    null,
                    cardSuffix,
                    creditNumber,
                    "",
                    selectedPaymentType,
                    binding.etCvv.text.toString().toInt(),
                    binding.etSignUpZip.text.toString().toInt(),
                    null,
                    date
                )
                homeActivity.clearStackAddCreditCard()
            }
        }
    }

    private fun initDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 2

        val dialog = Dialog(homeActivity)

        dialog.setTitle(getString(R.string.date_picker_title))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_date_picker)

        val monthPicker = dialog.findViewById(R.id.monthPicker) as NumberPicker
        val yearPicker = dialog.findViewById(R.id.yearPicker) as NumberPicker

        val btnCancel = dialog.findViewById(R.id.btnCancel) as Button
        val btnSet = dialog.findViewById(R.id.btnSetExpDate) as Button

        monthPicker.minValue = 1
        monthPicker.maxValue = 12

        if (month > 12)
            monthPicker.value = 12
        else
            monthPicker.value = month

        yearPicker.minValue = year
        yearPicker.maxValue = year + 50
        yearPicker.wrapSelectorWheel = false
        monthPicker.setOnValueChangedListener { _, _, _ ->
            if (yearPicker.value == year) {
                if (monthPicker.value < month) {
                    if (month <= 12) {
                        monthPicker.value = month
                    } else {
                        monthPicker.value = 12
                    }
                }
            }
        }

        yearPicker.setOnValueChangedListener { _, _, _ ->
            if (yearPicker.value == year) {
                if (monthPicker.value < month) {
                    if (month <= 12) {
                        monthPicker.value = month
                    } else {
                        monthPicker.value = 12
                    }
                }
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSet.setOnClickListener {
            val chosenMonth = monthPicker.value
            val chosenYear = yearPicker.value
            if (chosenMonth < 10)
                expiration_date_update.text =
                    getString(
                        R.string.exp_date_field_value,
                        chosenMonth.toString(),
                        chosenYear.toString()
                    )
            else expiration_date_update.text =
                getString(
                    R.string.exp_date_field_value_nonzero,
                    chosenMonth.toString(),
                    chosenYear.toString()
                )

            checkRequirements()
            binding.iconArrow.visibility = View.GONE
            binding.iconClearExpiration.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT < 23) {
                binding.expirationDateUpdate.setTextAppearance(homeActivity, R.style.TextViewDark)
            } else {
                binding.expirationDateUpdate.setTextAppearance(R.style.TextViewDark)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkRequirements()
            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    private fun initializeAddCreditCard() {
        setTextWatcherForAmountEditView(binding.etCreditCardUpdate)
        setTextWatcherForAmountEditView(binding.etSignUpZip)
        setTextWatcherForAmountEditView(binding.etCvv)
        checkRequirements()
        binding.etCreditCardUpdate.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {


                    val userInput = s.toString().replace("[^\\d]".toRegex(), "")
                    if (userInput.length <= 16) {
                        val sb = StringBuilder()
                        for (i in userInput.indices) {
                            if (i % 4 == 0 && i > 0) {
                                sb.append("-")
                            }
                            sb.append(userInput[i])
                        }
                        current = sb.toString()

                        s?.filters = arrayOfNulls<InputFilter>(0)
                    }
                    s?.replace(0, s.length, current, 0, current.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count == 0) {
                    binding.cardIcon1.visibility = View.VISIBLE
                    binding.cardIcon1.setImageResource(R.drawable.no_card)
                }
                when {

                    CardType.detect(
                        s.toString().replace(
                            "-",
                            ""
                        )
                    ) == CardType.AMERICAN_EXPRESS -> {
                        binding.cardIcon1.visibility = View.VISIBLE
                        binding.cardIcon1.setImageResource(R.drawable.card_amex)
                        selectedPaymentType = 0
                    }
                    CardType.detect(
                        s.toString().replace(
                            "-",
                            ""
                        )
                    ) == CardType.DISCOVER -> {
                        binding.cardIcon1.visibility = View.VISIBLE
                        binding.cardIcon1.setImageResource(R.drawable.card_doscover)
                        selectedPaymentType = 3
                    }
                    CardType.detect(
                        s.toString().replace(
                            "-",
                            ""
                        )
                    ) == CardType.MASTERCARD -> {
                        binding.cardIcon1.visibility = View.VISIBLE
                        binding.cardIcon1.setImageResource(R.drawable.card_mastercard)
                        selectedPaymentType = 6
                    }
                    CardType.detect(
                        s.toString().replace(
                            "-",
                            ""
                        )
                    ) == CardType.VISA -> {
                        binding.cardIcon1.visibility = View.VISIBLE
                        binding.cardIcon1.setImageResource(R.drawable.card_visa)
                        selectedPaymentType = 7
                    }
                }
            }
        })
    }

    private fun checkRequirements() {
        if ((binding.etCreditCardUpdate.text?.length == 18 || binding.etCreditCardUpdate.text?.length == 19) && binding.expirationDateUpdate.text.length == 7 && binding.expirationDateUpdate.text != getString(
                R.string.epmty_expire_date
            )
            && binding.etSignUpZip.text?.length == 5
        ) {
            Engine().setEnableButton(binding.btnSaveCreditCard, true)
        } else {
            Engine().setEnableButton(binding.btnSaveCreditCard, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
