package com.stark.findyourbuddy

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*
import com.stark.findyourbuddy.LoginActivity.Companion.FIREBASE_USER_DATABASE
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_list.view.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    var listOfContact = mutableListOf<UserContact>()
    var userContactAdapter: MainActivity.ContactAdapter? = null
    lateinit var databaseReference: DatabaseReference


    companion object {
        const val CONTACT_REQUEST_CODE = 333
        const val PICK_CONTACT_REQUEST_CODE = 111
        const val CHECK_LOCATION_CODE = 222
        const val MOBILE_NO = "MOBILE_NO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userData = UserData(this)
        userData.isFirstTimeLoad()
        databaseReference = FirebaseDatabase.getInstance().reference

        userContactAdapter = ContactAdapter(listOfContact)
        contact_list_main_activity.adapter = userContactAdapter
        contact_list_main_activity.setOnItemClickListener { parent, view, position, id ->

            val userInfo = listOfContact[position]
            val dataFormat = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
            val date = Date()
            databaseReference!!.child(FIREBASE_USER_DATABASE).child(userInfo.mobileNo)
                .child("request").setValue(dataFormat.format(date).toString())

            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra(MOBILE_NO, userInfo.mobileNo)
            this@MainActivity.startActivity(intent)
        }

        getUserLocation()
    }

    override fun onResume() {
        super.onResume()
        refreshUsers()
        checkContactPermission()
        checkLocationPermission()
    }

    fun refreshUsers() {
        if (UserData(this).loadMobileNo()!! == "EMPTY") return

        databaseReference!!.child(FIREBASE_USER_DATABASE).child(
            UserData(this).loadMobileNo()!!).child("Finders")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listOfContact.clear()
                    if (snapshot.value == null) {
                        listOfContact.add(UserContact("NO_USER", "No"))
                        userContactAdapter!!.notifyDataSetChanged()
                        return
                    }

                    var td = snapshot.value as MutableMap<String, Any>
                    for (key in td.keys) {
                        listOfContact.add(UserContact(mapOfContact[key].toString(), key))
                        userContactAdapter!!.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.addTrackerMenu -> {
                val intent = Intent(this, MyTrackerActivity::class.java)
                startActivity(intent)
            }
            R.id.helpMenu -> {

            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkContactPermission() {
        if (Build.VERSION.SDK_INT > 23) {
            if (ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    MyTrackerActivity.CONTACT_REQUEST_CODE
                )
            }
        }
        loadContact()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            CONTACT_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContact()
                } else {
                    Toast.makeText(this, "Please Give Contact Permission ", Toast.LENGTH_LONG)
                        .show()
                }
            }

            CHECK_LOCATION_CODE -> {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                } else {
                    Toast.makeText(this, "Please Give Location Permission ", Toast.LENGTH_LONG)
                        .show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    val mapOfContact = mutableMapOf<String, String>()

    private fun loadContact() {
        mapOfContact.clear()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor != null) {
            cursor.moveToFirst()
            do {
                var name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var mobileNo =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    mapOfContact[mobileNo] = name
            } while (cursor.moveToNext())

        }
    }

    inner class ContactAdapter(listOfContact2: MutableList<UserContact>) : BaseAdapter() {

        var listOfContactContactAdapter = listOfContact2
        override fun getCount(): Int = listOfContactContactAdapter.size

        override fun getItem(position: Int): Any = listOfContactContactAdapter[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val userContact = listOfContactContactAdapter[position]

            if (userContact.name.equals("NO_USER")) {
                return layoutInflater.inflate(R.layout.no_contact, null)

            } else {
                val userContactView = layoutInflater.inflate(R.layout.contact_list, null)
                userContactView.contact_list_name.text = userContact.name
                userContactView.contact_list_mobile_no.text = userContact.mobileNo

                return userContactView
            }
        }
    }


    fun getUserLocation() {

        if (!LocationService.isServiceRunning) {
            val intent = Intent(this, LocationService::class.java)
            startService(intent)
        }

    }


    fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT > 23) {
            if (ActivityCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), CHECK_LOCATION_CODE)
            }
        }
    }


}
