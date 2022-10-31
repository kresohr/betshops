package com.ikresimir.betshops.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import com.ikresimir.betshops.R
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.ikresimir.betshops.data.Repository
import com.ikresimir.betshops.model.BetshopsList
import com.ikresimir.betshops.model.MyClusterItem
import com.ikresimir.betshops.util.CustomClusterRenderer
import com.ikresimir.betshops.util.CustomInfoWindow
import com.ikresimir.betshops.util.VisibleBoundsAlgorithm
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(),
    OnMapReadyCallback, DialogInterface.OnDismissListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var betshopsList = BetshopsList(arrayOf(), 0)
    private lateinit var clusterManager: ClusterManager<MyClusterItem>
    private var lastSelectedMarker: Marker? = null


    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isLocationEnabled()) {
            LocationSettingsDialog(this).askForLocationDialog()
        }

        observeLocationSettings()
        setMapFragment()
        getRepositoryData()
        centerMapOnUser()
    }

    private fun observeLocationSettings() {
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        this.registerReceiver(gpsSwitchStateReceiver, filter)
    }

    private fun setMapFragment() {
        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getRepositoryData() {
        val repository = Repository()
        if (repository.isOnline(this)) {
            repository.getBetshops()

            GlobalScope.launch(Dispatchers.IO) {
                delay(3000L)
                if (repository.betshopsList.count == 0) {
                    delay(3000L)
                }
                betshopsList = repository.betshopsList
            }
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }


    }

    private fun openLocationDetailsDialog(
        lat: Double,
        lng: Double,
        name: String,
        address: String,
        city: String,
        county: String
    ) {
        val locationDetailsDialog = LocationDetailsDialog(this, this)
        locationDetailsDialog.showLocationDetailsDialog(lat, lng, name, address, city, county)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
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
    private fun centerMapOnUser() {

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_LOW_POWER, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLong = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 15f))
                }

            }
    }

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        clusterManager = ClusterManager<MyClusterItem>(this, map)
        map.setOnMarkerClickListener(clusterManager)
        map.setOnCameraIdleListener(clusterManager)

        setUpClusterMap()
        requestLocationPermission()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpClusterMap() {
        // Custom algorithm for clustering (Uses quad tree)
        val visibleBoundsAlgorithm = VisibleBoundsAlgorithm<MyClusterItem>(map)
        clusterManager.setAlgorithm(visibleBoundsAlgorithm)

        /** Delayed so the JSON response can be added to the list
         *  if the list is still empty, delay for another 3 seconds
         *  and try again.
         */
        GlobalScope.launch(Dispatchers.Main) {
            delay(4000L)
            if (betshopsList.count != 0){
                addMarkersOnMap()
            }
            else{
                delay(3000L)
                addMarkersOnMap()
            }

        }
        clusterManager.cluster()

        val customClusterRenderer = CustomClusterRenderer(this,map,clusterManager)
        clusterManager.renderer = customClusterRenderer
        clusterManager.markerCollection.setInfoWindowAdapter(CustomInfoWindow(LayoutInflater.from(this)))
        map.setInfoWindowAdapter(clusterManager.getMarkerManager())

        clusterManager.setOnClusterItemClickListener { selectedClusterItem ->
            val activeMarkerIcon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_active)

            val mSelectedMarker = customClusterRenderer.getMarker(selectedClusterItem)
            mSelectedMarker.setIcon(activeMarkerIcon)
            lastSelectedMarker = mSelectedMarker

            // If cluster item has been selected, find it in the list by lat & long and pass the details to dialog
            for (betshop in betshopsList.betshops) {
                if (betshop.location.lat == selectedClusterItem.position.latitude && betshop.location.lng == selectedClusterItem.position.longitude) {
                    openLocationDetailsDialog(
                        selectedClusterItem.position.latitude,
                        selectedClusterItem.position.longitude,
                        betshop.name, betshop.address, betshop.city, betshop.county
                    )
                }
            }
            return@setOnClusterItemClickListener false
        }

        map.setOnCameraIdleListener {
            visibleBoundsAlgorithm.updateBounds()
            clusterManager.cluster()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationPermission() {

        /**
        "On Android 12 (API level 31) or higher, users can request that your app retrieve only approximate location information,
        even when your app requests the ACCESS_FINE_LOCATION runtime permission."
         */

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_REQUEST_CODE
            )
            return
        }
        map.isMyLocationEnabled = true
    }

    private fun addMarkersOnMap(){
        for (betshop in betshopsList.betshops) {
            clusterManager.addItem(
                MyClusterItem(
                    betshop.location.lat,
                    betshop.location.lng,
                    betshop.name,
                    betshop.address
                )
            )
        }
    }

    // After the LocationDetailsDialog has been dismissed, return marker icon to normal.
    override fun onDismiss(dialog: DialogInterface?) {
        val normalMarkerIcon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal)
        lastSelectedMarker?.setIcon(normalMarkerIcon)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            centerMapOnUser()
            return
        }
    }

}





