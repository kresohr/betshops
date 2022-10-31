package com.ikresimir.betshops.util

import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.ikresimir.betshops.R

class CustomInfoWindow(inflater: LayoutInflater?) : GoogleMap.InfoWindowAdapter {

    private var mInflater: LayoutInflater? = inflater

    /**
     * marker.hideInfoWindow() has visual performance issue due to large number of markers
     * For that reason, I "show" an empty layout as Info Window.
     */
    val popup: View = mInflater!!.inflate(R.layout.empty_layout, null)
    override fun getInfoContents(marker: Marker): View? {
        return popup
    }

    override fun getInfoWindow(marker: Marker): View? {
        return popup
    }
}