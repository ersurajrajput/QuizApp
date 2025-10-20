package com.ersurajrajput.quizapp.repo

import com.ersurajrajput.quizapp.models.MCQActivityModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MCQActivityRepo {

    private val db = FirebaseFirestore.getInstance()
    private val activityCollection = db.collection("mcqActivity")

    // 🔹 Add or Update MCQ Activity
    suspend fun addOrUpdateActivity(activity: MCQActivityModel): Boolean {
        return try {
            activityCollection.document(activity.id).set(activity).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 🔹 Get all MCQ Activities
    suspend fun getAllActivities(): List<MCQActivityModel> {
        return try {
            val snapshot = activityCollection.get().await()
            snapshot.toObjects(MCQActivityModel::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 🔹 Get specific MCQ Activity by ID
    suspend fun getActivityById(id: String): MCQActivityModel? {
        return try {
            val doc = activityCollection.document(id).get().await()
            doc.toObject(MCQActivityModel::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🔹 Delete MCQ Activity
    suspend fun deleteActivity(id: String): Boolean {
        return try {
            activityCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
