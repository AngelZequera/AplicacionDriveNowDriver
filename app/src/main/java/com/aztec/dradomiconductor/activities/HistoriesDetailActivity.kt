package com.aztec.dradomiconductor.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.aztec.dradomiconductor.databinding.ActivityHistoryDetailBinding
import com.aztec.dradomiconductor.models.Client
import com.aztec.dradomiconductor.models.History
import com.aztec.dradomiconductor.providers.ClientProvider
import com.aztec.dradomiconductor.providers.HistoryProvider
import com.aztec.dradomiconductor.utils.RelativeTime
import com.bumptech.glide.Glide

class HistoriesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private var historyProvider = HistoryProvider()
    private var clientProvider = ClientProvider()
    private var extraid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        extraid = intent.getStringExtra("id")!!

        getHistory()

        binding.imgBack.setOnClickListener { finish() }
    }

    private fun getHistory(){
        historyProvider.getHistoryById(extraid).addOnSuccessListener { document ->
            if(document.exists()){
                val history = document.toObject(History::class.java)
                binding.txtOrigin.text  = history?.origin
                binding.txtDestination.text  = history?.destination
                binding.txtDate.text  = RelativeTime.getTimeAgo(history?.timestamp!!, this@HistoriesDetailActivity)
                binding.txtPrice.text  = "$ ${String.format("%.1f",history?.price)}"
                binding.txtMyCalification.text  = "${history?.calificationToMedico}"
                binding.txtClientCalification.text  = "${history?.calificationToClient}"
                binding.txtTimeAndDistance.text  = "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"

                getClientInfo(history?.idCliente!!)

            }
        }
    }

    private fun getClientInfo(id: String){
        clientProvider.getClientById(id).addOnSuccessListener { document ->
            if(document.exists()){
                val client = document.toObject(Client::class.java)
                binding.txtEmail.text = client?.correo
                binding.txtnombre.text = "${client?.nombre} ${client?.apellido}"

                if(client?.imagen != null){
                    if(client?.imagen != ""){
                        Glide.with(this).load(client?.imagen).into(binding.imgProfile)
                    }
                }
            }
        }
    }
}