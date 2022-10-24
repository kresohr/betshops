package com.ikresimir.betshops

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener


class MainActivity : AppCompatActivity(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSheet: Button = findViewById(R.id.btnTest)
        buttonSheet.setOnClickListener {
            showDialog()
        }

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    val currentLatLong = LatLng(location.latitude, location.longitude)
                    placeUserLocationMarker(currentLatLong)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
                }
            }

    }

    private fun showDialog() {
        val dialog: Dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet)

//        val editLayout: LinearLayout = dialog.findViewById(R.id.layoutEdit)
//        val textPlaceholder: LinearLayout = dialog.findViewById(R.id.txtPlaceHolder)
//        val text1: LinearLayout = dialog.findViewById(R.id.txtTest1)
//        val txtRoute: TextView = dialog.findViewById(R.id.txtRoute)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.RED))
        dialog.window?.setDimAmount(0F)
    }

    private fun addMarkers(googleMap: GoogleMap){
        var icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal)
        googleMap.addMarker(
            MarkerOptions()
                .title("Testing")
                .position(LatLng(45.815010,15.981919))
                .icon(icon)
        )
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
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

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    if (location != null) {
                        val currentLatLong = LatLng(location.latitude, location.longitude)
                        placeUserLocationMarker(currentLatLong)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
                    }
                }

            return
        }
        else{
            Toast.makeText(this,"Enable location in app settings for precise", Toast.LENGTH_SHORT).show()
        }
    }

    private fun placeUserLocationMarker(currentLatLong: LatLng){
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        map.addMarker(markerOptions)
    }

    override fun onMarkerClick(p0: Marker) = false

//    override fun onMyLocationButtonClick(): Boolean {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
//            .show();
//        // Return false so that we don't consume the event and the default behavior still occurs
//        // (the camera animates to the user's current position).
//        return false;
//    }
//
//    override fun onMyLocationClick(location : Location) {
//        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG)
//            .show();
//    }

}
