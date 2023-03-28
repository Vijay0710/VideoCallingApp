package com.asyncdevs.videocallapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CallActivity : AppCompatActivity() {

    var username = ""
    var friendUsername = ""
    var webView:WebView? = null
    var isPeerConnected = false
    var firebaseRef = Firebase.database.getReference("users")
    var isAudio = true
    var isVideo =true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        webView = findViewById<WebView>(R.id.webView)
        username = intent.getStringExtra("username")!!
        findViewById<Button>(R.id.callBtn).setOnClickListener {
            friendUsername = findViewById<EditText>(R.id.friendNameEdit).text.toString()
            sendCallRequest(friendUsername)
        }
        findViewById<ImageView>(R.id.toggleMic).setOnClickListener {
            isAudio = !isAudio
            callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")")
            findViewById<ImageView>(R.id.toggleMic).setImageResource(
                if(!isAudio)
                R.drawable.baseline_mic_off_24
            else R.drawable.baseline_mic_24
            )
        }
        findViewById<ImageView>(R.id.toggleVideoBtn).setOnClickListener {
            isVideo = !isVideo
            callJavascriptFunction("javascript:toggleVideo(\"${isVideo}\")")
            findViewById<ImageView>(R.id.toggleVideoBtn).setImageResource(
                if(!isVideo)
                    R.drawable.baseline_videocam_off_24
                else R.drawable.ic_baseline_videocam_24
            )
        }

        setUpWebView()


    }

    private fun sendCallRequest(friendUsername: String) {
        if(!isPeerConnected){
            Toast.makeText(this,"You're not connected. Check your internet connection",Toast.LENGTH_LONG).show()
            return
        } else{
            firebaseRef.child(friendUsername).child("incoming").setValue(username)
            firebaseRef.child(friendUsername).child("isAvailable").addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value.toString() == "true"){
                        listenForConnID()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun listenForConnID() {
        firebaseRef.child(friendUsername).child("connId").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null)
                    return
                switchToControls()
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setUpWebView() {

        webView!!.webChromeClient = object:WebChromeClient(){
            override fun onPermissionRequest(request: PermissionRequest?) {
//                super.onPermissionRequest(request)
                request?.grant(request.resources)
            }
        }
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.mediaPlaybackRequiresUserGesture = false
        webView!!.addJavascriptInterface(JavascriptInterface(this),"Android")
        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        webView!!.loadUrl(filePath)

        webView!!.webViewClient = object:WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
                initializePeer()

            }
        }
    }

    var uniqueId = ""
    private fun initializePeer() {
        uniqueId = getUniqueID()
        callJavascriptFunction("javascript:init(\"${uniqueId}\")")
        firebaseRef.child(username).child("incoming").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                println("Inside onDataChange")
                onCallRequest(snapshot.value as? String)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun onCallRequest(caller: String?) {
        if(caller==null) return
        findViewById<RelativeLayout>(R.id.callLayout).visibility = View.VISIBLE
        findViewById<TextView>(R.id.incomingCallText).text = "$caller is calling..."

        findViewById<ImageView>(R.id.acceptBtn).setOnClickListener {
            firebaseRef.child(username).child("connId").setValue(uniqueId)
            firebaseRef.child(username).child("isAvailable").setValue(true)

            findViewById<RelativeLayout>(R.id.callLayout).visibility = View.GONE
            switchToControls()
        }

        findViewById<ImageView>(R.id.rejectBtn).setOnClickListener {
            firebaseRef.child(username).child("incoming").setValue(null)
            findViewById<RelativeLayout>(R.id.callLayout).visibility = View.GONE
        }
    }

    private fun switchToControls() {
        findViewById<RelativeLayout>(R.id.inputLayout).visibility = View.GONE
        findViewById<LinearLayout>(R.id.callControlLayout).visibility = View.VISIBLE
    }

    private fun getUniqueID():String{
        return UUID.randomUUID().toString()
    }

    private fun callJavascriptFunction(functionString : String){
        webView!!.post {webView!!.evaluateJavascript(functionString,null)}
    }
    fun onPeerConnected() {
        isPeerConnected = true
    }

    override fun onBackPressed() {
        firebaseRef.child(username).setValue(null)
        webView!!.loadUrl("about:blank")
        startActivity(Intent(this,MainActivity::class.java))
        finish()
        super.onDestroy()

    }
}