package com.aztec.dradomiconductor.models

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Client (
    val id: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val imagen: String? = null,
    val token: String? = null
) {


    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Client>(json)
    }
}