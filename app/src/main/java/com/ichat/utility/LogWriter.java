package com.ichat.utility;

import android.util.Log;

import imsub.interfaces.ILogWriter;

/**
 * Created by gaoqi on 2016/6/22.
 */
public class LogWriter implements ILogWriter{

    private static final String TAG = LogWriter.class.getName();
    @Override
    public void e(String tag, String log) {
        Log.e(tag,log);
    }

    @Override
    public void w(String tag, String log) {
        Log.w(tag,log);
    }

    @Override
    public void i(String tag, String log) {
        Log.i(tag,log);
    }
}
