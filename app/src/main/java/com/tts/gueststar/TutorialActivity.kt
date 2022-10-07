package com.tts.gueststar

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.tts.gueststar.adapters.TutorialFragmentAdapter
import com.tts.gueststar.interfaces.TutorialPageInterface
import kotlinx.android.synthetic.main.activity_tutorial.*


class TutorialActivity : AppCompatActivity(), TutorialPageInterface {

    private var pageCount: Int = 0
    private var fromAccount = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        txt_skip.underline()

        if (intent != null) {
            fromAccount = intent.getStringExtra("fromAccount") ?: ""
        }

        pageCount = 3
        for (i in 0 until pageCount) {
            val view = View(this)
            val scale = baseContext.resources.displayMetrics.density
            if (i == 0) {
                val height = (20 * scale + 0.5f).toInt() / 2
                val params = LinearLayout.LayoutParams(height, height)
                params.gravity = Gravity.CENTER_VERTICAL
                params.rightMargin = height
                view.layoutParams = params
                view.setBackgroundResource(R.drawable.selected_dot)
            } else {
                val height = (20 * scale + 0.5f).toInt() / 2
                val params = LinearLayout.LayoutParams(height, height)
                params.gravity = Gravity.CENTER_VERTICAL
                params.rightMargin = height
                view.layoutParams = params
                view.setBackgroundResource(R.drawable.unselected_dot)
            }
            pagin.addView(view)
        }

        viewpager.adapter =
            TutorialFragmentAdapter(supportFragmentManager, pageCount)
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(p0: Int) {
                setPagin(p0)
            }
        })

        txt_skip.setOnClickListener { (this as TutorialPageInterface).onFinishTutorial() }

    }

    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun finishTutorial() {
        if (fromAccount.isEmpty()) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
        this.finish()
        overridePendingTransition(R.anim.hold, R.anim.slide_in_down)
    }


    private fun setPagin(position: Int) {
        if (position == 2) {
            pagin.visibility = View.INVISIBLE
        } else {
            pagin.visibility = View.VISIBLE
        }
        val scale = baseContext.resources.displayMetrics.density
        for (i in 0 until pageCount) {
            if (position == i) {
                val height = (20 * scale + 0.5f).toInt() / 2
                val params = LinearLayout.LayoutParams(height, height)
                params.gravity = Gravity.CENTER_VERTICAL
                params.rightMargin = height
                pagin.getChildAt(i).layoutParams = params
                pagin.getChildAt(i).setBackgroundResource(R.drawable.selected_dot)
            } else {
                val height1 = (20 * scale + 0.5f).toInt() / 2
                val params = LinearLayout.LayoutParams(height1, height1)
                params.gravity = Gravity.CENTER_VERTICAL
                params.rightMargin = height1
                pagin.getChildAt(i).layoutParams = params
                pagin.getChildAt(i).setBackgroundResource(R.drawable.unselected_dot)
            }
        }
    }

    override fun onFinishTutorial() {
        finishTutorial()
    }

    override fun onBackPressed() {
        finishTutorial()
    }

}