package com.tts.gueststar.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tts.gueststar.BuildConfig
import com.tts.gueststar.MainActivity
import com.tts.gueststar.MyApplication
import com.tts.gueststar.R
import com.tts.gueststar.interfaces.SetBottomNavigationIcon
import com.tts.gueststar.ui.BaseFragment
import com.tts.gueststar.utility.Engine
import kotlinx.android.synthetic.main.fragment_faq.*
import javax.inject.Inject

class FaqFragment : BaseFragment(), View.OnClickListener {

    private var url: String = BuildConfig.FAQ_URL
    private lateinit var homeActivity: MainActivity
    @Inject
    lateinit var mySharedPreferences: SharedPreferences
    private lateinit var app: MyApplication

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeActivity = activity as MainActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = homeActivity.application as MyApplication
        app.getMyComponent().inject(this)

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

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                homeActivity.presenter.dismissProgress()
            }
        }

        btn_back.setOnClickListener(this)
        btn_contact_us.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        (homeActivity as SetBottomNavigationIcon).hideBottomNavigation(true)
        if (Engine().isNetworkAvailable(homeActivity)) {
            webView.loadUrl(url)
        } else {
            Engine().showMsgDialog(
                getString(R.string.app_name),
                getString(R.string.error_network_connection),
                homeActivity
            )
        }

        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                homeActivity.onBackPressed()
            }

            R.id.btn_contact_us -> {
                val uriText =
                    "mailto:" + getString(R.string.EMAILCONTACT_US) + "?subject=" + getString(R.string.EMAILSUBJECTFAQ) + "&body=" + Engine().getDeviceName(
                        mySharedPreferences
                    )
                val uri = Uri.parse(uriText)
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = uri
                startActivity(Intent.createChooser(emailIntent, "Email"))
            }
        }
    }


}
