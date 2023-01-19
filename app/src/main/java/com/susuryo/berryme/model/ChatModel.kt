package com.susuryo.berryme.model

class ChatModel {
    var users: Map<String, Boolean> = HashMap() //채팅방 유저
    var comments: Map<String, Comment> = HashMap() //채팅방 내용

    class Comment {
        var uid: String? = null
        var message: String? = null
        var timestamp: Any? = null
    }
}
