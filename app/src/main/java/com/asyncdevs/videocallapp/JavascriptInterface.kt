package com.asyncdevs.videocallapp

class JavascriptInterface(val callActivity: CallActivity) {
    @android.webkit.JavascriptInterface
    public fun onPeerConnected(){
        callActivity.onPeerConnected()
    }
}