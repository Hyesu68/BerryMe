package com.susuryo.berryme.model

class PictureModel {
    var profileImageUrl: String? = null
    var uid: String? = null
    var username: String? = null
    var pictureImageUrl: String? = null
    var timestamp : Any? = null
    var value : String? = null
    var pictureKey: String? = null
    var Likes: HashMap<String, Boolean>? = null
    var comment = HashMap<String, Comments>()
    var likesNum: Int? = null

    class Comments {
        var uid: String? = null
        var username: String? = null
        var timestamp: Any? = null
        var value: String? = null
        var key: String? = null
    }
}