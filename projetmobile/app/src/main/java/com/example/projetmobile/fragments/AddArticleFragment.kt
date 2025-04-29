package com.example.projetmobile.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.projetmobile.R
import com.example.projetmobile.models.Article
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddArticleFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etExchangeFor: EditText
    private lateinit var spinnerCategory: Spinner // Changé de EditText à Spinner
    private lateinit var btnAddArticle: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var imageArticle: ImageView
    private lateinit var btnSelectImage: Button
    private var selectedImageUri: Uri? = null
    private var selectedCategory: String = ""  // Nouvelle variable pour stocker la catégorie sélectionnée

    // Liste des catégories disponibles
    private val categories = listOf("Shoes", "Cars", "Watch", "Clothes", "Tech","Bag")

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    // Sélection d'image
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageArticle.setImageURI(it)
        }
    }
    // Permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openImagePicker()
        else Toast.makeText(context, "Permission refusée", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_article, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etTitle        = view.findViewById(R.id.etTitle)
        etDescription  = view.findViewById(R.id.etDescription)
        etPrice        = view.findViewById(R.id.etPrice)
        etExchangeFor  = view.findViewById(R.id.etExchangeFor)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)  // Changé pour le Spinner
        btnAddArticle  = view.findViewById(R.id.btnAddArticle)
        progressBar    = view.findViewById(R.id.progressBar)
        imageArticle   = view.findViewById(R.id.imageArticle)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)

        // Firebase
        auth       = FirebaseAuth.getInstance()
        db         = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        // Configuration du spinner
        setupCategorySpinner()

        btnSelectImage.setOnClickListener { checkPermissionAndOpenImagePicker() }
        btnAddArticle.setOnClickListener { addArticle() }
    }

    private fun setupCategorySpinner() {
        // Créer un adaptateur pour le spinner avec la liste des catégories
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Appliquer l'adaptateur au Spinner
        spinnerCategory.adapter = adapter

        // Définir un écouteur pour capturer la sélection de catégorie
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Rien à faire ici
            }
        }
    }

    private fun checkPermissionAndOpenImagePicker() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), perm) == PackageManager.PERMISSION_GRANTED ->
                openImagePicker()
            else ->
                requestPermissionLauncher.launch(perm)
        }
    }

    private fun openImagePicker() {
        getContent.launch("image/*")
    }

    private fun addArticle() {
        val title       = etTitle.text.toString().trim()
        val desc        = etDescription.text.toString().trim()
        val priceStr    = etPrice.text.toString().trim()
        val exchangeFor = etExchangeFor.text.toString().trim()

        // Utiliser la catégorie sélectionnée dans le spinner
        val category    = selectedCategory

        // Validation
        if (title.isEmpty()) {
            etTitle.error = "Veuillez entrer un titre"; return
        }
        if (desc.isEmpty()) {
            etDescription.error = "Veuillez entrer une description"; return
        }
        if (priceStr.isEmpty() && exchangeFor.isEmpty()) {
            etPrice.error = "Prix ou échange requis"
            etExchangeFor.error = "Prix ou échange requis"
            return
        }
        if (category.isEmpty()) {
            Toast.makeText(context, "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Vous devez être connecté", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnAddArticle.isEnabled = false

        if (selectedImageUri != null) {
            uploadImageAndSaveArticle(title, desc, price, exchangeFor, category, currentUser.uid)
        } else {
            saveArticle(title, desc, price, exchangeFor, category, currentUser.uid, emptyList())
        }
    }

    private fun uploadImageAndSaveArticle(
        title: String, desc: String, price: Double,
        exchangeFor: String, category: String, userId: String
    ) {
        val fileRef = storageRef.child("articles/${UUID.randomUUID()}.jpg")
        fileRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    saveArticle(title, desc, price, exchangeFor, category, userId, listOf(uri.toString()))
                }.addOnFailureListener { e ->
                    onError("Téléchargement URL", e)
                }
            }
            .addOnFailureListener { e ->
                onError("Upload image", e)
            }
    }

    private fun saveArticle(
        title: String, desc: String, price: Double,
        exchangeFor: String, category: String, userId: String,
        imageUrls: List<String>
    ) {
        val now = Timestamp.now()
        val article = Article(
            userId     = userId,
            title      = title,
            description= desc,
            price      = price,
            exchangeFor= exchangeFor,
            category   = category,
            images     = imageUrls,
            status     = "available",
            createdAt  = now,
            updatedAt  = now
        )

        db.collection("articles")
            .add(article)
            .addOnSuccessListener { ref ->
                Log.d("FirestoreDebug", "Article ajouté ID=${ref.id}")
                resetForm()
                Toast.makeText(context, "Article publié ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                onError("Enregistrement article", e)
            }
    }

    private fun resetForm() {
        progressBar.visibility = View.GONE
        btnAddArticle.isEnabled = true
        etTitle.text.clear()
        etDescription.text.clear()
        etPrice.text.clear()
        etExchangeFor.text.clear()
        spinnerCategory.setSelection(0)  // Réinitialiser le spinner
        imageArticle.setImageResource(R.drawable.ic_image_placeholder)
        selectedImageUri = null
    }

    private fun onError(step: String, e: Exception) {
        Log.e("FirestoreDebug", "Erreur $step", e)
        progressBar.visibility = View.GONE
        btnAddArticle.isEnabled = true
        Toast.makeText(context, "Erreur $step: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}