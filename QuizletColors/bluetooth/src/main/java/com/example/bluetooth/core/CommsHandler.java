package com.example.bluetooth.core;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class CommsHandler extends Handler {
    public static final String TAG = CommsHandler.class.getSimpleName();

    public interface IMessageListener {
        void update(@NonNull String msg);
    }
    public interface IEndListener {
        void end();
    }

    private final IMessageListener mMessageListener;
    private final IEndListener mEndListener;

    public CommsHandler(IMessageListener messageListener, IEndListener endListener) {
        super();
        mMessageListener = messageListener;
        mEndListener = endListener;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        byte[] byteArray = (byte[]) msg.obj;
        int length = msg.arg1;
        byte[] resultArray = length == -1 ? byteArray : new byte[length];
        if (byteArray != null) {
            for (int i = 0; i < byteArray.length && i < length; ++i) {
                resultArray[i] = byteArray[i];
            }
        }
        String text = new String(resultArray, StandardCharsets.UTF_8);
        if (msg.what == BluetoothTalker.MessageConstants.MESSAGE_WRITE) {
            Log.v(TAG, "we just wrote... [" + length + "] '" + text + "'");
        } else if (msg.what == BluetoothTalker.MessageConstants.MESSAGE_READ) {
            Log.d(TAG, "we just read... [" + length + "] '" + text + "'");
            Log.v(TAG, "    >>r " + Arrays.toString((byte[]) msg.obj));
            mMessageListener.update(text);
        } else if (msg.what == BluetoothTalker.MessageConstants.MESSAGE_END) {
            Log.i(TAG, "Received an end msg");
            mEndListener.end();
        }

    }
}
