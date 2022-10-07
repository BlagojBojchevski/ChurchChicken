package com.tts.gueststar.ui

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment(), FragmentNavigation.View {

    private var navigationPresenter: FragmentNavigation.Presenter? = null
    override fun attachPresenter(presenter: FragmentNavigation.Presenter) {
        navigationPresenter = presenter
    }
}