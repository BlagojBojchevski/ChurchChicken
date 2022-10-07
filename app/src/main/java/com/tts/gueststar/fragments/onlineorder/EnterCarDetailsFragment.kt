package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.databinding.FragmentAddNewCreditCardBinding
import com.tts.gueststar.databinding.FragmentEnterCarDetailsBinding
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.CarDetailsPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.OLOBasketResponse
import kotlinx.android.synthetic.main.fragment_enter_car_details.*
import javax.inject.Inject

class EnterCarDetailsFragment : BaseFragment(), View.OnClickListener,
    CarDetailsPresenter.CarDetailsView {

    private var _binding: FragmentEnterCarDetailsBinding? = null
    private val binding get() = _binding!!

    private var externalPartnerId = ""
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: CarDetailsPresenter
    private var numberFieldsSend = 0
    private var isFromHome: Boolean? = null
    private var isFromSummary: Boolean = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            externalPartnerId = requireArguments().getString(AppConstants.EXTERNAL_ID)!!
            isFromHome = requireArguments().getBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT)
            isFromSummary = requireArguments().getBoolean(AppConstants.EXTRA_IS_FROM_SUMMARY)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEnterCarDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        presenter = CarDetailsPresenter(this)

        setTextWatcherForAmountEditView(binding.etMake)
        setTextWatcherForAmountEditView(binding.etModel)
        setTextWatcherForAmountEditView(binding.etColor)

        Engine().setEnableButton(btn_proceed_to_order, false)
        binding.btnProceedToOrder.setOnClickListener(this)
        binding.btnClose.setOnClickListener(this)

//        view.viewTreeObserver.addOnGlobalLayoutListener {
//            val r = Rect()
//            view.getWindowVisibleDisplayFrame(r)
//            val heightDiff = view.rootView.height - (r.bottom - r.top)
//            if (heightDiff > 500) {
//                try {
//                    binding.btnProceedToOrder.visibility = View.GONE
//                } catch (er: IllegalStateException) {
//                }
//            } else {
//                try {
//                    binding.btnProceedToOrder.visibility = View.VISIBLE
//                } catch (er: IllegalStateException) {
//                }
//            }
//        }
    }

    private fun setTextWatcherForAmountEditView(amountEditText: EditText) {
        val fieldValidatorTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                presenter.checkRequirements(
                    binding.etMake.text.toString(),
                    binding.etModel.text.toString(),
                    binding.etColor.text.toString()
                )
            }

        }
        amountEditText.addTextChangedListener(fieldValidatorTextWatcher)
    }

    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)

        if (isFromHome!!) {
            binding.btnClose.setImageResource(R.drawable.arrow_white_left)
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        } else {
            binding.btnClose.setImageResource(R.drawable.close_icon)
            (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        }

        if (OrderHelper.carInfo.isNotEmpty()) {
            binding.etMake.setText(OrderHelper.carInfo.split(";")[0])
            binding.etModel.setText(OrderHelper.carInfo.split(";")[1])
            binding.etColor.setText(
                OrderHelper.carInfo.split(
                    ";"
                )[2]
            )

        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }

            R.id.btn_proceed_to_order -> {
                if (Engine().isNetworkAvailable(homeActivity)) {
                    homeActivity.presenter.showProgress()

                    OrderHelper.carInfo =
                        binding.etMake.text.toString() + ";" +  binding.etModel.text.toString() + ";" +  binding.etColor.text.toString()
                    presenter.addCustomFieldsToBasket(
                        homeActivity,
                        OrderHelper.filed_make,
//                        55624,
                        binding.etMake.text.toString(),
                        OrderHelper.basketId.toString()
                    )
                    presenter.addCustomFieldsToBasket(
                        homeActivity,
                        OrderHelper.filed_model,
//                        55625,
                        binding.etModel.text.toString(),
                        OrderHelper.basketId.toString()
                    )
                    presenter.addCustomFieldsToBasket(
                        homeActivity,
                        OrderHelper.filed_color,
//                        55626,
                        binding.etColor.text.toString(),
                        OrderHelper.basketId.toString()
                    )
                } else {
                    Engine().showMsgDialog(
                        getString(R.string.app_name),
                        getString(R.string.error_network_connection),
                        homeActivity
                    )
                }
            }
        }
    }


    override fun disableButton() {
        Engine().setEnableButton(btn_proceed_to_order, false)
    }

    override fun enableButton() {
        Engine().setEnableButton(btn_proceed_to_order, true)
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

    override fun onSuccessAddFields(response: OLOBasketResponse) {
        numberFieldsSend++
        if (numberFieldsSend == 3) {
            homeActivity.presenter.dismissProgress()
            if (!isFromSummary) {
                homeActivity.presenter.openFragmentRight(
                    getFragmentMenu(
                        OrderMenuFragment(), externalPartnerId
                    ),
                    AppConstants.TAG_ONLINE_ORDER
                )
            } else {
                homeActivity.onBackPressed()
            }
        }
    }

    private fun getFragmentMenu(
        fragment: BaseFragment,
        external_partner_id: String
    ): BaseFragment {
        val bundle = Bundle()
        bundle.putString(AppConstants.EXTERNAL_ID, external_partner_id)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_HOME_FRAGMENT, isFromHome!!)
        bundle.putBoolean(AppConstants.EXTRA_IS_FROM_CURBSIDE_DELIVERY, true)
        fragment.arguments = bundle
        return fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
