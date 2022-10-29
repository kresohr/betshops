package com.ikresimir.betshops.model

data class Betshop(
    val address: String,
    val city: String,
    val city_id: Int,
    val county: String,
    val id: Int,
    val location: Location,
    val name: String
)