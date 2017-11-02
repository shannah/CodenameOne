/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Codename One through http://www.codenameone.com/ if you 
 * need additional information or have any questions.
 */

package com.codename1.ui.spinner;

import com.codename1.io.Util;
import com.codename1.l10n.L10NManager;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.list.DefaultListModel;
import com.codename1.ui.plaf.UIManager;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>{@code Picker} is a component and API that allows either popping up a spinner or
 * using the native picker API when applicable. This is quite important for some
 * platforms where the native spinner behavior is very hard to replicate.</p>
 * 
 * <script src="https://gist.github.com/codenameone/5e437d82812dfcbdf092.js"></script>
 * <img src="https://www.codenameone.com/img/developer-guide/components-picker.png" alt="Picker UI" />
 * <img src="https://www.codenameone.com/img/developer-guide/components-picker-date-time-on-simulator.png" alt="Date And Time Picker On the simulator" />
 * <img src="https://www.codenameone.com/img/developer-guide/components-picker-date-android.png" alt="Android native date picker" />
 * <img src="https://www.codenameone.com/img/developer-guide/components-picker-strings-android.png" alt="Android native String picker" />
 * <img src="https://www.codenameone.com/img/developer-guide/components-picker-time-android.png" alt="Android native time picker" />
 * <img src="https://www.codenameone.com/img/developer-guide/components-picker-duration-android.png" alt="Android duration picker" />
 * <img src="https://www.codenameone.com/img/developer-guide/components-picker-duration-hours-android.png" alt="Android duration hours picker" />
 * <img src="https://www.codenameone.com/img/developer-guide/components-picker-time-android.png" alt="Android duration minutes picker" />
 * 
 *
 * @author Shai Almog
 */
public class Picker extends Button {
    private int type = Display.PICKER_TYPE_DATE;
    private Object value = new Date();
    private boolean showMeridiem;
    private Object metaData;
    private Object renderingPrototype = "XXXXXXXXXXXXXX";
    private SimpleDateFormat formatter;
    private int preferredPopupWidth;
    private int preferredPopupHeight;
    private int minuteStep = 5;
    
