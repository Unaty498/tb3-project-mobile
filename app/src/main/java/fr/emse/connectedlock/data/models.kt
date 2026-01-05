package fr.emse.connectedlock.data

data class User(val id: String, val name: String, val email: String)

data class Badge(val id: String, val type: String, val expiryDate: String)

data class Door(val id: String, val name: String, val location: String)
