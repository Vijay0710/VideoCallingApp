package com.asyncdevs.videocallapp

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    val permissions = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.RECORD_AUDIO)
    private val REQUESTCODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val db = Firebase.database
//        val myRef = db.getReference("users/vijay/uid")
//        myRef.setValue("1")

        if(!isPermissionGranted()){
            askPermission()
        }
        findViewById<Button>(R.id.loginBtn).setOnClickListener {
            val username = findViewById<EditText>(R.id.usernameEdit).text.toString()
            val intent = Intent(this,CallActivity::class.java)
            intent.putExtra("username",username)
            startActivity(intent)
            finish()
        }
    }

    private fun askPermission(){
        ActivityCompat.requestPermissions(this,permissions,REQUESTCODE)
    }

    private fun isPermissionGranted(): Boolean {
        permissions.forEach {
            if(ActivityCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED)
                return false
        }
        return  true
    }
}