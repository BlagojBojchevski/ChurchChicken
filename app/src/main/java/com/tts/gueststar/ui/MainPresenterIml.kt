package com.tts.gueststar.ui


interface MainPresenterIml {
    fun openFragment(fragment: BaseFragment, tag: String)
    fun openFragmentUp(fragment: BaseFragment, tag: String)
    fun openFragmentRight(fragment: BaseFragment, tag: String)
    fun openFragmentRightNew(fragment: BaseFragment, tag: String)
    fun openFragmentRightDown(fragment: BaseFragment, tag: String)
    fun setUpView()
    fun showProgress()
    fun dismissProgress()
}