package com.tts.gueststar.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tts.gueststar.MainActivity
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.utility.AppConstants
import kotlinx.android.synthetic.main.fragment_web_view.*


class WebViewFragment : BaseFragment(), View.OnClickListener {

    private var url: String = ""
    private var fragmentTitle: String? = ""
    private lateinit var homeActivity: MainActivity


    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null){
            url = requireArguments().getString(AppConstants.WEB_VIEW_URL) ?: ""
            fragmentTitle = requireArguments().getString(AppConstants.WEB_VIEW_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val settings = webView.settings
        settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                homeActivity.presenter.showProgress()
            }

            override fun onPageFinished(view: WebView, url: String) {
                homeActivity.presenter.dismissProgress()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                homeActivity.presenter.dismissProgress()
            }
        }

        btn_close.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        webView.loadUrl(url)
        webViewTitle.text = fragmentTitle
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btn_close -> {
                homeActivity.onBackPressed()
            }
        }
    }

}
