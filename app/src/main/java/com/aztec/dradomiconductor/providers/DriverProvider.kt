package com.aztec.dradomiconductor.providers

import android.net.Uri
import android.util.Log
import com.aztec.dradomiconductor.models.Driver
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File

class DriverProvider {
    val db = Firebase.firestore.collection("Drivers")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")

    fun create(driver: Driver):Task<Void>{
        return db.document(driver.id!!).set(driver)
    }



    fun uploadImage(id: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromeFile = Uri.fromFile(file)
        val ref = storage.child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromeFile)

        return uploadTask.addOnFailureListener {
            Log.d("FIREBASE", "ERROR {${it.message}}")
        }
    }

    fun getDriver(idDriver: String): Task<DocumentSnapshot> {
        return db.document(idDriver).get()
    }

    fun createToken(idDriver: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful){
                val token = it.result // Token de notificaciones
                updateToken(idDriver, token)
            }
        }
    }


    fun updateToken(idDriver: String, token: String): Task<Void> {

        val map: MutableMap<String, Any> = HashMap()
        map["token"] = token
        return db.document(idDriver).update(map)
    }

    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }


    fun update(driver: Driver): Task<Void> {

        val map: MutableMap<String, Any> = HashMap()
        map["nombre"] = driver?.nombre!!
        map["apellido"] = driver?.apellido!!
        map["telefono"] = driver?.telefono!!
        map["marcaAuto"] = driver?.marcaAuto!!
        map["colorCar"] = driver?.colorCar!!
        map["placa"] = driver?.placa!!
        map["imagen"] = driver?.imagen!!
        return db.document(driver?.id!!).update(map)
    }

}