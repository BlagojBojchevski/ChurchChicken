/**
 * Copyright 2010 Lukasz Szmit <devmail@szmit.eu>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tts.gueststar.utility;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.ViewSwitcher;
import com.tts.gueststar.R;
import com.tts.olosdk.models.Range;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateTimePicker extends RelativeLayout
        implements View.OnClickListener, OnDateChangedListener, OnTimeChangedListener {
    private static String TAG = "DateTimePicker";

    // DatePicker reference
    private DatePicker datePicker;
    // TimePicker reference
    private TimePicker timePicker;
    // ViewSwitcher reference
    private ViewSwitcher viewSwitcher;
    // Calendar reference
    private Calendar mCalendar;

    private int TIME_PICKER_INTERVAL = 15;
    private boolean mEventIgnored = false;
    // private boolean mEventReset = false;
    private int mMaxFutureDayCount = OrderHelper.INSTANCE.getAdvanceorderdays();

    private Calendar startDateTime;
    private Calendar endDateTime;

    private Calendar lastDatePicker;

    private boolean mFirstSetup = false;

    private int minDay;
    private int minMonth;
    private int minYear;

    private int maxDay;
    private int maxMonth;
    private int maxYear;

    // Constructor start
    public DateTimePicker(Context context) {
        this(context, null);
    }

    public DateTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        System.out.println("[" + TAG + "] onCreate() ********************");

        // Get LayoutInflater instance
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate myself
        inflater.inflate(R.layout.datetimepicker, this, true);

        // Inflate the date and time picker views
        final LinearLayout datePickerView = (LinearLayout) inflater.inflate(R.layout.datepicker, null);
        final LinearLayout timePickerView = (LinearLayout) inflater.inflate(R.layout.timepicker, null);

        TIME_PICKER_INTERVAL = 15;
        Log.d("le6end4", "TIME_PICKER_INTERVAL: " + TIME_PICKER_INTERVAL);
        mMaxFutureDayCount = 7;

        // Grab the ViewSwitcher so we can attach our picker views to it
        viewSwitcher = (ViewSwitcher) this.findViewById(R.id.DateTimePickerVS);
        datePicker = (DatePicker) datePickerView.findViewById(R.id.DatePicker);
        timePicker = (TimePicker) timePickerView.findViewById(R.id.TimePicker);

        // Disabled manual input
        datePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        timePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        // Handle button clicks
        ((Button) findViewById(R.id.SwitchToTime)).setOnClickListener(this); // shows
        // the
        // time
        // picker
        ((Button) findViewById(R.id.SwitchToDate)).setOnClickListener(this); // shows
        // the
        // date
        // picker

        if (mMaxFutureDayCount <= 0) {
            ((Button) findViewById(R.id.SwitchToTime)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.SwitchToDate)).setVisibility(View.GONE);
        }

        // Populate ViewSwitcher
        viewSwitcher.addView(datePickerView, 0);
        viewSwitcher.addView(timePickerView, 1);

        if (mMaxFutureDayCount <= 0)
            viewSwitcher.showNext();
    }
    // Constructor end

    // Called every time the user changes DatePicker values
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // Update the internal Calendar instance
        mCalendar.set(year, monthOfYear, dayOfMonth, mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE));

        if (year > maxYear) {
            updateDate(maxYear, maxMonth, dayOfMonth);
            return;
        }

        if (monthOfYear > maxMonth && year == maxYear) {
            updateDate(maxYear, maxMonth, dayOfMonth);
            return;
        }

        if (dayOfMonth > maxDay && year == maxYear && monthOfYear == maxMonth) {
            updateDate(maxYear, maxMonth, maxDay);
            return;
        }

        if (year < minYear) {
            updateDate(minYear, minMonth, dayOfMonth);
            return;
        }

        if (monthOfYear < minMonth && year == minYear) {
            updateDate(minYear, minMonth, dayOfMonth);
            return;
        }

        if (dayOfMonth < minDay && year == minYear && monthOfYear == minMonth) {
            updateDate(minYear, minMonth, minDay);
            return;
        }

        System.out.println("[" + TAG + "] onDateChange: " + year + ", " + monthOfYear + ", " + dayOfMonth);

        if (lastDatePicker.getTimeInMillis() != mCalendar.getTimeInMillis()) {
            lastDatePicker.setTime(mCalendar.getTime());
            setStartEndTime();
        }
    }

    // Called every time the user changes TimePicker values
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        // Update the internal Calendar instance

        int mHourOfDay = hourOfDay;
        int mMinute = minute;

        if (!mFirstSetup) {
            if (!mEventIgnored) {
                mMinute = mMinute * TIME_PICKER_INTERVAL; //getRoundedMinute(mMinute);
                if (mMinute == 60) {
                    mMinute = 0;
                    try {
                        if (mHourOfDay == 23) {
                            mHourOfDay = 1;
                        } else {
                            mHourOfDay++;
                        }
                    } catch (Exception e) {
                        mHourOfDay = 1;
                    }
                }

                System.out.println("[" + TAG + "] onTimeChange: " + mHourOfDay + ", " + mMinute);

                updateTime(hourOfDay, minute);

                mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH), mHourOfDay, mMinute, mCalendar.getMinimum(Calendar.SECOND));

                System.out.println("[" + TAG + "] ==> mCalendar: " + mCalendar.getTime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(startDateTime.getTime());

                if (mCalendar.before(cal)) {
                    System.out.println(
                            "[" + TAG + "] can't select calendar before start/open time: " + startDateTime.getTime());

                    mHourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                    mMinute = cal.get(Calendar.MINUTE);

                    updateTime(mHourOfDay, mMinute / TIME_PICKER_INTERVAL);

                    mCalendar.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                            mHourOfDay, mMinute, mCalendar.getMinimum(Calendar.SECOND));
                }

                if (mCalendar.after(endDateTime)) {
                    System.out
                            .println("[" + TAG + "] can't select calendar after close time: " + endDateTime.getTime());

                    mHourOfDay = endDateTime.get(Calendar.HOUR_OF_DAY);
                    mMinute = endDateTime.get(Calendar.MINUTE);

                    updateTime(mHourOfDay, mMinute / TIME_PICKER_INTERVAL);

                    mCalendar.set(endDateTime.get(Calendar.YEAR), endDateTime.get(Calendar.MONTH),
                            endDateTime.get(Calendar.DAY_OF_MONTH), mHourOfDay, mMinute, mCalendar.getMinimum(Calendar.SECOND));
                }
            }
        } else {
//			mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
//					mCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute*TIME_PICKER_INTERVAL, mCalendar.getMinimum(Calendar.SECOND));
        }
    }

    // Handle button clicks
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.SwitchToDate:
                v.setEnabled(false);
                findViewById(R.id.SwitchToTime).setEnabled(true);
                viewSwitcher.showPrevious();
                break;

            case R.id.SwitchToTime:
                v.setEnabled(false);
                findViewById(R.id.SwitchToDate).setEnabled(true);
                viewSwitcher.showNext();
                break;
        }
    }

    // Convenience wrapper for internal Calendar instance
    public int get(final int field) {
        return mCalendar.get(field);
    }

    // Reset DatePicker, TimePicker and internal Calendar instance
    public void reset() {
        Calendar c = Calendar.getInstance();
        c.setTime(OrderHelper.INSTANCE.getMCurrentDate());
        // c.set(Calendar.SECOND, 0);
        updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        updateTime(hour, minute / TIME_PICKER_INTERVAL);

        mCalendar.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), hour, minute);
    }

    // Convenience wrapper for internal Calendar instance
    public long getDateTimeMillis() {
        return mCalendar.getTimeInMillis();
    }

    public Date getTime() {
        return mCalendar.getTime();
    }

    // Convenience wrapper for internal TimePicker instance
    public void setIs24HourView(boolean is24HourView) {
        timePicker.setIs24HourView(is24HourView);
    }

    // Convenience wrapper for internal TimePicker instance
    public boolean is24HourView() {
        return timePicker.is24HourView();
    }

    public void setMinDate(Date minDate) {
        System.out.println("[" + TAG + "] setMinDate() ********************");

        Calendar cal = Calendar.getInstance();
        cal.setTime(minDate);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));

        // datePicker.setMinDate(cal.getTimeInMillis());

        minYear = cal.get(Calendar.YEAR);
        minMonth = cal.get(Calendar.MONTH);
        minDay = cal.get(Calendar.DAY_OF_MONTH);

        System.out.println("[" + TAG + "] MinDateTime: " + cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, mMaxFutureDayCount);

        maxYear = cal.get(Calendar.YEAR);
        maxMonth = cal.get(Calendar.MONTH);
        maxDay = cal.get(Calendar.DAY_OF_MONTH);

        System.out.println("[" + TAG + "] MaxDateTime: " + cal.getTime());

        initCalendar();
    }

    // Convenience wrapper for internal DatePicker instance
    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        System.out.println("[" + TAG + "] updateDate");
        datePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    // Convenience wrapper for internal TimePicker instance
    public void updateTime(int currentHour, int currentMinute) {
        System.out.println("[" + TAG + "] updateTime");
        mEventIgnored = true;
        timePicker.setCurrentHour(currentHour);
        timePicker.setCurrentMinute(currentMinute);
        mEventIgnored = false;
    }

    private int getRoundedMinute(int minute) {
        if (minute % TIME_PICKER_INTERVAL != 0) {
            int minuteFloor = minute - (minute % TIME_PICKER_INTERVAL);
            minute = minuteFloor + (minute == minuteFloor + 1 ? TIME_PICKER_INTERVAL : 0);
            // if (minute == 60)
            // minute = 0;
        }
        // minute = minute + TIME_PICKER_INTERVAL;
        return minute;
    }

    private void initCalendar() {
        System.out.println("[" + TAG + "] initCalendar() ********************");

        // Grab a Calendar instance
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.SECOND, 0);

        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();

        lastDatePicker = Calendar.getInstance();

        // if (OrderConstants.mSiteStartDate!=null)
        // startDateTime.setTime(OrderConstants.mSiteStartDate);

        if (mCalendar.before(startDateTime)) {
            try {
                mCalendar.setTime(startDateTime.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            } // all done
        }

        // Init date picker
        System.out.println("[" + TAG + "] Init date picker");
        datePicker.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH), this);

        // Init time picker
        System.out.println("[" + TAG + "] Init time picker");
        timePicker.setOnTimeChangedListener(this);

        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        minute = getRoundedMinute(minute) + TIME_PICKER_INTERVAL;

        if (minute == 60) {
            minute = 0;
            try {
                if (hour == 23) {
                    hour = 1;
                } else {
                    hour++;
                }
            } catch (Exception e) {
                hour = 1;
            }
        }

        mFirstSetup = true;
        updateTime(hour, minute / TIME_PICKER_INTERVAL);
        mFirstSetup = false;

        OrderHelper.INSTANCE.setMCurrentDate(mCalendar.getTime());

        setStartEndTime();
    }

    private void setStartEndTime() {
        System.out.println("[" + TAG + "] setStartEndTime() ********************");

//        startDateTime.setTime(mCalendar.getTime());
//        endDateTime.setTime(mCalendar.getTime());

        if (OrderHelper.INSTANCE.getAcceptsordersbeforeopening() && OrderHelper.INSTANCE.getAcceptsordersuntilclosing()) {
            if (OrderHelper.INSTANCE.getDeliveryMode().equals("pickup")) {
                for (int i = 0; i < OrderHelper.INSTANCE.getCalendarRange().getCalendar().size(); i++) {
                    if (OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getType().equals("business")) {
                        outloop:
                        for (Range itemRange : OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getRanges()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                            String tmpDate = sdf1.format(mCalendar.getTime());
                            if (itemRange.getStart().contains(tmpDate)) {
                                try {
                                    startDateTime.setTime(sdf.parse(itemRange.getStart() + ":00"));
                                    endDateTime.setTime(sdf.parse(itemRange.getEnd() + ":00"));

                                    startDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL);
                                    startDateTime.add(Calendar.MILLISECOND, 1000);
                                    endDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL * -1);
                                    endDateTime.add(Calendar.MILLISECOND, 1000);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break outloop;
                            }
                        }
                    }
                }


            } else if (OrderHelper.INSTANCE.getDeliveryMode().equals("curbside")) {
                for (int i = 0; i < OrderHelper.INSTANCE.getCalendarRange().getCalendar().size(); i++) {
                    if (OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getType().equals("curbsidepickup")) {
                        outloop:
                        for (Range itemRange : OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getRanges()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                            String tmpDate = sdf1.format(mCalendar.getTime());
                            if (itemRange.getStart().contains(tmpDate)) {
                                try {
                                    startDateTime.setTime(sdf.parse(itemRange.getStart() + ":00"));
                                    endDateTime.setTime(sdf.parse(itemRange.getEnd() + ":00"));

                                    startDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL);
                                    startDateTime.add(Calendar.MILLISECOND, 1000);
                                    endDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL * -1);
                                    endDateTime.add(Calendar.MILLISECOND, 1000);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break outloop;
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < OrderHelper.INSTANCE.getCalendarRange().getCalendar().size(); i++) {
                    if (OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getType().equals("dispatch")) {
                        outloop:
                        for (Range itemRange : OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getRanges()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                            String tmpDate = sdf1.format(mCalendar.getTime());
                            if (itemRange.getStart().contains(tmpDate)) {
                                try {
                                    startDateTime.setTime(sdf.parse(itemRange.getStart() + ":00"));
                                    endDateTime.setTime(sdf.parse(itemRange.getEnd() + ":00"));

                                    startDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL);
                                    startDateTime.add(Calendar.MILLISECOND, 1000);
                                    endDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL * -1);
                                    endDateTime.add(Calendar.MILLISECOND, 1000);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break outloop;
                            }
                        }
                    }
                }
            }


        } else {
            if (OrderHelper.INSTANCE.getDeliveryMode().equals("pickup")) {
                for (int i = 0; i < OrderHelper.INSTANCE.getCalendarRange().getCalendar().size(); i++) {
                    if (OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getType().equals("business")) {
                        outloop:
                        for (Range itemRange : OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getRanges()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                            String tmpDate = sdf1.format(mCalendar.getTime());
                            if (itemRange.getStart().contains(tmpDate)) {
                                try {
                                    System.out.println("[" + "BR CURRENT" + "] start: " + OrderHelper.INSTANCE.getMCurrentDate());
                                    System.out.println("[" + "BR MCALENDAR" + "] start: " + mCalendar.getTime());
                                    if (OrderHelper.INSTANCE.getMCurrentDate().getDay() == mCalendar.getTime().getDay()) {
                                        try {
                                            startDateTime.setTime(sdf.parse(OrderHelper.INSTANCE.getBasket().getEarliestreadytime() + ":00"));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        System.out.println("[" + "ACECAR-istiden" + "] start: " + startDateTime.getTime());
                                    } else {
                                        startDateTime.setTime(sdf.parse(itemRange.getStart() + ":00"));
                                        System.out.println("[" + "ACECAR-razlicen den" + "] start: " + startDateTime.getTime());
                                    }
                                    endDateTime.setTime(sdf.parse(itemRange.getEnd() + ":00"));

                                    startDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL);
                                    startDateTime.add(Calendar.MILLISECOND, 1000);
                                    endDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL * -1);
                                    endDateTime.add(Calendar.MILLISECOND, 1000);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break outloop;
                            }
                        }
                    }
                }


            } else if (OrderHelper.INSTANCE.getDeliveryMode().equals("curbside")) {
                for (int i = 0; i < OrderHelper.INSTANCE.getCalendarRange().getCalendar().size(); i++) {
                    if (OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getType().equals("curbsidepickup")) {
                        outloop:
                        for (Range itemRange : OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getRanges()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                            String tmpDate = sdf1.format(mCalendar.getTime());
                            if (itemRange.getStart().contains(tmpDate)) {
                                try {
                                    System.out.println("[" + "BR CURRENT" + "] start: " + OrderHelper.INSTANCE.getMCurrentDate());
                                    System.out.println("[" + "BR MCALENDAR" + "] start: " + mCalendar.getTime());
                                    if (OrderHelper.INSTANCE.getMCurrentDate().getDay() == mCalendar.getTime().getDay()) {
                                        try {
                                            startDateTime.setTime(sdf.parse(OrderHelper.INSTANCE.getBasket().getEarliestreadytime() + ":00"));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        System.out.println("[" + "ACECAR-istiden" + "] start: " + startDateTime.getTime());
                                    } else {
                                        startDateTime.setTime(sdf.parse(itemRange.getStart() + ":00"));
                                        System.out.println("[" + "ACECAR-razlicen den" + "] start: " + startDateTime.getTime());
                                    }
                                    endDateTime.setTime(sdf.parse(itemRange.getEnd() + ":00"));

                                    startDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL);
                                    startDateTime.add(Calendar.MILLISECOND, 1000);
                                    endDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL * -1);
                                    endDateTime.add(Calendar.MILLISECOND, 1000);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break outloop;
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < OrderHelper.INSTANCE.getCalendarRange().getCalendar().size(); i++) {
                    if (OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getType().equals("dispatch")) {
                        outloop:
                        for (Range itemRange : OrderHelper.INSTANCE.getCalendarRange().getCalendar().get(i).getRanges()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                            String tmpDate = sdf1.format(mCalendar.getTime());
                            if (itemRange.getStart().contains(tmpDate)) {
                                try {
                                    System.out.println("[" + "BR CURRENT" + "] start: " + OrderHelper.INSTANCE.getMCurrentDate());
                                    System.out.println("[" + "BR MCALENDAR" + "] start: " + mCalendar.getTime());
                                    if (OrderHelper.INSTANCE.getMCurrentDate().getDay() == mCalendar.getTime().getDay()) {
                                        try {
                                            startDateTime.setTime(sdf.parse(OrderHelper.INSTANCE.getBasket().getEarliestreadytime() + ":00"));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        System.out.println("[" + "ACECAR-istiden" + "] start: " + startDateTime.getTime());
                                    } else {
                                        startDateTime.setTime(sdf.parse(itemRange.getStart() + ":00"));
                                        System.out.println("[" + "ACECAR-razlicen den" + "] start: " + startDateTime.getTime());
                                    }
                                    endDateTime.setTime(sdf.parse(itemRange.getEnd() + ":00"));

                                    startDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL);
                                    startDateTime.add(Calendar.MILLISECOND, 1000);
                                    endDateTime.add(Calendar.MINUTE, TIME_PICKER_INTERVAL * -1);
                                    endDateTime.add(Calendar.MILLISECOND, 1000);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break outloop;
                            }
                        }
                    }
                }
            }

        }
//        Calendar now = Calendar.getInstance();
//        now.setTime(OrderHelper.INSTANCE.getMCurrentDate());
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        if (sdf.format(now.getTime()).equalsIgnoreCase(sdf.format(startDateTime.getTime()))) {
//            if (now.after(startDateTime)) {
//                startDateTime.setTime(now.getTime());
//
//                System.out.println("[" + TAG + "] set startDateTime after firsttime");
//
//                if (mCalendar.before(startDateTime))
//                    updateTime(startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime.get(Calendar.MINUTE));
//            }
//        } else
//            updateTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(OrderHelper.INSTANCE.getMCurrentDate());

        Calendar selectTime = Calendar.getInstance();
        selectTime.setTime(OrderHelper.INSTANCE.getMCurrentDate());

        Calendar later = Calendar.getInstance();
        later.setTime(mCalendar.getTime());

        System.out.println("[" + TAG + "] mCurrentDate: " + OrderHelper.INSTANCE.getMCurrentDate());
        System.out.println("[" + TAG + "] mCalendar: " + mCalendar.getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        // checking for currentTime
        if (sdf.format(selectTime.getTime()).equalsIgnoreCase(sdf.format(startDateTime.getTime()))) {
            if (selectTime.after(later))
                selectTime.setTime(later.getTime());

            // selected time between openTime & closedTime
            if (selectTime.after(startDateTime) && selectTime.before(endDateTime)) {
                if (startDateTime.before(currentTime)) {
                    System.out.println("[" + TAG + "] openTime before currentTime, set openTime=currentTime");

                    startDateTime.setTime(currentTime.getTime());
                    startDateTime.set(Calendar.SECOND, 1);
                }

                if (selectTime.before(startDateTime)) {
                    System.out.println("[" + TAG + "] selectTime before openTime, set selectTime=openTime");

                    updateTime(startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL);

                    mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                            mCalendar.get(Calendar.DAY_OF_MONTH), startDateTime.get(Calendar.HOUR_OF_DAY),
                            startDateTime.get(Calendar.MINUTE));
                }

                // selected time after closedTime
            } else if (selectTime.after(endDateTime)) {
                System.out.println("[" + TAG + "] selectTime after closedTime, set date to tomorrow with openTime");

                if (startDateTime.before(currentTime)) {
                    System.out.println("[" + TAG + "] openTime before currentTime, set openTime=currentTime");

                    startDateTime.setTime(currentTime.getTime());
                    startDateTime.set(Calendar.SECOND, 1);
                }

                mCalendar.add(Calendar.DAY_OF_MONTH, 1);

                updateDate(mCalendar.get(Calendar.DAY_OF_MONTH), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.YEAR));
                updateTime(startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL);

                mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH), startDateTime.get(Calendar.HOUR_OF_DAY),
                        startDateTime.get(Calendar.MINUTE));

                // selected time before openTime
            } else if (selectTime.before(startDateTime)) {
                System.out.println("[" + TAG + "] selectTime before openTime");

                if (startDateTime.before(currentTime)) {
                    System.out.println("[" + TAG + "] openTime before currentTime, set openTime=currentTime");

                    startDateTime.setTime(currentTime.getTime());
                    startDateTime.set(Calendar.SECOND, 1);

                    updateTime(startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL);

                    mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                            mCalendar.get(Calendar.DAY_OF_MONTH), startDateTime.get(Calendar.HOUR_OF_DAY),
                            startDateTime.get(Calendar.MINUTE));
                } else {
                    System.out.println("[" + TAG + "] currentTime before openTime, set startDateTime=currentTime");

                    updateTime(startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL);

                    mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                            mCalendar.get(Calendar.DAY_OF_MONTH), startDateTime.get(Calendar.HOUR_OF_DAY),
                            startDateTime.get(Calendar.MINUTE));
                }
            }
            // checking for laterTime
        } else {
            if (later.after(endDateTime)) {
                System.out.println("[" + TAG + "] laterTime after closedTime, set time to closeTime");

                updateTime(endDateTime.get(Calendar.HOUR_OF_DAY), endDateTime.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL);

                mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH), endDateTime.get(Calendar.HOUR_OF_DAY),
                        endDateTime.get(Calendar.MINUTE));
            } else if (later.before(startDateTime)) {
                System.out.println("[" + TAG + "] currentTime before openTime, set startDateTime=currentTime");

                updateTime(startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL);

                mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH), startDateTime.get(Calendar.HOUR_OF_DAY),
                        startDateTime.get(Calendar.MINUTE));
            }
        }

        System.out.println("[" + TAG + "] startDateTime: " + startDateTime.getTime());
        System.out.println("[" + TAG + "] endDateTime: " + endDateTime.getTime());
        System.out.println("[" + TAG + "] calendarTime: " + mCalendar.getTime());
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Class<?> classForid = Class.forName("com.android.internal.R$id");
            Field timePickerField = classForid.getField("timePicker");
//			timePicker = (TimePicker) findViewById(timePickerField.getInt(null));
            Field field = classForid.getField("minute");

            NumberPicker minuteSpinner = (NumberPicker) timePicker
                    .findViewById(field.getInt(null));
            minuteSpinner.setMinValue(0);
            minuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
            List<String> displayedValues = new ArrayList<>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(String.format("%02d", i));
            }
            minuteSpinner.setDisplayedValues(displayedValues
                    .toArray(new String[displayedValues.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}