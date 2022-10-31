package com.ikresimir.betshops.util

import android.util.Log
import com.google.maps.android.clustering.ClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithm
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.quadtree.PointQuadTree
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.geometry.Bounds
import java.util.ArrayList
import java.util.concurrent.TimeUnit

/**
 * Custom algorithm for cluster rendering based on QuadTree found on StackOverflow:
 * https://stackoverflow.com/questions/38674213/best-way-to-render-only-the-visible-cluster-items-on-a-google-map/70910328#70910328
 */

class VisibleBoundsAlgorithm<T : ClusterItem?>(private val map: GoogleMap) :
    NonHierarchicalDistanceBasedAlgorithm<T>(), ScreenBasedAlgorithm<T> {
    private var bounds: LatLngBounds
    override fun getClusteringItems(
        quadTree: PointQuadTree<QuadItem<T>>,
        mapZoom: Float
    ): Collection<QuadItem<T>> {
        val oldMillis = System.currentTimeMillis() //Getting time before operations

        //Getting all items from QuadTree
        val items: MutableList<QuadItem<T>> = ArrayList(quadTree.search(Bounds(0.0, 1.0, 0.0, 1.0)))
        val itemIterator = items.listIterator()
        while (itemIterator.hasNext()) {
            val item = itemIterator.next()
            if (!bounds.contains(item.position)) { //if map do not contain item on this position remove it
                itemIterator.remove()
            }
        }
        val newMillis = System.currentTimeMillis()
        Log.println(
            Log.ERROR,
            "Clustering items",
            "end in: " + (newMillis - oldMillis) + " (" + TimeUnit.SECONDS.convert(
                newMillis - oldMillis,
                TimeUnit.MILLISECONDS
            ) + " seconds)"
        )
        return items //Returning new massive of items
    }

    /**
     * Call this before clustering.
     */
    fun updateBounds() {
        bounds = map.projection.visibleRegion.latLngBounds
    }

    //When map is moving or zooming it should re-cluster
    override fun shouldReclusterOnMapMovement(): Boolean {
        return true
    }

    override fun onCameraChange(position: CameraPosition) {
        //required
    }

    init {
        bounds = map.projection.visibleRegion.latLngBounds
    }
}