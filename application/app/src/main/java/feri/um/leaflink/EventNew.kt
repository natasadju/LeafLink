package feri.um.leaflink

import org.bson.types.ObjectId

data class EventNew (
    val _id: String = ObjectId().toHexString(),
    val name: String,
    val location: String,
    val description: String,
    val date: String
)