package com.aztec.dradomiconductor.providers

import com.aztec.dradomiconductor.api.IFCMapi
import com.aztec.dradomiconductor.api.RetrofitClient
import com.aztec.dradomiconductor.models.FCMBody
import com.aztec.dradomiconductor.models.FCMResponse
import retrofit2.Call
import retrofit2.create

class NotificationProvider {
    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMBody): Call<FCMResponse>{
        return RetrofitClient.getClient(URL).create(IFCMapi::class.java).send(body)

    }
}