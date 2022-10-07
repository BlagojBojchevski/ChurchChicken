package com.tts.gueststar.models

data class LoyaltyHistory(
    val restaurant_id: Int? = 0,
    val date: Long,
    val points: Int? = 0,
    var name: String,
    val subtotal: Double? = 0.0,
    val total_points_earned: Int? = 0,
    val activity_type: String,
    val amount: Double? = 0.0
)