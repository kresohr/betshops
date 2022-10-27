package com.ikresimir.betshops

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import java.time.LocalTime
import java.util.*
import java.util.Calendar.*


class MainActivity : AppCompatActivity(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener, DialogInterface.OnDismissListener{

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var selectedMarker: Marker
    private var isMarkerSelected: Boolean = false

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkCurrentTime()
        if(!isLocationEnabled(this)){
            askForLocationDialog()
        }

        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        this.registerReceiver(gpsSwitchStateReceiver, filter)

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        centerMapOnUser()
    }

    private fun askForLocationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.gps_dialog)

        val yesButton: TextView = dialog.findViewById(R.id.btnYes)
        val noButton: TextView = dialog.findViewById(R.id.btnNo)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.CENTER)

        noButton.setOnClickListener {
            dialog.dismiss()
        }
        yesButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            dialog.dismiss()
        }
    }

    private fun showLocationDetailsDialog(locationName: String?,locationAddress: String?, locationCityCounty: String?, locationPhone: String?, locationOpenHours: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet)

        val closeButton: ImageView = dialog.findViewById(R.id.btnClose)
        val routeButton: TextView = dialog.findViewById(R.id.txtRoute)
        val txtName: TextView = dialog.findViewById(R.id.txtName)
        txtName.text = locationName
        val txtAddress: TextView = dialog.findViewById(R.id.txtAddress)
        txtAddress.text = locationAddress
        val txtCityCounty: TextView = dialog.findViewById(R.id.txtCityCounty)
        txtCityCounty.text = locationCityCounty
        val txtPhone: TextView = dialog.findViewById(R.id.txtPhone)
        txtPhone.text = locationPhone
        val txtOpenHours: TextView = dialog.findViewById(R.id.txtOpenHours)
        txtOpenHours.text = checkCurrentTime()

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setDimAmount(0F)
        dialog.window?.setBackgroundDrawableResource(R.color.white)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        routeButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:44.787197,20.457273?q=44.787197,20.457273"+" ("+"test"+")"))
            startActivity(intent)
        }

        dialog.setOnDismissListener(this)
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun addMarkers(googleMap: GoogleMap){
        var icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal)
        googleMap.addMarker(
            MarkerOptions()
                .title("Testing")
                .position(LatLng(45.815010,15.981919))
                .icon(icon)
        )

        googleMap.addMarker(
            MarkerOptions()
                .title("Testing")
                .position(LatLng(45.803714752197266,15.992773056030273))
                .icon(icon)
        )
    }

    private val gpsSwitchStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isGpsEnabled && isNetworkEnabled) {
                    centerMapOnUser()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun centerMapOnUser(){

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_LOW_POWER, null).addOnSuccessListener { location : Location? ->
            if (location != null) {
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeUserLocationMarker(currentLatLong)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
                Toast.makeText(baseContext,"TESTING: MAP CENTERED ON USER", Toast.LENGTH_SHORT).show()
            }

        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false
        map.setOnMarkerClickListener(this)
        setUpMap()
        addMarkers(map)
    }

    @SuppressLint("MissingPermission")
    private fun setUpMap(){
        /**
        "On Android 12 (API level 31) or higher, users can request that your app retrieve only approximate location information, even when your app requests the ACCESS_FINE_LOCATION runtime permission."
         */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)   {
            map.isMyLocationEnabled = true
            centerMapOnUser()
            return
        }
        else{
            Toast.makeText(this,"TESTING: Location not enabled yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun placeUserLocationMarker(currentLatLong: LatLng){
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        map.addMarker(markerOptions)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        //In case of two fast selects, user can set icon for two selected markers at the same time.
        if (isMarkerSelected == true){
            var icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal)
            selectedMarker.setIcon(icon)
        }

        var icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_active)
        marker.hideInfoWindow()
        marker.setIcon(icon)
        selectedMarker = marker
        showLocationDetailsDialog(marker.title,"Zagrebačka 13", "Piškorevci - Đakovo", "031/854-140", "08:00 - 16:00")
        isMarkerSelected = true

        return true
    }



    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(gpsSwitchStateReceiver)
    }

    // If the current time is between 08:00 - 16:00, the shops are open.
    private fun checkCurrentTime(): String{
        val currentTime = getInstance().get(HOUR_OF_DAY)

        if (currentTime >= 8 && currentTime < 16){
            return "Open now until 16:00"
        }
        else
            return "Opens tomorrow at 08:00"
    }


    override fun onDismiss(p0: DialogInterface?) {
        if (selectedMarker != null){
            var icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal)
            selectedMarker.setIcon(icon)
            isMarkerSelected = false
        }
    }

}
