package com.example.projetmobile.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    @DocumentId
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var exchangeFor: String = "",
    var category: String = "",
    var images: List<String> = listOf(),
    var userId: String = "",
    var status: String = "available", // Ce champ manquait
    var createdAt: Timestamp = Timestamp.now(), // Ce champ avait un type différent
    var updatedAt: Timestamp = Timestamp.now() // Ce champ manquait
) : Parcelable