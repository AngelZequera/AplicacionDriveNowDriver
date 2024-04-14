package com.aztec.dradomiconductor.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.aztec.dradomiconductor.R
import com.aztec.dradomiconductor.databinding.ActivityMapBinding
import com.aztec.dradomiconductor.fragments.ModalBottonSheetBooking
import com.aztec.dradomiconductor.fragments.ModalBottonSheetMenu
import com.aztec.dradomiconductor.models.Booking
import com.aztec.dradomiconductor.models.FCMBody
import com.aztec.dradomiconductor.models.FCMResponse
import com.aztec.dradomiconductor.providers.AuthProvider
import com.aztec.dradomiconductor.providers.BookingProvider
import com.aztec.dradomiconductor.providers.DriverProvider
import com.aztec.dradomiconductor.providers.GeoProvider
import com.aztec.dradomiconductor.providers.NotificationProvider
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ListenerRegistration
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener, SensorEventListener {

    private var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLating: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val driverProvider = DriverProvider()
    private val notificationProvider = NotificationProvider()
    private val modalBooking = ModalBottonSheetBooking()
    private val modalMenu = ModalBottonSheetMenu()

    //Sensor de movimiento de camara
    private var angle: Int = 0
    private val rotaciondeMatrix = FloatArray(16)
    private var sensorManager: SensorManager? = null
    private var vectSensor: Sensor? = null
    private var declinacion = 0.0f
    private var isFirstTimeOnResume = false
    private var isFirstLocation = false

    val timer = object : CountDownTimer(30000,1000){
        override fun onTick(counter: Long) {
            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "Tiempo terminado")
            modalBooking.dismiss()
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        vectSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        easyWayLocation = EasyWayLocation(this,locationRequest, false,false,this)

        //Permisos para acceder a la ubicación
        locationpermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        listenerBooking()
        createToken()

        binding.btnConnect.setOnClickListener { connectDriver() }
        binding.btnDisconnect.setOnClickListener { disconnectDriver() }
        binding.imgMenu.setOnClickListener { showModalMenu() }

    }

    val locationpermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("Localizacion", "Permiso concedido")
                   // easyWayLocation?.startLocation();
                    checkIfDriverIsConnect()

                }

                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("Localizacion", "Permiso concedido con limitacion")
                  //  easyWayLocation?.startLocation();
                    checkIfDriverIsConnect()
                }

                else -> {
                    Log.d("Localizacion", "Permiso no concedido")
                }
            }
        }
    }



    private fun createToken(){
        driverProvider.createToken(authProvider.getId())
    }


    private fun showModalMenu(){
        modalMenu.show(supportFragmentManager, ModalBottonSheetMenu.TAG)
    }

    private fun showModalBooking(booking: Booking){

        val bundle = Bundle()
        bundle.putString("booking", booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.isCancelable = false // Nos va a permitir que el usuario no pueda ocultar el modal botton sheet
        modalBooking.show(supportFragmentManager, ModalBottonSheetBooking.TAG)
        timer.start()
    }

    private fun listenerBooking(){
        //Cambios en tiempo real
        bookingListener = bookingProvider.getBooking().addSnapshotListener{ snapshot, e ->
            if (e != null){
                Log.d("Firestore","Error ${e.message}")
                return@addSnapshotListener
        }

            if (snapshot != null){
                if(snapshot.documents.size > 0){
                    val booking = snapshot!!.documents[0].toObject(Booking::class.java)
                    Log.d("Firestore","Data: ${booking?.toJson()}")
                    if(booking?.status == "create"){
                        showModalBooking(booking!!)
                    }

                }
            }
        }
    }

    private fun checkIfDriverIsConnect(){
        geoProvider.getLocation(authProvider.getId()).addOnSuccessListener { document ->
            if(document.exists()){
                if(document.contains("l")){
                    connectDriver()
                }else{
                    showButtomConnect()
                }
            }else{
                showButtomConnect()
            }
        }
    }

    private fun saveLocation(){
        if(myLocationLating != null){
            geoProvider.saveLocation(authProvider.getId(), myLocationLating!!)
        }
    }

    private fun disconnectDriver(){
        // "?" Nos aseguramos que si en caso que la variable llegue nula no se podra ejecutar el metodo
        easyWayLocation?.endUpdates()

        if(myLocationLating != null){
            geoProvider.removeLocation(authProvider.getId())
            showButtomConnect()
        }
    }

    private fun connectDriver(){
        // "?" Nos aseguramos que si en caso que la variable llegue nula no se podra ejecutar el metodo
        easyWayLocation?.endUpdates() // Cerrar los otros hilos de ejecuacion
        easyWayLocation?.startLocation()
        showButtomDisconnect()
    }

    private fun showButtomConnect(){
        binding.btnDisconnect.visibility = View.GONE // Ocultando el boton de desconectarse
        binding.btnConnect.visibility = View.VISIBLE // Mostrando el boton de conectarse
    }

      private fun showButtomDisconnect(){
        binding.btnDisconnect.visibility = View.VISIBLE  // Mostrando el boton de desconectarse
        binding.btnConnect.visibility = View.GONE // Ocultando el boton de conectarse
    }


    //Configuracion del tamaño de la imagen
    private fun addMarker(){
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.uber_car)
        val markerIcon = getMarketFromDrawable(drawable!!)

        if(markerDriver != null){
            markerDriver?.remove() // No redibujar el icono
        }

        if(myLocationLating != null){
            markerDriver = googleMap?.addMarker(
                MarkerOptions()
                    .position(myLocationLating!!)
                    .anchor(0.5f,0.5f)
                    .flat(true)
                    .icon(markerIcon)
            )
        }
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //Permitir dar zoom
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        //easyWayLocation?.startLocation()
        iniciarSensor()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap?.isMyLocationEnabled = false

        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )

            if(!success!!){
                Log.d("MAPAS","No se encontro el estilo del mapa")
            }
        }catch (e: Resources.NotFoundException){
                Log.d("MAPAS","Error: ${e.toString()}")
        }
    }

    override fun locationOn() {

    }


    //ubicacion en tiempo real
    override fun currentLocation(location: Location) { // Actualizacion de la posicion en tiempo real
        // Obtenido la LAT y LONG de la poscion actual
        myLocationLating = LatLng(location.latitude, location.longitude)

        val field = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )

        declinacion = field.declination

