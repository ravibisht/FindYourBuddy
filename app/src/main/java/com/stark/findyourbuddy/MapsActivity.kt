package com.stark.findyourbuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.stark.findyourbuddy.UserData.Companion.MOBILE_NO

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val bundle = intent.extras
        val mobileNo = bundle?.getString(MOBILE_NO)

        databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(LoginActivity.FIREBASE_USER_DATABASE)
            .child(mobileNo!!)
            .child("location")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    try {
                        val td = snapshot!!.value as MutableMap<String, Any>
                        val log = td["log"].toString()
                        val lat = td["lat"].toString()
                        lastOnline = td["lastOnline"].toString()
                        sydney = LatLng(lat.toDouble(), log.toDouble())
                        loadMap()
                    } catch (e: Exception) {
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    fun loadMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    companion object {
        var sydney = LatLng(-34.0, 151.0)
        var lastOnline = "no last Online"

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.addMarker(MarkerOptions().position(sydney).title("last online : $lastOnline"))
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15f))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))

    }
}