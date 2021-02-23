package com.stark.findyourbuddy

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class LocationService : Service() {
    private val TAG = "LocationService"

    var isServiceRuning = false

    companion object {
        lateinit var myLocation: Location
        var isServiceRunning = false

    }

    lateinit var databaseReference: DatabaseReference

    override fun onBind(intent: Intent?): IBinder? =  null

    override fun onCreate() {
        databaseReference = FirebaseDatabase.getInstance().reference
        isServiceRuning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var myLocationListener = MyLocationListener()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            3,
            3f,
            myLocationListener
        )

        val myMobileNo = UserData(this).loadMobileNo()!!
        databaseReference!!.child(LoginActivity.FIREBASE_USER_DATABASE)
            .child(myMobileNo).child("request")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (myLocation == null) return

                    val dateFormat = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
                    val date = Date()
                    val locationFirebaseDatabaseRefrence = databaseReference!!.child(
                        LoginActivity.FIREBASE_USER_DATABASE
                    )
                        .child(myMobileNo)
                        .child("location")

                    locationFirebaseDatabaseRefrence.child("lat")
                        .setValue(myLocation!!.latitude)
                    locationFirebaseDatabaseRefrence.child("log")
                        .setValue(myLocation!!.longitude)

                    locationFirebaseDatabaseRefrence.child("lastOnline")
                        .setValue(dateFormat.format(date))
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        return Service.START_NOT_STICKY
    }


    inner class MyLocationListener : LocationListener {
        constructor() : super() {
            myLocation = Location("me")
            myLocation!!.longitude = 0.0
            myLocation!!.latitude = 0.0
        }

        override fun onLocationChanged(location: Location) {
            myLocation = location
            Log.i(TAG, "onLocationChanged: me called with latitude :  ${location.latitude} , latitude : ${location.longitude}")
        }

        override fun onProviderDisabled(provider: String) {
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {
        }
    }
}