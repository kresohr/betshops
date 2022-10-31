package com.ikresimir.betshops.util


import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.ikresimir.betshops.R
import com.ikresimir.betshops.model.MyClusterItem


class CustomClusterRenderer(
    context: Context?, map: GoogleMap?,
    clusterManager: ClusterManager<MyClusterItem>
) : DefaultClusterRenderer<MyClusterItem>(context, map, clusterManager)
{


    var defaultMarkerIcon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal)
    var activeMarkerIcon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_active)

    override fun onClusterItemUpdated(item: MyClusterItem, marker: Marker) {
        super.onClusterItemUpdated(item, marker)
        marker.hideInfoWindow()
    }

    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        val test = getMarker(item)
        if (test != null){
            test.hideInfoWindow()
        }

        markerOptions.icon(defaultMarkerIcon)
    }

    override fun setOnClusterItemInfoWindowClickListener(listener: ClusterManager.OnClusterItemInfoWindowClickListener<MyClusterItem>?) {
        super.setOnClusterItemInfoWindowClickListener(listener)
    }

    override fun onClusterItemRendered(clusterItem: MyClusterItem, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)
//        val markerItem = getMarker(clusterItem)
//        markerItem.hideInfoWindow()
//        setOnClusterItemClickListener {
//
//            return@setOnClusterItemClickListener false }
    }

//    override fun onClusterItemUpdated(item: MyItem, marker: Marker) {
//        super.onClusterItemUpdated(item, marker)
//        val markerItem = getMarker(clusterItem)
//        markerItem.hideInfoWindow()
//        marker.hideInfoWindow()
//
//    }

}