    /**
     * Default constructor
     */
    public Picker() {
        setUIID("TextField");
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(Display.getInstance().isNativePickerTypeSupported(type)) {
                    
                    switch (type) {
                        case Display.PICKER_TYPE_DURATION:
                        case Display.PICKER_TYPE_DURATION_HOURS:
                        case Display.PICKER_TYPE_DURATION_MINUTES: {
                            metaData = "minuteStep="+minuteStep;
                            break;
                        }
                    }
                    
                    setEnabled(false);
                    Object val = Display.getInstance().showNativePicker(type, Picker.this, value, metaData);
                    if(val != null) {
                        value = val;
                        updateValue();
                    }
                    setEnabled(true);
                } else {
                    Dialog pickerDlg = new Dialog();
                    pickerDlg.setDisposeWhenPointerOutOfBounds(true);
                    pickerDlg.setLayout(new BorderLayout());
                    Calendar cld = Calendar.getInstance();
                    switch(type) {
                        case Display.PICKER_TYPE_STRINGS: {
                            GenericSpinner gs = new GenericSpinner();
                            if(renderingPrototype != null) {
                                gs.setRenderingPrototype((String)renderingPrototype);
                            }
                            String[] strArr = (String[])metaData;
                            gs.setModel(new DefaultListModel((Object[])strArr));
                            if(value != null) {
                                int slen = strArr.length;
                                for(int iter = 0 ; iter < slen ; iter++) {
                                    if(strArr[iter].equals(value)) {
                                        gs.getModel().setSelectedIndex(iter);
                                        break;
                                    }
                                }
                            }
                            if (showDialog(pickerDlg, gs)) {
                                value = gs.getValue();
                            }
                            break;
                        }
                        case Display.PICKER_TYPE_DATE: {
                            DateSpinner ds = new DateSpinner();
                            if(value == null) {
                                cld.setTime(new Date());
                            } else {
                                cld.setTime((Date)value);
                            }
                            ds.setStartYear(1900);
                            ds.setCurrentDay(cld.get(Calendar.DAY_OF_MONTH));
                            ds.setCurrentMonth(cld.get(Calendar.MONTH) + 1);
                            ds.setCurrentYear(cld.get(Calendar.YEAR));
                            if (showDialog(pickerDlg, ds)) {
                            
                                cld.set(Calendar.DAY_OF_MONTH, ds.getCurrentDay());
                                cld.set(Calendar.MONTH, ds.getCurrentMonth() - 1);
                                cld.set(Calendar.YEAR, ds.getCurrentYear());
                                value = cld.getTime();
                            }
                            break;
                        }
                        case Display.PICKER_TYPE_TIME: {
                            int v = ((Integer)value).intValue();
                            int hour = v / 60;
                            int minute = v % 60;
                            TimeSpinner ts = new TimeSpinner();
                            ts.setShowMeridiem(isShowMeridiem());
                            if(showMeridiem && hour > 12) {
                                ts.setCurrentMeridiem(true);
                                ts.setCurrentHour(hour - 12);
                            } else {
                                ts.setCurrentHour(hour);
                            }
                            ts.setCurrentMinute(minute);
                            if (showDialog(pickerDlg, ts)) {

                                if(isShowMeridiem()) {
                                    int offset = 0;
                                    if(ts.getCurrentHour() == 12) {
                                        if(!ts.isCurrentMeridiem()) {
                                            offset = 12;
                                        }
                                    } else {
                                        if(ts.isCurrentMeridiem()) {
                                            offset = 12;
                                        }
                                    }
                                    hour = ts.getCurrentHour() + offset;
                                } else {
                                    hour = ts.getCurrentHour();
                                }
                                value = new Integer(hour * 60 + ts.getCurrentMinute());
                            }
                            break;
                        }
                        case Display.PICKER_TYPE_DATE_AND_TIME: {
                            DateTimeSpinner dts = new DateTimeSpinner();
                            cld.setTime((Date)value);
                            dts.setCurrentDate((Date)value);
                            dts.setShowMeridiem(isShowMeridiem());
                            if(isShowMeridiem() && dts.isCurrentMeridiem()) {
                                dts.setCurrentHour(cld.get(Calendar.HOUR));
                            } else {
                                dts.setCurrentHour(cld.get(Calendar.HOUR_OF_DAY));
                            }
                            dts.setCurrentMinute(cld.get(Calendar.MINUTE));
                            if (showDialog(pickerDlg, dts)) {
                                cld.setTime(dts.getCurrentDate());
                                if(isShowMeridiem() && dts.isCurrentMeridiem()) {
                                    cld.set(Calendar.AM_PM, Calendar.PM);
                                    cld.set(Calendar.HOUR, dts.getCurrentHour());
                                } else {
                                    cld.set(Calendar.HOUR_OF_DAY, dts.getCurrentHour());
                                }
                                cld.set(Calendar.MINUTE, dts.getCurrentMinute());
                                value = cld.getTime();
                            }
                            break;
                        }
                        case Display.PICKER_TYPE_DURATION_HOURS:
                        case Display.PICKER_TYPE_DURATION_MINUTES:
                        case Display.PICKER_TYPE_DURATION: {
                            long v = ((Long)value).longValue();
                            int hour = (int)(v / 1000 / 60 / 60);
                            int minute = (int) ((v / 1000 / 60) % 60);
                            TimeSpinner ts = new TimeSpinner();
                            ts.setDurationMode(true);
                            if (type == Display.PICKER_TYPE_DURATION_HOURS) {
                                ts.setMinutesVisible(false);
                            } else if (type == Display.PICKER_TYPE_DURATION_MINUTES) {
                                ts.setHoursVisible(false);
                            }
                            ts.setCurrentHour(hour);
                            ts.setCurrentMinute(minute);
                            ts.setMinuteStep(minuteStep);
                            if (showDialog(pickerDlg, ts)) {
                                
                                value = new Long(ts.getCurrentHour() * 60 * 60 * 1000l + 
                                        ts.getCurrentMinute() * 60 * 1000l);
                            }
                            break;
                        }
                            
                    }
                    updateValue();
                }
            }
            
