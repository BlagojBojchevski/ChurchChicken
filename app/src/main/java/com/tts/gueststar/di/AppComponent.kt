package com.tts.gueststar.di

import com.tts.gueststar.MainActivity
import com.tts.gueststar.fragments.*
import com.tts.gueststar.fragments.contact.SupportLocationsFragment
import com.tts.gueststar.fragments.contact.SurveyFragment
import com.tts.gueststar.fragments.managepayment.ManagePaymentFirst
import com.tts.gueststar.fragments.managepayment.ManagePaymentFragment
import com.tts.gueststar.fragments.notificationcenter.NotificationCenterFragment
import com.tts.gueststar.fragments.notificationcenter.SingleNotificationFragment
import com.tts.gueststar.fragments.onlineorder.*
import com.tts.gueststar.fragments.rewards.RewardsFragment
import com.tts.gueststar.fragments.userauth.UpdatePasswordFragment
import com.tts.gueststar.fragments.userauth.*

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainSignUpFragment: MainSignUpFragment)
    fun inject(accountFragment: AccountFragment)
    fun inject(logInFragment: LogInFragment)
    fun inject(signUpFragment: SignUpFragment)
    fun inject(forgotPasswordFragment: ForgotPasswordFragment)
    fun inject(updatePasswordFragment: UpdatePasswordFragment)
    fun inject(locationFragment: LocationsFragment)
    fun inject(personalSettingsFragment: PersonalSettingsFragment)
    fun inject(homeFragment: HomeFragment)
    fun inject(rewardsFragment: RewardsFragment)
    fun inject(faqFragment: FaqFragment)
    fun inject(referFriendFragment: ReferFriendFragment)
    fun inject(promoCodeFragment: PromoCodeFragment)
    fun inject(activityHistoryFragment: ActivityHistoryFragment)
    fun inject(supportLocationsFragment: SupportLocationsFragment)
    fun inject(surveyFragment: SurveyFragment)
    fun inject(managePaymentFragment: ManagePaymentFragment)
    fun inject(favoriteLocationFragment: FavoriteLocationFragment)
    fun inject(managePaymentFragmentFirst: ManagePaymentFirst)
    fun inject(singleNotificationFragment: SingleNotificationFragment)
    fun inject(notificationCenterFragment: NotificationCenterFragment)
    fun inject(enterCarDeliveryAddressFragment: EnterDeliveryAddressFragment)
    fun inject(deliveryAddresesFragment: DeliveryAddresesFragment)
    fun inject(orderMenuFragment: OrderMenuFragment)
    fun inject(orderSubMenuFragment: OrderSubMenuFragment)
    fun inject(itemDetailsFragment: ItemDetailsFragment)
    fun inject(orderSummaryFragment: OrderSummaryFragment)
    fun inject(changeOrderLocationFragment: ChangeOrderLocationFragment)
    fun inject(checkoutFragment: CheckoutFragment)
    fun inject(addNewCreditCardFragment: AddNewCreditCardFragment)
    fun inject(chooseCreditCardFragment: ChooseCreditCardFragment)
    fun inject(chooseGiftCardFragment: ChooseGiftCardFragment)
    fun inject(addNewGiftCardFragment: AddNewGiftCardFragment)
    fun inject(loyaltyRewadsFragment: LoyaltyRewadsFragment)
    fun inject(selectOrderModeFragment: SelectOrderModeFragment)
    fun inject(enterCarDetailsFragment: EnterCarDetailsFragment)
}
