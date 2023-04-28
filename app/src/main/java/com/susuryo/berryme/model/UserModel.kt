package com.susuryo.berryme.model

class UserModel {
    var username: String? = null
    var profileImageUrl: String? = null
    var uid: String? = null
    var info: String? = null
    var email: String? = null
    var Pictures: HashMap<String, Picture>? = null

    class Picture {
        var picUid: String? = null
        var picUrl: String? = null
        var isPrivate: Boolean? = null
    }
}