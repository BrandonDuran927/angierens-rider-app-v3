package com.brandon.angierens_rider.task.domain.model

data class Customer(
    val user_uid: String,
    val birth_date: String?,
    val date_hired: String?,
    val email: String,
    val first_name: String,
    val gender: String,
    val is_active: Boolean,
    val last_name: String,
    val middle_name: String?,
    val other_contact: String?,
    val phone_number: String,
    val user_role: String
)