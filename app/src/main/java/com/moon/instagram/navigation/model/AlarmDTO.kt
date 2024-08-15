package com.moon.instagram.navigation.model

data class AlarmDTO(
    var destinationUid: String = "",
    var userId: String = "",
    var uid: String = "",
    //0 : like alarm
    //1 : comment alarm
    //2 : follow alarm
    var kind: Int? = null,
    var message: String = "",
    var timeStamp: Long = 0
)
