package com.ikresimir.betshops.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiRequests {

    @GET
    fun getBetShops(@Url url: String): Call<BetshopsList>


}