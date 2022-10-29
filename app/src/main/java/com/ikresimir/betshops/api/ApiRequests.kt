package com.ikresimir.betshops.api

import com.ikresimir.betshops.model.BetshopsList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiRequests {

    @GET
    fun getBetShops(@Url url: String): Call<BetshopsList>


}