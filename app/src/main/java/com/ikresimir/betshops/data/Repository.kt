package com.ikresimir.betshops.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ikresimir.betshops.api.ApiRequests
import com.ikresimir.betshops.model.BetshopsList
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://interview.superology.dev/"

class Repository {
    var betshopsList = BetshopsList(arrayOf(), 0)

    @OptIn(DelicateCoroutinesApi::class)
    fun getBetshops() {

        /** Bounding box consists of 4 different coordinates
         *  Lat, Lng from NorthEast and Lat,Lng from SouthWest
         *  Values specified below are enough to cover a bit more than "Germany" region
         *  In case if "Betshop" opens more locations, boundingBox coordinates can be expanded as well
         */
        val northEastLat = 56.043664
        val northEastLng = 18.864658
        val southWestLat = 45.115208
        val southWestLng = 1.450694

        val api =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequests::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            val response = api.getBetShops(
                BASE_URL + "betshops?boundingBox=" + northEastLat + "," + northEastLng + "," + southWestLat + "," + southWestLng
            ).awaitResponse()
            if (response.isSuccessful) {
                betshopsList = response.body()!!
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

}


