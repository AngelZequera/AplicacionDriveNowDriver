package com.aztec.dradomiconductor.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.aztec.dradomiconductor.R
import com.aztec.dradomiconductor.activities.HistoriesActivity
import com.aztec.dradomiconductor.activities.MainActivity
import com.aztec.dradomiconductor.activities.MapActivity
import com.aztec.dradomiconductor.activities.MapTripActivity
import com.aztec.dradomiconductor.activities.ProfileActivity
import com.aztec.dradomiconductor.models.Booking
import com.aztec.dradomiconductor.models.Driver
import com.aztec.dradomiconductor.providers.AuthProvider
import com.aztec.dradomiconductor.providers.BookingProvider
import com.aztec.dradomiconductor.providers.DriverProvider
import com.aztec.dradomiconductor.providers.GeoProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.toObject

class ModalBottonSheetMenu: BottomSheetDialogFragment() {

    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()

    var txtUserName: TextView? = null
    var linearLayoutLogout: LinearLayout? = null
    var linearLayoutProfile: LinearLayout? = null
    var LinearLayoutHistory: LinearLayout? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_botton_sheet_menu,container, false)
        txtUserName = view.findViewById(R.id.txtUserName)
        linearLayoutLogout = view.findViewById(R.id.LinearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.LinearLayoutProfile)
        LinearLayoutHistory = view.findViewById(R.id.LinearLayoutHistory)

        getDriverMedico()

        linearLayoutLogout?.setOnClickListener {
            goToMain()
        }

        linearLayoutProfile?.setOnClickListener {
            goToProfile()
        }

        LinearLayoutHistory?.setOnClickListener {
            goToHistory()
        }

        return view
    }

    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun goToHistory(){
        val i = Intent(activity, HistoriesActivity::class.java)
        startActivity(i)
    }


    private fun goToMain(){
        authProvider.logout()
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getDriverMedico(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if(document.exists()){
                val driver = document.toObject(Driver::class.java)
                txtUserName?.text = "${driver?.nombre} ${driver?.apellido}"
            }
        }
    }

    companion object {
        const val TAG = "ModalBottonSheetMenu"
    }

}