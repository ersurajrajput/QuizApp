package com.ersurajrajput.quizapp

import android.app.Application
import android.util.Log
import com.cloudinary.android.MediaManager
import com.ersurajrajput.quizapp.models.SecretsModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore

class MyApp: Application() {
    companion object {
        var isCloudinaryInitialized = false
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        if (!isCloudinaryInitialized) {
            val db = FirebaseFirestore.getInstance()
            val secCollection = db.collection("sec")

            secCollection.document("sec").get()
                .addOnSuccessListener { document ->
                    val secrets = document.toObject(SecretsModel::class.java)

                    if (secrets != null && secrets.uName.isNotEmpty() &&
                        secrets.key.isNotEmpty() && secrets.sec.isNotEmpty()
                    ) {
                        val config = mapOf(
                            "cloud_name" to secrets.uName,
                            "api_key" to secrets.key,
                            "api_secret" to secrets.sec
                        )
                        MediaManager.init(this, config)
                        isCloudinaryInitialized = true
                        Log.d("Cloudinary", "Initialized successfully!")
                    } else {
                        Log.e("Cloudinary", "Secrets are missing or incomplete in Firestore")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Cloudinary", "Failed to fetch Cloudinary secrets", e)
                }


        }
    }
}
