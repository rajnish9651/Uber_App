package com.trainee.project.uberapp.model

data class Booking(
    val id:String="",
    val fromLocation: String="",
    val toLocation: String="",
    val userId: String="",
    val driverId: String? = null, // Initially null, assigned when a driver accepts
    val status: String = "pending" // pending, accepted, completed
)
