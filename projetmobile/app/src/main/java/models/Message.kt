package com.example.projetmobile.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId
    var id: String = "",
    var conversationId: String = "",
    var senderId: String = "",
    var content: String = "",
    var timestamp: Timestamp? = null,
    var read: Boolean = false
)