package com.absolutefresh.sales.data.model

enum class UserRole {
    REP,
    DRIVER
}

enum class StaffStatus {
    ACTIVE,
    INACTIVE,
    PENDING_APPROVAL
}

data class User(
    val id: String,
    val identifier: String,
    val name: String,
    val role: UserRole,
    val status: StaffStatus = StaffStatus.ACTIVE,
    val area: String = "Lucknow East"
)
