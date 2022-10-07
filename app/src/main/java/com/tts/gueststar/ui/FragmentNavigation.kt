package com.tts.gueststar.ui

interface FragmentNavigation {
    interface View {
        fun attachPresenter(presenter: Presenter)
    }

    interface Presenter {
        fun addFragment(fragment: BaseFragment)
    }
}