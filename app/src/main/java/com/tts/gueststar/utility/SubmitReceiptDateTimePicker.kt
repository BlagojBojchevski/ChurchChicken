package com.tts.gueststar.utility

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TimePicker
import android.widget.ViewSwitcher


import com.tts.gueststar.R

import java.util.Calendar
import java.util.Date

class SubmitReceiptDateTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener,
    DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener {


    // DatePicker reference
    private val datePicker: DatePicker
    // TimePicker reference
    private val timePicker: TimePicker
    // ViewSwitcher reference
    private val viewSwitcher: ViewSwitcher
    // Calendar reference
    private val mCalendar: Calendar

    val time: Date
        get() = mCalendar.time

    init {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate myself
        inflater.inflate(R.layout.datetimepicker, this, true)

        // Inflate the date and time picker views
        val datePickerView = inflater.inflate(R.layout.datepicker, null) as LinearLayout
        val timePickerView = inflater.inflate(R.layout.timepicker, null) as LinearLayout


        // Grab the ViewSwitcher so we can attach our picker views to it
        viewSwitcher = this.findViewById<View>(R.id.DateTimePickerVS) as ViewSwitcher
        datePicker = datePickerView.findViewById<View>(R.id.DatePicker) as DatePicker
        timePicker = timePickerView.findViewById<View>(R.id.TimePicker) as TimePicker

        // Disabled manual input
        datePicker.descendantFocusability = DatePicker.FOCUS_BLOCK_DESCENDANTS
        timePicker.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS

        // Handle button clicks
        (findViewById<View>(R.id.SwitchToTime) as Button).setOnClickListener(this) // shows
        // the
        // time
        // picker
        (findViewById<View>(R.id.SwitchToDate) as Button).setOnClickListener(this) // shows
        // the
        // date
        // picker


        // Populate ViewSwitcher
        viewSwitcher.addView(datePickerView, 0)
        viewSwitcher.addView(timePickerView, 1)

        mCalendar = Calendar.getInstance()

        val c = Calendar.getInstance()

        datePicker.maxDate = c.timeInMillis

        datePicker.init(
            mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
            mCalendar.get(Calendar.DAY_OF_MONTH), this
        )
        timePicker.setOnTimeChangedListener(this)


    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.SwitchToDate -> {
                v.isEnabled = false
                findViewById<View>(R.id.SwitchToTime).isEnabled = true
                viewSwitcher.showPrevious()
            }

            R.id.SwitchToTime -> {
                v.isEnabled = false
                findViewById<View>(R.id.SwitchToDate).isEnabled = true
                viewSwitcher.showNext()
            }
        }
    }

    operator fun get(field: Int): Int {
        return mCalendar.get(field)
    }


    override fun onDateChanged(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        mCalendar.set(year, monthOfYear, dayOfMonth)

    }

    override fun onTimeChanged(view: TimePicker, hourOfDay: Int, minute: Int) {
        timePicker.currentMinute = minute
        timePicker.currentHour = hourOfDay
        mCalendar.set(Calendar.MINUTE, minute)
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
    }

    fun reset() {
        val c = Calendar.getInstance()
        datePicker.updateDate(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        timePicker.currentMinute = c.get(Calendar.MINUTE)
        timePicker.currentHour = c.get(Calendar.HOUR)
    }
}
