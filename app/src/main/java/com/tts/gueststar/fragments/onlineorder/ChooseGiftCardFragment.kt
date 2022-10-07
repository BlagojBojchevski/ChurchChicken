package com.tts.gueststar.fragments.onlineorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.adapters.GiftCardsAdapter
import com.tts.gueststar.interfaces.CreditCardInterface
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.ui.onlineorder.GetBillingAccountsPresenter
import com.tts.gueststar.utility.AppConstants
import com.tts.gueststar.utility.Engine
import com.tts.gueststar.utility.OrderHelper
import com.tts.olosdk.models.Billingaccount
import com.tts.olosdk.models.Billingfield
import com.tts.olosdk.models.OLOUserBillingAccountsResponse
import kotlinx.android.synthetic.main.fragment_list_gift_card.*
import javax.inject.Inject

class ChooseGiftCardFragment : BaseFragment(), View.OnClickListener,
    GetBillingAccountsPresenter.BillingAccountsView {
    override fun onSuccessDelteCreditcard() {
        presenter.getUserBillingAccounts(
            homeActivity, Engine().getOloAuthToken(mySharedPreferences)!!,
            OrderHelper.basketId!!
        )
    }

    override fun onSuccessGetUserBillingAccounts(response: OLOUserBillingAccountsResponse) {
        homeActivity.presenter.dismissProgress()


        billingaccounts.clear()
        for (item in response.billingaccounts) {
            if (item.accounttype !=  getString(R.string.type_credit_card) && item.accounttype != getString(R.string.type_payinstore)) {
                billingaccounts.add(item)
            }
        }

            if (billingaccounts.isNotEmpty()) {
                saved_gift_cards.layoutManager =
                    LinearLayoutManager(
                        homeActivity.applicationContext,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                adapter = GiftCardsAdapter(
                    homeActivity,
                    billingaccounts,
                    object : CreditCardInterface {
                        override fun removeCreditCard(item: Billingaccount) {
                            if (Engine().isNetworkAvailable(homeActivity)) {
                                homeActivity.presenter.showProgress()
                                presenter.deleteUserBillingAccount(
                                    homeActivity, Engine().getOloAuthToken(mySharedPreferences)!!,
                                    item.accountid
                                )
                            } else {
                                Engine().showMsgDialog(
                                    "",
                                    getString(R.string.error_network_connection),
                                    homeActivity
                                )
                            }
                        }

                        override fun chooseCreditCard(item: Billingaccount) {
                            OrderHelper.currentAddedGiftCardBalance =
                                item.balance.balance
                            val giftCard = Billingfield(value = item.cardsuffix)
                            OrderHelper.currentAddedGiftCard = giftCard
                           OrderHelper.giftCardAccountId = item.accountid

                            homeActivity.clearStackAddCreditCard()
                        }
                    }
                )
                saved_gift_cards.adapter = adapter
            } else {
                homeActivity.presenter.openFragmentRight(
                    AddNewGiftCardFragment(),
                    AppConstants.TAG_ADD_CREDIT_CARD
                )
            }
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

    private var billingaccounts: MutableList<Billingaccount> = mutableListOf()
    private lateinit var adapter: GiftCardsAdapter
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication
    lateinit var presenter: GetBillingAccountsPresenter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_gift_card, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

        presenter = GetBillingAccountsPresenter(this)
        btn_close.setOnClickListener(this)
        btn_gift_credit_card.setOnClickListener(this)
    }


    override fun onResume() {
        super.onResume()
        Engine().hideSoftKeyboard(homeActivity)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(false)

        btn_close.setImageResource(R.drawable.arrow_left_black)
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)



        if (Engine().isNetworkAvailable(homeActivity)) {
            homeActivity.presenter.showProgress()
            presenter.getUserBillingAccounts(
                homeActivity, Engine().getOloAuthToken(mySharedPreferences)!!,
                OrderHelper.basketId!!
            )
        } else {
            Engine().showMsgDialog(
                "",
                getString(R.string.error_network_connection),
                homeActivity
            )
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }

            R.id.btn_gift_credit_card ->{
                homeActivity.presenter.openFragmentRightNew(
                    AddNewGiftCardFragment(),
                    AppConstants.TAG_ADD_CREDIT_CARD
                )
            }
        }
    }
}
