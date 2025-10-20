package com.ersurajrajput.quizapp.repo

import com.ersurajrajput.quizapp.models.MatchTheFollowingModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MatchTheFollowingRepo {

    private val db = FirebaseFirestore.getInstance()
    private val quizCollection = db.collection("matchTheFollowing")

    /**
     * Listen for real-time updates of all quizzes
     */
    fun getQuizList(onResult: (MutableList<MatchTheFollowingModel>) -> Unit): ListenerRegistration {
        return quizCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                // Handle the error appropriately
                return@addSnapshotListener
            }
            if (querySnapshot != null) {
                val quizList = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(MatchTheFollowingModel::class.java)?.apply { id = doc.id }
                }.toMutableList()
                onResult(quizList)
            }
        }
    }

    /**
     * Save or update a quiz
     */
    fun saveQuiz(quiz: MatchTheFollowingModel, onComplete: (Boolean, String?) -> Unit) {
        val docRef = if (quiz.id.isBlank()) {
            quizCollection.document()
        } else {
            quizCollection.document(quiz.id)
        }

        quiz.id = docRef.id

        docRef.set(quiz)
            .addOnSuccessListener {
                onComplete(true, quiz.id)
            }
            .addOnFailureListener {
                onComplete(false, null)
            }
    }

    /**
     * Delete a quiz by ID
     */
    fun deleteQuiz(quizId: String, onComplete: (Boolean) -> Unit) {
        quizCollection.document(quizId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Edit a quiz by ID
     */
    fun editQuiz(id: String, quiz: MatchTheFollowingModel, onComplete: (Boolean) -> Unit) {
        quizCollection.document(id).set(quiz)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Get a single quiz by ID
     */
    fun getQuizById(quizId: String, onResult: (MatchTheFollowingModel?) -> Unit) {
        quizCollection.document(quizId).get()
            .addOnSuccessListener { documentSnapshot ->
                val quiz = documentSnapshot.toObject(MatchTheFollowingModel::class.java)?.apply { id = documentSnapshot.id }
                onResult(quiz)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
