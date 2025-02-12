package com.trainee.project.uberapp.model

data class Booking(
    val id:String="",
    val fromLocation: String="",
    val toLocation: String="",
    val userId: String="",
    val driverId: String? = null,
    val status: String = "pending"
)
