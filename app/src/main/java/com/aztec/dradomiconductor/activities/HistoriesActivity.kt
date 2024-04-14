package com.aztec.dradomiconductor.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.aztec.dradomiconductor.R
import com.aztec.dradomiconductor.adapter.HistoryAdapter
import com.aztec.dradomiconductor.databinding.ActivityHistoriesBinding
import com.aztec.dradomiconductor.models.History
import com.aztec.dradomiconductor.providers.HistoryProvider

class HistoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoriesBinding
    private var historyProvider = HistoryProvider()
    private var histories = ArrayList<History>()
    private lateinit var adapter: HistoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.listHistories.layoutManager = linearLayoutManager

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Historial de viajes medicos"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        getHistories()
    }

    private fun getHistories(){
        histories.clear()

        historyProvider.getHistories().get().addOnSuccessListener { query ->
            if(query != null){
                if (query.documents.size > 0){
                    val documents = query.documents
                    for (d in documents){
                        var history = d.toObject(History::class.java)
                        history?.id = d.id
                        histories.add(history!!)
                    }
                    adapter = HistoryAdapter(this@HistoriesActivity,histories)
                    binding.listHistories.adapter = adapter
                }
            }
        }
    }
}