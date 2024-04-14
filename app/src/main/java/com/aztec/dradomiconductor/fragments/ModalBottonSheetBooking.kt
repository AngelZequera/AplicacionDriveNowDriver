package com.aztec.dradomiconductor.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.aztec.dradomiconductor.R
import com.aztec.dradomiconductor.activities.MapActivity
import com.aztec.dradomiconductor.activities.MapTripActivity
import com.aztec.dradomiconductor.models.Booking
import com.aztec.dradomiconductor.providers.AuthProvider
import com.aztec.dradomiconductor.providers.BookingProvider
import com.aztec.dradomiconductor.providers.GeoProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottonSheetBooking: BottomSheetDialogFragment() {

    private lateinit var textViewOrigin: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var textViewTimeAndDistance: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancelar: Button

    private val bookingProvider = BookingProvider()
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private lateinit var mapActivity: MapActivity

    private lateinit var booking:Booking

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view = inflater.inflate(R.layout.modal_botton_sheet_booking,container, false)


        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        textViewTimeAndDistance = view.findViewById(R.id.textViewTimeAndDistance)
        btnAccept = view.findViewById(R.id.btnAccept)
        btnCancelar = view.findViewById(R.id.btnCancel)

        //La llave
        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!


        Log.d("ARGUMENTS","Booking: ${booking?.toJson()}")
        //Toast.makeText(context, "${booking?.toJson()}\"", Toast.LENGTH_SHORT).show()
        textViewOrigin.text = booking?.origin
        textViewDestination.text = booking?.destination
        textViewTimeAndDistance.text = "${booking?.time} Min - ${booking?.km} km"

        btnAccept.setOnClickListener { acceptBooking(booking?.idCliente!!) }
        btnCancelar.setOnClickListener { cancelBooking(booking?.idCliente!!) }

        return view
    }

    private fun cancelBooking(idClient: String){
        bookingProvider.updateStatus(idClient, "cancel").addOnCompleteListener {
            (activity as? MapActivity)?.timer?.cancel()
            dismiss()
        }
    }
    private fun acceptBooking(idClient: String){
        bookingProvider.updateStatus(idClient, "accept").addOnCompleteListener {
            (activity as? MapActivity)?.timer?.cancel()
            if(it.isSuccessful){
                (activity as? MapActivity)?.easyWayLocation?.endUpdates()
                geoProvider.removeLocation(authProvider.getId())
                gotoMapTrip()
           //     Toast.makeText(context, "Medico en camino", Toast.LENGTH_SHORT).show()

            }else{
//                if(context != null){
//                    Toast.makeText(activity, "No se pudo aceptar", Toast.LENGTH_LONG).show()
//                }
            }
        }
    }

    private fun gotoMapTrip(){
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context?.startActivity(i)
    }

    companion object {
        const val TAG = "ModalBottonSheet"
    }

    // fun cuando el usuario oculta el modal
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? MapActivity)?.timer?.cancel()
//        if(booking.idCliente != null){
//            cancelBooking(booking?.idCliente!!)
//        }
    }
}