package com.aztec.dradomiconductor.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.aztec.dradomiconductor.providers.BookingProvider

class CancelReceiver: BroadcastReceiver() {

    val bookingProvider = BookingProvider()


    override fun onReceive(context: Context, intent: Intent) {
        val idBooking = intent.extras?.getString("idBooking")
       // Toast.makeText(context, "${idBooking}", Toast.LENGTH_SHORT).show()
        cancelBooking_notification(idBooking!!)
    }

    //Aceptar viaje notificacion accion
   private fun cancelBooking_notification(idBooking: String){
        bookingProvider.updateStatus(idBooking, "cancel").addOnCompleteListener {
            if(it.isSuccessful){
                Log.d("RECEIVER","El servicio fue cancelado")
                //     Toast.makeText(context, "Medico en camino", Toast.LENGTH_SHORT).show()

            }else{
                Log.d("RECEIVER","No se actualizado el estado del servicio")
            }
        }
    }

}