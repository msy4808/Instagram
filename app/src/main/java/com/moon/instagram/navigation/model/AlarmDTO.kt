package com.moon.instagram.navigation.model

data class AlarmDTO(
    var destinationUid: String = "",
    var userId: String = "",
    var uid: String = "",
    var kind: Int = 0,
    var message: String = "",
    var timeStamp: Long = 0
)
