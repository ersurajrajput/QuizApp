    package com.ersurajrajput.quizapp.repo

    import com.ersurajrajput.quizapp.models.DragAndDropModel
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.ListenerRegistration

    class DragAndDropRepo {

        private val db = FirebaseFirestore.getInstance()
        private val quizCollection = db.collection("dragAndDrop")

        /**
         * Listen for real-time updates of all Drag & Drop quizzes
         */
        fun getQuizList(onResult: (MutableList<DragAndDropModel>) -> Unit): ListenerRegistration {
            return quizCollection.addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val quizList = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(DragAndDropModel::class.java)?.apply { id = doc.id }
                    }.toMutableList()
                    onResult(quizList)
                }
            }
        }

        /**
         * Save or update a Drag & Drop quiz
         */
        fun saveQuiz(quiz: DragAndDropModel, onComplete: (Boolean, String?) -> Unit) {
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
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }

        /**
         * Edit an existing quiz by ID
         */
        fun editQuiz(id: String, quiz: DragAndDropModel, onComplete: (Boolean) -> Unit) {
            quizCollection.document(id).set(quiz)
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }

        /**
         * Get a single quiz by ID
         */
        fun getQuizById(quizId: String, onResult: (DragAndDropModel?) -> Unit) {
            quizCollection.document(quizId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val quiz = documentSnapshot.toObject(DragAndDropModel::class.java)?.apply {
                        id = documentSnapshot.id
                    }
                    onResult(quiz)
                }
                .addOnFailureListener {
                    onResult(null)
                }
        }
    }
