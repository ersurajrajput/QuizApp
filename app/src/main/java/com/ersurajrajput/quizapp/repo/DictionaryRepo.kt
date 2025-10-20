package com.ersurajrajput.quizapp.repo

import com.ersurajrajput.quizapp.models.DictionaryModel
import com.ersurajrajput.quizapp.models.VocabularyWord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DictionaryRepo {

    private val db = FirebaseFirestore.getInstance()
    private val dictionaryCollection = db.collection("dictionary")

    /**
     * Get all dictionaries with real-time updates
     */
    fun getDictionaryList(onResult: (MutableList<DictionaryModel>) -> Unit): ListenerRegistration {
        return dictionaryCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) return@addSnapshotListener
            if (querySnapshot != null) {
                val list = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(DictionaryModel::class.java)?.apply { id = doc.id }
                }.toMutableList()
                onResult(list)
            }
        }
    }

    /**
     * Save a dictionary (create or update)
     */
    fun saveDictionary(dictionary: DictionaryModel, onComplete: (Boolean, String?) -> Unit) {
        val docRef = if (dictionary.id.isBlank()) {
            dictionaryCollection.document()
        } else {
            dictionaryCollection.document(dictionary.id)
        }

        dictionary.id = docRef.id

        docRef.set(dictionary)
            .addOnSuccessListener { onComplete(true, dictionary.id) }
            .addOnFailureListener { onComplete(false, null) }
    }

    /**
     * Delete a dictionary by ID
     */
    fun deleteDictionary(dictionaryId: String, onComplete: (Boolean) -> Unit) {
        dictionaryCollection.document(dictionaryId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Update an existing dictionary completely
     */
    fun editDictionary(dictionaryId: String, dictionary: DictionaryModel, onComplete: (Boolean) -> Unit) {
        dictionaryCollection.document(dictionaryId).set(dictionary)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Get a dictionary by ID
     */
    fun getDictionaryById(dictionaryId: String, onResult: (DictionaryModel?) -> Unit) {
        dictionaryCollection.document(dictionaryId).get()
            .addOnSuccessListener { doc ->
                val dictionary = doc.toObject(DictionaryModel::class.java)?.apply { id = doc.id }
                onResult(dictionary)
            }
            .addOnFailureListener { onResult(null) }
    }

    /**
     * Add a word to an existing dictionary
     */
    fun addWordToDictionary(dictionaryId: String, word: VocabularyWord, onComplete: (Boolean) -> Unit) {
        val docRef = dictionaryCollection.document(dictionaryId)
        docRef.get()
            .addOnSuccessListener { doc ->
                val currentWords = doc.toObject(DictionaryModel::class.java)?.vocabularyWord?.toMutableList()
                    ?: mutableListOf()
                currentWords.add(word)
                docRef.update("vocabularyWord", currentWords)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Remove a word from a dictionary by word string
     */
    fun removeWordFromDictionary(dictionaryId: String, wordString: String, onComplete: (Boolean) -> Unit) {
        val docRef = dictionaryCollection.document(dictionaryId)
        docRef.get()
            .addOnSuccessListener { doc ->
                val currentWords = doc.toObject(DictionaryModel::class.java)?.vocabularyWord?.toMutableList()
                    ?: mutableListOf()
                val updatedWords = currentWords.filter { it.word != wordString }
                docRef.update("vocabularyWord", updatedWords)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }
}
