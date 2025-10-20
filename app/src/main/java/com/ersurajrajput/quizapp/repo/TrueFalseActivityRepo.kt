package com.ersurajrajput.quizapp.repo

import com.ersurajrajput.quizapp.models.TrueFalseActivityModel
import com.ersurajrajput.quizapp.models.TrueFalseQuestion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TrueFalseActivityRepo {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("truefalse")

    // CREATE: Add a new activity
    fun addActivity(activity: TrueFalseActivityModel, onComplete: (Boolean) -> Unit) {
        collection.document(activity.id)
            .set(activity)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // READ: Get all activities
    fun getAllActivities(onComplete: (List<TrueFalseActivityModel>) -> Unit) {
        collection.get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(TrueFalseActivityModel::class.java) }
                onComplete(list)
            }
            .addOnFailureListener { onComplete(emptyList()) }
    }

    // READ: Get activity by ID
    fun getActivityById(id: String, onComplete: (TrueFalseActivityModel?) -> Unit) {
        collection.document(id).get()
            .addOnSuccessListener { doc ->
                val activity = doc.toObject(TrueFalseActivityModel::class.java)
                onComplete(activity)
            }
            .addOnFailureListener { onComplete(null) }
    }

    // UPDATE: Update an activity
    fun updateActivity(activity: TrueFalseActivityModel, onComplete: (Boolean) -> Unit) {
        collection.document(activity.id)
            .set(activity, SetOptions.merge())
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // DELETE: Delete activity by ID
    fun deleteActivity(id: String, onComplete: (Boolean) -> Unit) {
        collection.document(id)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ADD question to an activity
    fun addQuestion(activityId: String, question: TrueFalseQuestion, onComplete: (Boolean) -> Unit) {
        getActivityById(activityId) { activity ->
            if (activity != null) {
                val updatedQuestions = activity.questions.toMutableList()
                updatedQuestions.add(question)
                updateActivity(activity.copy(questions = updatedQuestions), onComplete)
            } else {
                onComplete(false)
            }
        }
    }

    // DELETE question from an activity
    fun deleteQuestion(activityId: String, questionId: String, onComplete: (Boolean) -> Unit) {
        getActivityById(activityId) { activity ->
            if (activity != null) {
                val updatedQuestions = activity.questions.filter { it.id != questionId }
                updateActivity(activity.copy(questions = updatedQuestions), onComplete)
            } else {
                onComplete(false)
            }
        }
    }
}
