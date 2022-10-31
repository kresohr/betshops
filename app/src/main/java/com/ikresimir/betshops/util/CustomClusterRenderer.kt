package com.ikresimir.betshops.util


import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions

import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

import com.ikresimir.betshops.R
import com.ikresimir.betshops.model.MyClusterItem

class CustomClusterRenderer(
    context: Context?, map: GoogleMap?,
    clusterManager: ClusterManager<MyClusterItem>
) : DefaultClusterRenderer<MyClusterItem>(context, map, clusterManager) {
    var defaultMarkerIcon: BitmapDescriptor =
        BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_normal)

    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        markerOptions.icon(defaultMarkerIcon)
    }


}