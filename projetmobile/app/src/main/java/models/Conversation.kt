package com.example.projetmobile.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Conversation(
    @DocumentId
    var id: String = "",
    var participants: List<String> = listOf(),
    var lastMessage: String = "",
    var lastTimestamp: Timestamp? = null,
    var articleId: String = "",
    var articleTitle: String = ""
)