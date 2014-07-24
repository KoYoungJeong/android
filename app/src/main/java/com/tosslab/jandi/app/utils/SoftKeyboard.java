package com.tosslab.jandi.app.utils;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 24..
 */
public class SoftKeyboard implements View.OnFocusChangeListener {
    private static final int CLEAR_FOCUS = 0;

    private ViewGroup layout;
    private int layoutBottom;
    private InputMethodManager im;
    private int[] coords;
    private boolean isKeyboardShow;
    private SoftKeyboardChangesThread softKeyboardThread;
    private List<EditText> editTextList;

    private View tempView; // reference to a focused EditText

    public SoftKeyboard(ViewGroup layout, InputMethodManager im)
    {
        this.layout = layout;
        keyboardHideByDefault();
        initEditTexts();
        this.im = im;
        this.coords = new int[2];
        this.isKeyboardShow = false;
        this.softKeyboardThread = new SoftKeyboardChangesThread();
        this.softKeyboardThread.start();
    }


    public void openSoftKeyboard()
    {
        if(!isKeyboardShow)
        {
            layoutBottom = getLayoutCoordinates();
            im.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
            softKeyboardThread.keyboardOpened();
            isKeyboardShow = true;
        }
    }

    public void closeSoftKeyboard()
    {
        if(isKeyboardShow)
        {
            im.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            isKeyboardShow = false;
        }
    }

    public void setSoftKeyboardCallback(SoftKeyboardChanged mCallback)
    {
        softKeyboardThread.setCallback(mCallback);
    }

    public interface SoftKeyboardChanged
    {
        public void onSoftKeyboardHide();
        public void onSoftKeyboardShow();
    }

    private int getLayoutCoordinates()
    {
        layout.getLocationOnScreen(coords);
        return coords[1] + layout.getHeight();
    }

    private void keyboardHideByDefault()
    {
        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
    }

    private void initEditTexts()
    {
        editTextList = new ArrayList<EditText>();
        int childCount = layout.getChildCount();
        for(int i=0;i<=childCount -1;i++)
        {
            View v = layout.getChildAt(i);
            if(v instanceof EditText)
            {
                EditText editText = (EditText) v;
                editText.setOnFocusChangeListener(this);
                editText.setCursorVisible(false);
                editTextList.add(editText);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(hasFocus && !isKeyboardShow)
        {
            layoutBottom = getLayoutCoordinates();
            softKeyboardThread.keyboardOpened();
            isKeyboardShow = true;
            tempView = v;
        }
    }

    // This handler will clear focus of selected EditText
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message m)
        {
            switch(m.what)
            {
                case CLEAR_FOCUS:
                    if(tempView != null)
                    {
                        tempView.clearFocus();
                        tempView = null;
                    }
                    break;
            }
        }
    };

    private class SoftKeyboardChangesThread extends Thread
    {
        private boolean started;
        private SoftKeyboardChanged mCallback;

        public SoftKeyboardChangesThread()
        {
            this.started = true;
        }

        public void setCallback(SoftKeyboardChanged mCallback)
        {
            this.mCallback = mCallback;
        }

        @Override
        public void run()
        {
            while(started)
            {
                // Wait until keyboard is requested to open
                synchronized(this)
                {
                    try
                    {
                        wait();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                int currentBottomLocation = getLayoutCoordinates();

                // There is some lag between open soft-keyboard function and when it really appears.
                while(currentBottomLocation == layoutBottom)
                {
                    currentBottomLocation = getLayoutCoordinates();
                }

                mCallback.onSoftKeyboardShow();

                // When keyboard is opened from EditText, initial bottom location is greater than layoutBottom
                // and at some moment equals layoutBottom.
                // That broke the previous logic, so I added this new loop to handle this.
                while(currentBottomLocation >= layoutBottom)
                {
                    currentBottomLocation = getLayoutCoordinates();
                }

                // Now Keyboard is shown, keep checking layout dimensions until keyboard is gone
                while(currentBottomLocation != layoutBottom)
                {
                    currentBottomLocation = getLayoutCoordinates();
                }

                mCallback.onSoftKeyboardHide();

                // if keyboard has been opened clicking and EditText.
                if(isKeyboardShow)
                    isKeyboardShow = false;

                // if an EditText is focused, remove its focus (on UI thread)
                mHandler.obtainMessage(CLEAR_FOCUS).sendToTarget();
            }
        }

        public void keyboardOpened()
        {
            synchronized(this)
            {
                notify();
            }
        }

    }
}
