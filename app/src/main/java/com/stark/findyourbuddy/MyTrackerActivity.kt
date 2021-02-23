package com.stark.findyourbuddy

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.firebase.database.FirebaseDatabase
import com.stark.findyourbuddy.LoginActivity.Companion.FIREBASE_USER_DATABASE
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_my_tracker.*
import kotlinx.android.synthetic.main.contact_list.view.*

class MyTrackerActivity : AppCompatActivity() {

    private val TAG = "MyTrackerActivity"

    var listOfContact = mutableListOf<UserContact>()
    var userContactAdapter: ContactAdapter? = null

    companion object {
        val CONTACT_REQUEST_CODE = 333
        val PICK_CONTACT_REQUEST_CODE = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tracker)

        userContactAdapter = ContactAdapter(listOfContact)
        contact_list.adapter = userContactAdapter
        contact_list.setOnItemClickListener { parent, view, position, id ->
            val userInfo = listOfContact[position]
            UserData.MyTracker.remove(userInfo.mobileNo)

            val databaseRefrence = FirebaseDatabase.getInstance().reference
            databaseRefrence.child(FIREBASE_USER_DATABASE)
                .child(userInfo.mobileNo)
                .child("Finders")
                .child(UserData(this@MyTrackerActivity).loadMobileNo()!!)
                .removeValue()

            UserData(this@MyTrackerActivity).saveUserContactInfo()
            refreshData()
        }
        UserData(this).loadContactInfo()
        refreshData()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tracker_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.addContact -> {
                checkPermission()
            }
            R.id.finishActivity -> finish()

            else -> {
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
        }
        return true

    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT > 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    CONTACT_REQUEST_CODE
                )
            }
        }
        pickContact()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (CONTACT_REQUEST_CODE == requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickContact()
        } else {
            Toast.makeText(this, "Please Give Contact Permission ", Toast.LENGTH_LONG).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, PICK_CONTACT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_CONTACT_REQUEST_CODE -> {

                if (resultCode == Activity.RESULT_OK) {
                    val contactData = data?.data
                    val contactCursor = contentResolver.query(contactData!!, null, null, null, null)

                    if (contactCursor!!.moveToFirst()) {
                        var id = contactCursor.getString(
                            contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                        )
                        val hasMobileNO = contactCursor.getString(
                            contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        )

                        if (hasMobileNO.equals("1")) {
                            val mobileNos = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                                null, null
                            )
                            Log.i(TAG, "onActivityResult:  ${ContactsContract.CommonDataKinds.Phone.CONTENT_URI} 2 : ${ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id}")

                            mobileNos!!.moveToFirst()
                            var mobileNumber =
                                mobileNos.getString(mobileNos.getColumnIndexOrThrow("data1"))
                            var mobileNumberDisplayName = contactCursor.getString(
                                contactCursor.getColumnIndex(
                                    ContactsContract.Contacts.DISPLAY_NAME
                                )
                            )

                            UserData.MyTracker[mobileNumber] = mobileNumberDisplayName
                            refreshData()
                            UserData(this).saveUserContactInfo()

                            //Save Real Time Database
                            val databaseReference = FirebaseDatabase.getInstance().reference
                            databaseReference.child(FIREBASE_USER_DATABASE)
                                .child(mobileNumber)
                                .child("Finders")
                                .child(UserData(this).loadMobileNo()!!)
                                .setValue(true)

                        }
                    }
                }

            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    private fun refreshData() {
        listOfContact.clear()

        for ((key, value) in UserData.MyTracker) {
            listOfContact.add(UserContact(value, key))
        }
        userContactAdapter!!.notifyDataSetChanged()

    }

    inner class ContactAdapter(listOfContact2: MutableList<UserContact>) : BaseAdapter() {

        var listOfContactContactAdapter = listOfContact2

        override fun getCount(): Int = listOfContactContactAdapter.size

        override fun getItem(position: Int): Any = listOfContactContactAdapter[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val userContact = listOfContactContactAdapter[position]
            val userContactView = layoutInflater.inflate(R.layout.contact_list, null)
            userContactView.contact_list_name.setText(userContact.name)
            userContactView.contact_list_mobile_no.setText(userContact.mobileNo)

            return userContactView
        }

    }
}