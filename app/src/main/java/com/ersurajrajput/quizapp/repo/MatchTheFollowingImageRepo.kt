package com.ersurajrajput.quizapp.repo

import com.ersurajrajput.quizapp.models.MatchTheFollowingImageModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MatchTheFollowingImageRepo {

    private val db = FirebaseFirestore.getInstance()
    private val quizCollection = db.collection("matchTheFollowingImage")

    /**
     * Listen for real-time updates of all image quizzes
     */
    fun getQuizList(onResult: (MutableList<MatchTheFollowingImageModel>) -> Unit): ListenerRegistration {
        return quizCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) return@addSnapshotListener

            if (querySnapshot != null) {
                val quizList = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(MatchTheFollowingImageModel::class.java)?.apply { id = doc.id }
                }.toMutableList()
                onResult(quizList)
            }
        }
    }

    /**
     * Save or update an image quiz
     */
    fun saveQuiz(quiz: MatchTheFollowingImageModel, onComplete: (Boolean, String?) -> Unit) {
        val docRef = if (quiz.id.isBlank()) {
            quizCollection.document()
        } else {
            quizCollection.document(quiz.id)
        }

        quiz.id = docRef.id

        docRef.set(quiz)
            .addOnSuccessListener { onComplete(true, quiz.id) }
            .addOnFailureListener { onComplete(false, null) }
    }

    /**
     * Delete an image quiz by ID
     */
    fun deleteQuiz(quizId: String, onComplete: (Boolean) -> Unit) {
        quizCollection.document(quizId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Edit an image quiz by ID
     */
    fun editQuiz(id: String, quiz: MatchTheFollowingImageModel, onComplete: (Boolean) -> Unit) {
        quizCollection.document(id).set(quiz)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Get a single image quiz by ID
     */
    fun getQuizById(quizId: String, onResult: (MatchTheFollowingImageModel?) -> Unit) {
        quizCollection.document(quizId).get()
            .addOnSuccessListener { documentSnapshot ->
                val quiz = documentSnapshot.toObject(MatchTheFollowingImageModel::class.java)?.apply { id = documentSnapshot.id }
                onResult(quiz)
            }
            .addOnFailureListener { onResult(null) }
    }
}
