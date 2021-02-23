package com.stark.findyourbuddy

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import java.text.SimpleDateFormat
import java.util.*


class LoginActivity : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    private val TAG = "LoginActivity"

    companion object {
        const val FIREBASE_USER_DATABASE = "Users"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        signInAnonymously()
    }

    fun signInAnonymously() {
        mAuth!!.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "signInAnonymously: success")
                    val currentUser = mAuth!!.currentUser
                } else {
                    Toast.makeText(this@LoginActivity, "Authentication Failed ", Toast.LENGTH_LONG)
                        .show()
                }

            }
    }

    fun registerMobileNo(view: View) {
        if (mobileNo_ET.text.toString()
                .trim() == "" && mobileNo_ET.text.toString().length >= 10
        ) return

        val dataFormat = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
        val date = Date()
        val userData = UserData(this)
        userData.saveMobileNo(mobileNo_ET.text.toString())
        val databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.child(FIREBASE_USER_DATABASE).child(mobileNo_ET.text.toString())

        Toast.makeText(this, mobileNo_ET.text.toString(), Toast.LENGTH_LONG).show()
        databaseReference.child(FIREBASE_USER_DATABASE).child(mobileNo_ET.text.toString())
            .child("request")
            .setValue(dataFormat.format(date).toString())

        databaseReference.child(FIREBASE_USER_DATABASE).child(mobileNo_ET.text.toString())
            .child("Finders")

        Toast.makeText(this, mobileNo_ET.text.toString(), Toast.LENGTH_LONG).show()

        finish()
    }
}