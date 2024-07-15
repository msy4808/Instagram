package com.moon.instagram.navigation.model

data class ContentDTO(var explain: String = "",
                      var imageUrl: String = "", var uid: String = "",
                      var userId: String = "",var timeStamp: Long = 0,
                      var favoriteCount: Int = 0,
                      var favorites: Map<String, Boolean> = HashMap()) {

    data class Comment(var uid: String = "",
                       var userId: String = "",
                       var comment: String = "",
                       var timeStamp: Long = 0) {

    }
}
