package com.codewithrish.bonjour.models;

import android.webkit.JavascriptInterface;

import com.codewithrish.bonjour.activities.CallingActivity;

public class InterfaceJava {
    CallingActivity callingActivity;
    public InterfaceJava(CallingActivity callingActivity) {
        this.callingActivity = callingActivity;
    }

    @JavascriptInterface
    public void onPeerConnected() {
        callingActivity.onPeerConnected();
    }
}
