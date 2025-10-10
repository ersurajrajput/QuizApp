package com.ersurajrajput.quizapp.repo


import com.ersurajrajput.quizapp.models.FillInTheBlanksModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FillInTheBlanksRepo {

    private val db = FirebaseFirestore.getInstance()
    private val quizCollection = db.collection("fillInTheBlanks")

    /**
     * Get a real-time list of quizzes from Firestore.
     * Returns a ListenerRegistration to allow detaching the listener later.
     */
    fun getQuizList(onResult: (MutableList<FillInTheBlanksModel>) -> Unit): ListenerRegistration {
        return quizCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                // In a real app, handle this error (e.g., log it or show a message)
                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                val quizList = querySnapshot.documents.mapNotNull { doc ->
                    // Convert document to a FillInTheBlanksModel and set its ID
                    doc.toObject(FillInTheBlanksModel::class.java)?.apply { id = doc.id }
                }.toMutableList()
                onResult(quizList)
            }
        }
    }

    /**
     * Save a new quiz or update an existing one in Firestore.
     * If the quiz model's ID is blank, a new document is created.
     */
    fun saveQuiz(quiz: FillInTheBlanksModel, onComplete: (Boolean, String?) -> Unit) {
        val docRef = if (quiz.id.isBlank()) {
            // Create a new document reference if ID is missing
            quizCollection.document()
        } else {
            // Use the existing document reference
            quizCollection.document(quiz.id)
        }

        // Ensure the model's ID matches the document ID
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
     * Delete a quiz from Firestore using its document ID.
     */
    fun deleteQuiz(quizId: String, onComplete: (Boolean) -> Unit) {
        quizCollection.document(quizId).delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    /**
     * Edit/overwrite a quiz in Firestore with new data.
     */
    fun editQuiz(id: String, quiz: FillInTheBlanksModel, onComplete: (Boolean) -> Unit) {
        quizCollection.document(id).set(quiz)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    /**
     * Get a single quiz by its document ID from Firestore.
     */
    fun getQuizById(quizId: String, onResult: (FillInTheBlanksModel?) -> Unit) {
        quizCollection.document(quizId).get()
            .addOnSuccessListener { documentSnapshot ->
                val quiz = documentSnapshot.toObject(FillInTheBlanksModel::class.java)?.apply { id = documentSnapshot.id }
                onResult(quiz)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}