package com.stark.findyourbuddy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class UserData(context: Context) {

    private var context = context
    private var sharedPreferences: SharedPreferences? = null

    companion object {
        const val USER_DATA_SHARE_PREFERENCE = "USER_DATA_SHARE_PREFERENCE";
        const val MOBILE_NO = "MOBILE_NO"
        const val LIST_OF_TRACKERS = "LIST_OF_TRACKERS"
        var MyTracker = mutableMapOf<String, String>()
    }


    init {
        this.sharedPreferences =
            context.getSharedPreferences(USER_DATA_SHARE_PREFERENCE, Context.MODE_PRIVATE)
    }

    fun saveMobileNo(mobileNo: String) {
        sharedPreferences!!
            .edit()
            .putString(MOBILE_NO, mobileNo)
            .commit()
    }

    fun loadMobileNo(): String? {
        val mobileNo = sharedPreferences!!.getString(MOBILE_NO, "EMPTY")

        return mobileNo
    }


    fun isFirstTimeLoad() {
        val mobileNo = sharedPreferences!!.getString(MOBILE_NO, "EMPTY")
        if (mobileNo == "EMPTY") {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
        }
    }

    fun saveUserContactInfo() {
        var listOfTracker = ""
        for ((k, v) in MyTracker) {

            if (listOfTracker.length == 0) {
                listOfTracker = k + "%" + v
            } else {
                listOfTracker += "*" + k + "%" + v
            }
        }

        sharedPreferences?.edit()?.putString(LIST_OF_TRACKERS, listOfTracker)?.commit()

    }

    fun loadContactInfo() {
        MyTracker.clear()
        var listOfTracker = sharedPreferences?.getString(LIST_OF_TRACKERS, "empty")
        if (!listOfTracker.equals("empty")) {
            var userContactInfo = listOfTracker!!.split("*").toTypedArray()
            var i = 0

            while (i < userContactInfo.size) {
                var userContact = userContactInfo[i].split("%").toTypedArray()
                MyTracker.put(userContact[0], userContact[1])
                i++
            }

        }

    }
}