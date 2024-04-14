package com.aztec.dradomiconductor.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthProvider {


    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(correo: String, contrasena: String): Task<AuthResult>{
        return auth.signInWithEmailAndPassword(correo, contrasena)
    }

    fun registro(correo: String, contrasena: String): Task<AuthResult>{
        return auth.createUserWithEmailAndPassword(correo, contrasena)
    }

    fun existSeccion(): Boolean{
        var exist = false
        if(auth.currentUser != null){
            exist = true
        }
        return exist
    }

    fun getId():String{



        // EJECUCIÓN DE PUNTERO NULO
        /*
        *
        *  if(auth.currentUser != null){
            return auth.currentUser.uid
        }else{
            return ""
        }
        *
        * */


        return auth.currentUser?.uid ?:""
    }

    fun logout(){
        auth.signOut()
    }

}