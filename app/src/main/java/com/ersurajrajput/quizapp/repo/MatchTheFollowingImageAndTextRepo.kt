package com.ersurajrajput.quizapp.repo

import com.ersurajrajput.quizapp.models.MatchTheFollowingImageAndTextModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MatchTheFollowingImageAndTextRepo {

    private val db = FirebaseFirestore.getInstance()
    private val quizCollection = db.collection("matchTheFollowingTextAndImage")

    /**
     * Listen for real-time updates of all text-image-based quizzes
     */
    fun getQuizList(onResult: (MutableList<MatchTheFollowingImageAndTextModel>) -> Unit): ListenerRegistration {
        return quizCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) return@addSnapshotListener

            if (querySnapshot != null) {
                val quizList = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(MatchTheFollowingImageAndTextModel::class.java)?.apply { id = doc.id }
                }.toMutableList()
                onResult(quizList)
            }
        }
    }

    /**
     * Save or update a quiz (auto-generates ID if new)
     */
    fun saveQuiz(
        quiz: MatchTheFollowingImageAndTextModel,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docRef = if (quiz.id.isBlank()) quizCollection.document() else quizCollection.document(quiz.id)
        quiz.id = docRef.id

        docRef.set(quiz)
            .addOnSuccessListener { onComplete(true, quiz.id) }
            .addOnFailureListener { onComplete(false, null) }
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
     * Edit a quiz (replace existing data)
     */
    fun editQuiz(
        id: String,
        quiz: MatchTheFollowingImageAndTextModel,
        onComplete: (Boolean) -> Unit
    ) {
        quizCollection.document(id).set(quiz)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Get a single quiz by its ID
     */
    fun getQuizById(
        quizId: String,
        onResult: (MatchTheFollowingImageAndTextModel?) -> Unit
    ) {
        quizCollection.document(quizId).get()
            .addOnSuccessListener { document ->
                val quiz = document.toObject(MatchTheFollowingImageAndTextModel::class.java)
                    ?.apply { id = document.id }
                onResult(quiz)
            }
            .addOnFailureListener { onResult(null) }
    }
}