            private boolean showDialog(Dialog pickerDlg, Component c) {
                pickerDlg.addComponent(BorderLayout.CENTER, c);
                Button ok = new Button(new Command("OK"));
                final boolean[] userCanceled = new boolean[1];
                Button cancel = new Button(new Command("Cancel") {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        userCanceled[0] = true;
                        super.actionPerformed(evt);
                    }
                });
                Container buttons = GridLayout.encloseIn(2, cancel, ok);
                pickerDlg.addComponent(BorderLayout.SOUTH, buttons);
                if(Display.getInstance().isTablet()) {
                    pickerDlg.showPopupDialog(Picker.this);
                } else {
                    pickerDlg.show();
                }
                return !userCanceled[0];
            }
        });
        updateValue();
    }
    
    /**
     * Sets the type of the picker to one of Display.PICKER_TYPE_DATE, Display.PICKER_TYPE_DATE_AND_TIME, Display.PICKER_TYPE_STRINGS, 
     * Display.PICKER_TYPE_DURATION, Display.PICKER_TYPE_DURATION_HOURS, Display.PICKER_TYPE_DURATION_MINUTES or
     * Display.PICKER_TYPE_TIME
     * @param type the type
     */
    public void setType(int type) {
        this.type = type;
        switch(type) {
            case Display.PICKER_TYPE_DATE:
            case Display.PICKER_TYPE_DATE_AND_TIME:
                if(!(value instanceof Date)) {
                    value = new Date();
                }
                break;
            case Display.PICKER_TYPE_STRINGS:
                if(!Util.instanceofObjArray(value)) {
                    setStrings(new String[] {" "});
                }
                break;
            case Display.PICKER_TYPE_TIME:
                if(!(value instanceof Integer)) {
                    setTime(0);
                }
                break;
            case Display.PICKER_TYPE_DURATION:
            case Display.PICKER_TYPE_DURATION_HOURS:
            case Display.PICKER_TYPE_DURATION_MINUTES:
                    
                if (!(value instanceof Long)) {
                    setDuration(0l);
                }
                break;
        }
    }

    /**
     * Returns the type of the picker
     * @return one of Display.PICKER_TYPE_DATE, Display.PICKER_TYPE_DATE_AND_TIME, Display.PICKER_TYPE_STRINGS,
     * Display.PICKER_TYPE_DURATION, Display.PICKER_TYPE_DURATION_HOURS, Display.PICKER_TYPE_DURATION_MINUTES, or
     * Display.PICKER_TYPE_TIME
     */
    public int getType() {
        return type;
    }
    
    /**
     * Returns the date, this value is used both for type date/date and time. Notice that this 
     * value isn't used for time
     * @return the date object
     */
    public Date getDate() {
        return (Date)value;
    }
    
    /**
     * Sets the date, this value is used both for type date/date and time. Notice that this 
     * value isn't used for time. Notice that this value will have no effect if the picker
     * is currently showing.
     * 
     * @param d the new date
     */
    public void setDate(Date d) {
        value = d;
        updateValue();
    }
    
    private String twoDigits(int i) {
        if(i < 10) {
            return "0" + i;
        }
        return "" + i;
    }
    
    /**
     * <p>Sets the string entries for the string picker. <br>
     * sample usage for this method below:</p>
     * 
     * <script src="https://gist.github.com/codenameone/47602e679f61712693bd.js"></script>
     * @param strs string array
     */
    public void setStrings(String... strs) {
        this.type = Display.PICKER_TYPE_STRINGS;
        int slen = strs.length;
        for (int i = 0; i < slen; i++) {
            String str = strs[i];
            strs[i] = getUIManager().localize(str, str);
        }
        metaData = strs;
        
        if(!(value instanceof String)) {
            value = null;
        }
        updateValue();
    }
    
    /**
     * Returns the String array matching the metadata
     * @return a string array
     */
    public String[] getStrings() {
        return (String[])metaData;
    }
    
    /**
     * Sets the current value in a string array picker
     * @param str the current value
     */
    public void setSelectedString(String str) {
        value = str;
        updateValue();
    }
    
    /**
     * Returns the current string
     * @return the selected string
     */
    public String getSelectedString() {
        return (String) value;
    }
    
    /**
     * Returns the index of the selected string
     * @return the selected string offset or -1
     */
    public int getSelectedStringIndex() {
        int offset = 0;
        for(String s : (String[])metaData) {
            if(s == value) {
                return offset;
            }
            offset++;
        }
        return -1;
    }

    /**
     * Returns the index of the selected string
     * @param index sets the index of the selected string
     */
    public void setSelectedStringIndex(int index) {
        value = ((String[])metaData)[index];
        updateValue();
    }

    /**
     * Updates the display value of the picker, subclasses can override this to invoke 
     * set text with the right value
     */
    protected void updateValue() {
        if(value == null) {
            setText("...");
            return;
        }
        
        if(getFormatter() != null) {
            setText(formatter.format(value));
            return;
        }
        
        switch(type) {
            case Display.PICKER_TYPE_STRINGS: {
                value = getUIManager().localize(value.toString(), value.toString());
                setText(value.toString());
                break;
            }
            case Display.PICKER_TYPE_DATE: {
                setText(L10NManager.getInstance().formatDateShortStyle((Date)value));
                break;
            }
            case Display.PICKER_TYPE_TIME: {
                int v = ((Integer)value).intValue();
                int hour = v / 60;
                int minute = v % 60;
                if(showMeridiem) {
                    String text;
                    if(hour >= 12) {
                        text = "pm";
                    } else {
                        text = "am";
                    }
                    setText(twoDigits(hour <= 12 ? hour : hour - 12) + ":" + twoDigits(minute) + text);
                } else {
                    setText(twoDigits(hour) + ":" + twoDigits(minute));
                }
                break;
            }
            case Display.PICKER_TYPE_DATE_AND_TIME: {
                setText(L10NManager.getInstance().formatDateTimeShort((Date)value));
                break;
            }
            case Display.PICKER_TYPE_DURATION_HOURS:
            case Display.PICKER_TYPE_DURATION_MINUTES:
            case Display.PICKER_TYPE_DURATION: {
                long v = ((Long)value).longValue();
                int hour = (int)(v / 60 / 60 / 1000);
                int minute = (int)(v / 1000 / 60) % 60;
                StringBuilder sb = new StringBuilder();
                UIManager uim = getUIManager();
                if (hour > 0) {
                    sb.append(hour).append(" ")
                            .append(hour > 1 ? uim.localize("hours", "hours") : uim.localize("hour", "hour"))
                            .append(" ");
                }
                if (minute > 0) {
                    sb.append(minute).append(" ")
                            .append(minute > 1 ? uim.localize("minutes", "minutes") : uim.localize("minute", "minute"));
                            
                }
                setText(sb.toString().trim());
                if ("".equals(getText())) {
                    setText("...");
                }
                break;
            }
                
        }
    }
    
    /**
     * This value is only used for time type and is ignored in the case of date and time where
     * both are embedded within the date.
     * @param time the time value as minutes since midnight e.g. 630 is 10:30am
     */
    public void setTime(int time) {
        value = new Integer(time);
        updateValue();
    }

    /**
     * Convenience method equivalent to invoking setTime(hour * 60 + minute);
     * @param hour the hour in 24hr format
     * @param minute the minute within the hour
     */
    public void setTime(int hour, int minute) {
        setTime(hour * 60 + minute);
    }
    
    /**
     * This value is only used for time type and is ignored in the case of date and time where
     * both are embedded within the date.
     * 
     * @return the time value as minutes since midnight e.g. 630 is 10:30am
     */
    public int getTime() {
        return ((Integer)value).intValue();
    }
    
    /**
     * This value is only used for duration type.
     * @param duration The duration value in milliseconds.
     * @see #setDuration(int, int) 
     * @see #getDuration() 
     * @see #getDurationHours() 
     * @see #getDurationMinutes() 
     */
    public void setDuration(long duration) {
        value = new Long(duration);
        updateValue();
    }
    
    /**
     * Sets the minute step size for PICKER_TYPE_DURATION, and PICKER_TYPE_DURATION_TIME types.
     * @param step The step size in minutes.
     */
    public void setMinuteStep(int step) {
        this.minuteStep = step;
    }
    
    /**
     * Convenience method for setting duration in hours and minutes.
     * @param hour The hours for duration.
     * @param minute The minutes for duration.
     * @see #setDuration(long) 
     * @see #getDuration() 
     * @see #getDurationHours() 
     * @see #getDurationMinutes() 
     */
    public void setDuration(int hour, int minute) {
        setDuration(hour * 60 * 60 * 1000l + minute * 60 *1000l);
    }
    
    /**
     * This value is used for the duration type.
     * @return The duration in milliseconds.
     * @see #getDurationHours() 
     * @see #getDurationMinutes() 
     */
    public long getDuration() {
        return (Long)value;
    }
    
    /**
     * Gets the duration hours.  Used only for duration type.
     * @return The duration hours.
     * @see #getDurationMinutes() 
     * @see #getDuration() 
     */
    public int getDurationHours() {
        return (int)(getDuration() / 60 / 60 / 1000l);
    }
    
    /**
     * Gets the duration minutes.  Used only for duration type.
     * @return The duration minutes.
     * @see #getDurationHours() 
     * @see #getDuration()
     */
    public int getDurationMinutes() {
        return (int)(getDuration() / 1000 / 60) % 60;
    }

    /**
     * Indicates whether hours should be rendered as AM/PM or 24hr format
     * @return the showMeridiem
     */
    public boolean isShowMeridiem() {
        return showMeridiem;
    }

    /**
     * Indicates whether hours should be rendered as AM/PM or 24hr format
     * @param showMeridiem the showMeridiem to set
     */
    public void setShowMeridiem(boolean showMeridiem) {
        this.showMeridiem = showMeridiem;
        updateValue();
    }

    /**
     * When using a lightweight spinner this will be used as the rendering prototype
     * @return the renderingPrototype
     */
    public Object getRenderingPrototype() {
        return renderingPrototype;
    }

    /**
     * When using a lightweight spinner this will be used as the rendering prototype
     * @param renderingPrototype the renderingPrototype to set
     */
    public void setRenderingPrototype(Object renderingPrototype) {
        this.renderingPrototype = renderingPrototype;
    }

    /**
     * Allows us to define a date format for the display of dates/times
     * @return the defined formatter
     */
    public SimpleDateFormat getFormatter() {
        return formatter;
    }

    /**
     * Allows us to define a date format for the display of dates/times
     * 
     * @param formatter the new formatter
     */
    public void setFormatter(SimpleDateFormat formatter) {
        this.formatter = formatter;
        updateValue();
    }
    
    /**
     * The preferred width of the popup dialog for the picker.  This will only 
     * be used on devices where the popup width and height are configurable, such 
     * as the iPad or tablets.  On iPhone, the picker always spans the width of the 
     * screen along the bottom.
     * @param width The preferred width of the popup.
     */
    public void setPreferredPopupWidth(int width) {
        this.preferredPopupWidth = width;
    }
    
    /**
     * The preferred height of the popup dialog for the picker.  This will only 
     * be used on devices where the popup width and height are configurable, such 
     * as the iPad or tablets.  On iPhone, the picker always spans the width of the 
     * screen along the bottom.
     * @param height The preferred height of the popup.
     */
    public void setPreferredPopupHeight(int height) {
        this.preferredPopupHeight = height;
    }
    
    /**
     * The preferred width of the popup dialog. This will only 
     * be used on devices where the popup width and height are configurable, such 
     * as the iPad or tablets.  On iPhone, the picker always spans the width of the 
     * screen along the bottom. 
     * @return 
     */
    public int getPreferredPopupWidth() {
        return preferredPopupWidth;
    }
    
    /**
     * The preferred height of the popup dialog.  This will only 
     * be used on devices where the popup width and height are configurable, such 
     * as the iPad or tablets.  On iPhone, the picker always spans the width of the 
     * screen along the bottom.
     * @return 
     */
    public int getPreferredPopupHeight() {
        return preferredPopupHeight;
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getPropertyNames() {
        return new String[] {"Strings"};
    }

    /**
     * {@inheritDoc}
     */
    public Class[] getPropertyTypes() {
       return new Class[] { String[].class };
    }

    /**
     * {@inheritDoc}
     */
    public String[] getPropertyTypeNames() {
        return new String[] {"String []"};
    }

    /**
     * {@inheritDoc}
     */
    public Object getPropertyValue(String name) {
        if(name.equals("Strings")) {
            return getStrings();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String setPropertyValue(String name, Object value) {
        if(name.equals("Strings")) {
            setStrings((String[])value);
            return null;
        }
        return super.setPropertyValue(name, value);
    }

    /**
     * Returns the value which works for all picker types
     * @return the value object
     */
    public Object getValue() {
        return value;
    }
}
