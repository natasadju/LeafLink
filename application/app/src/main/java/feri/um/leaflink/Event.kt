package feri.um.leaflink

import org.bson.types.ObjectId

data class Event(
    val _id: String = ObjectId().toHexString(),
    val name: String,
    val parkId: String,
    val description: String,
    val date: String
)