//        if(!isFirstLocation){
//            isFirstLocation = true
//            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
//                CameraPosition.builder().target(myLocationLating!!).zoom(19f).build()
//            ))
//        }
//        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
//            CameraPosition.builder().target(myLocationLating!!).build()
//        ))

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLating!!).zoom(19f).build()
        ))

        addDirectionMarker(myLocationLating!!, angle)
        saveLocation()
    }

    override fun locationCancelled() {

    }

    private fun updateCamare(bearing: Float){
        val oldPos = googleMap?.cameraPosition
        val pos = CameraPosition.builder(oldPos!!).bearing(bearing).tilt(50f).build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
        if (myLocationLating != null){
            addDirectionMarker(myLocationLating!!,angle)
        }
    }

    private fun addDirectionMarker(latLng: LatLng, angle: Int){
        val circleDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_up_arrow_circle)
        val markerIcon = getMarketFromDrawable(circleDrawable!!)
        if (markerDriver != null){
            markerDriver?.remove()
        }
        markerDriver = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .rotation(angle.toFloat())
                .flat(true)
                .icon(markerIcon)
        )
    }


    private fun getMarketFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            120,
            120,
            Bitmap.Config.ARGB_8888
        )

        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,120,120)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }



    override fun onDestroy() { // Cierra la aplicacion o entra a otra Activity
        super.onDestroy()
        easyWayLocation?.endUpdates()
        bookingListener?.remove()
        pararSensor()
    }


    //Cada vaz que detecta un movimiento en el celular
    override fun onSensorChanged(evento: SensorEvent?) {
        if (evento!!.sensor.type == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rotaciondeMatrix, evento.values)
            val orientacion = FloatArray(3)
            SensorManager.getOrientation(rotaciondeMatrix, orientacion)
            if (Math.abs(Math.toDegrees(orientacion[0].toDouble()) - angle) > 0.8){
                val bearing = Math.toDegrees(orientacion[0].toDouble()).toFloat() + declinacion
                updateCamare(bearing)
            }
            angle = Math.toDegrees(orientacion[0].toDouble()).toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }


    private fun iniciarSensor(){
        if (sensorManager != null){
            sensorManager?.registerListener(this,vectSensor, SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM)
        }
    }

    private fun pararSensor(){
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()

        if (!isFirstTimeOnResume){
            isFirstTimeOnResume = true
        }else{
            iniciarSensor()
        }
      //  easyWayLocation?.startLocation() // Cada vez que abrimos la pantalla actual
    }

    override fun onPause() {
        super.onPause()
        pararSensor()
    }

}