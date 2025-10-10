package com.ersurajrajput.quizapp.repo

import com.ersurajrajput.quizapp.models.ImageMCQModel
import com.google.firebase.firestore.FirebaseFirestore

class ImageMcqRepo {

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("imageMcq")

    // 1. Get all activities
    fun getAllActivities(onComplete: (List<ImageMCQModel>) -> Unit) {
        collectionRef.get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ImageMCQModel::class.java)?.apply { id = doc.id }
                }
                onComplete(list)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    // 2. Get single activity by ID
    fun getActivityById(id: String, onComplete: (ImageMCQModel?) -> Unit) {
        collectionRef.document(id).get()
            .addOnSuccessListener { doc ->
                val activity = doc.toObject(ImageMCQModel::class.java)?.apply { this.id = doc.id }
                onComplete(activity)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    // 3. Add new activity
    fun addActivity(activity: ImageMCQModel, onComplete: (Boolean) -> Unit) {
        collectionRef.add(activity)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // 4. Update existing activity
    fun updateActivity(activity: ImageMCQModel, onComplete: (Boolean) -> Unit) {
        if (activity.id.isEmpty()) {
            onComplete(false)
            return
        }
        collectionRef.document(activity.id).set(activity)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // 5. Delete activity
    fun deleteActivity(id: String, onComplete: (Boolean) -> Unit) {
        collectionRef.document(id).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
