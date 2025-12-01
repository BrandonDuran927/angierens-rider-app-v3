package com.brandon.angierens_rider.authentication.data.mappers

import com.brandon.angierens_rider.authentication.data.respond.UserDto
import com.brandon.angierens_rider.authentication.domain.User

fun UserDto.toDomain() : User {
    return User(
        user_uid = user_uid,
        birth_date = birth_date,
        date_hired = date_hired,
        email = email,
        first_name = first_name,
        gender = gender,
        is_active = is_active,
        last_name = last_name,
        middle_name = middle_name,
        other_contact = other_contact,
        phone_number = phone_number,
        user_role = user_role
    )
}