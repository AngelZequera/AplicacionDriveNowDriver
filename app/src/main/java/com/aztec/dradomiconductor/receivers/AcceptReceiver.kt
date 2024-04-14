package com.aztec.dradomiconductor.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aztec.dradomiconductor.activities.MapActivity
import com.aztec.dradomiconductor.activities.MapTripActivity
import com.aztec.dradomiconductor.providers.BookingProvider

class  AcceptReceiver: BroadcastReceiver() {

    val bookingProvider = BookingProvider()

    override fun onReceive(context: Context, intent: Intent) {
        val idBooking = intent.extras?.getString("idBooking")
        acceptBooking(context,idBooking!!)
    }

    //Aceptar viaje notificacion accion
    private fun acceptBooking(context: Context, idBooking: String){
        bookingProvider.updateStatus(idBooking, "accept").addOnCompleteListener {
            if(it.isSuccessful){
                gotoMapTrip(context)
                //     Toast.makeText(context, "Medico en camino", Toast.LENGTH_SHORT).show()

            }else{
               Log.d("RECEIVER","No se actualizar estado del servicio")
            }
        }
    }

    private fun gotoMapTrip(context: Context){
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.action = Intent.ACTION_RUN
        context.startActivity(i)
    }

}