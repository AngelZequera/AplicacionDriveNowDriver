package com.aztec.dradomiconductor.providers

import android.util.Log
import com.aztec.dradomiconductor.models.Booking
import com.aztec.dradomiconductor.models.Client
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class BookingProvider {
    val db = Firebase.firestore.collection("Bookings")
    val authProvider = AuthProvider()


    fun create(booking: Booking): Task<Void> {
       return db.document(authProvider.getId()).set(booking).addOnFailureListener {
           Log.d("FIRESTORE","Error: ${it.message}")
       }
    }


    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())
    }

    fun updateStatus (idClient: String, status: String): Task<Void> {
        return db.document(idClient).update("status", status).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR ${exception.message}")
        }
    }
}