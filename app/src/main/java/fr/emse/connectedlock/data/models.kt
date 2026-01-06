package fr.emse.connectedlock.data

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String? = "",
    val phone: String? = "",
    val role: String = "",
    val active: Boolean = false
)

data class Badge(
    val id: String = "",
    val badgeNumber: String = "",
    val type: String = "",
    val userId: String = "",
    val expiryDate: String = "",
    val active: Boolean = false,
    val physicallyMapped: Boolean = false
)

data class Door(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val deviceId: String? = "",
    val active: Boolean = false
)
