package com.brandon.angierens_rider.task.data.mappers

import com.brandon.angierens_rider.task.data.remote.respond.AddOnDto
import com.brandon.angierens_rider.task.data.remote.respond.AddressDto
import com.brandon.angierens_rider.task.data.remote.respond.CustomerDto
import com.brandon.angierens_rider.task.data.remote.respond.DeliveryDto
import com.brandon.angierens_rider.task.data.remote.respond.MenuDto
import com.brandon.angierens_rider.task.data.remote.respond.OrderDto
import com.brandon.angierens_rider.task.data.remote.respond.OrderItemAddOnDto
import com.brandon.angierens_rider.task.data.remote.respond.OrderItemDto
import com.brandon.angierens_rider.task.domain.model.AddOn
import com.brandon.angierens_rider.task.domain.model.Address
import com.brandon.angierens_rider.task.domain.model.Customer
import com.brandon.angierens_rider.task.domain.model.Delivery
import com.brandon.angierens_rider.task.domain.model.Menu
import com.brandon.angierens_rider.task.domain.model.Order
import com.brandon.angierens_rider.task.domain.model.OrderItem
import com.brandon.angierens_rider.task.domain.model.OrderItemAddOn

fun DeliveryDto.toDomain(): Delivery {
    return Delivery(
        addressId = address_id,
        createdAt = created_at,
        deliveryFee = delivery_fee,
        deliveryId = delivery_id,
        deliveryTime = delivery_time,
        riderId = rider_id,
        deliveryStatus = delivery_status,
        orders = order?.map { it.toDomain() } ?: emptyList(),
        address = address?.toDomain()
    )
}

fun OrderDto.toDomain(): Order {
    return Order(
        orderId = order_id,
        additionalInformation = additional_information,
        completedDate = completed_date,
        createdAt = created_at,
        customerUid = customer_uid,
        deliveryId = delivery_id,
        failedDeliveryReason = failed_delivery_reason,
        orderCooked = order_cooked,
        orderNumber = order_number,
        orderStatus = order_status,
        orderType = order_type,
        paymentId = payment_id,
        scheduleId = schedule_id,
        statusUpdatedAt = status_updated_at,
        totalPrice = total_price,
        items = items?.map { it.toDomain() } ?: emptyList(),
        customer = customer?.toDomain()
    )
}

fun OrderItemDto.toDomain(): OrderItem {
    return OrderItem(
        orderItemId = order_item_id,
        orderId = order_id,
        menuId = menu_id,
        quantity = quantity,
        subtotalPrice = subtotal_price,
        isCompleted = is_completed,
        addOns = addOns?.map { it.toDomain() } ?: emptyList(),
        menu = menu?.toDomain()
    )
}

fun OrderItemAddOnDto.toDomain(): OrderItemAddOn {
    return OrderItemAddOn(
        orderItemAddOnId = order_item_add_on_id,
        orderItemId = order_item_id,
        addOnId = add_on_id,
        quantity = quantity,
        subtotalPrice = subtotal_price,
        isCompleted = is_completed,
        addOn = addOn?.toDomain()
    )
}

fun MenuDto.toDomain(): Menu {
    return Menu(
        menuId = menu_id,
        name = name,
        inclusion = inclusion,
        description = description,
        price = price,
        isAvailable = is_available,
        category = category,
        size = size,
        imageUrl = image_url
    )
}

fun AddOnDto.toDomain(): AddOn {
    return AddOn(
        addOnId = add_on,
        name = name,
        price = price
    )
}

fun AddressDto.toDomain(): Address {
    return Address(
        addressId = address_id,
        addressType = address_type,
        addressLine = address_line,
        region = region,
        city = city,
        barangay = barangay,
        postalCode = postal_code,
        customerId = customer_id,
        latitude = latitude,
        longitude = longitude
    )
}


fun CustomerDto.toDomain(): Customer {
    return Customer(
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
