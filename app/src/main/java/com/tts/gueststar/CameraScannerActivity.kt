package com.tts.gueststar

import android.app.Activity
import android.os.Bundle
import com.google.zxing.Result
import com.tts.gueststar.utility.AppConstants
import kotlinx.android.synthetic.main.camera_scanner_layout.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class CameraScannerActivity : Activity(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_scanner_layout)
        mScannerView = cameraScanner
        mScannerView.setAutoFocus(true)
        btnCameraCancel.setOnClickListener { finish() }

    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }


    override fun handleResult(result: Result) {
        finish()
        AppConstants.isFromCameraScanner = true
        AppConstants.scannedCode = result.text
    }
}