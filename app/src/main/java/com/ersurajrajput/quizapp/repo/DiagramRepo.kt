package com.ersurajrajput.quizapp.repo

import GameModel
import com.ersurajrajput.quizapp.models.DiagramModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DiagramRepo {
    private val db = FirebaseFirestore.getInstance()
    private val diagramsCollection = db.collection("diagrams")


    fun getDiagramsList(onResult: (MutableList<DiagramModel>) -> Unit): ListenerRegistration {
        return diagramsCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                // Handle the error appropriately in your app
                return@addSnapshotListener
            }
            if (querySnapshot != null) {
                val dList = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(DiagramModel::class.java)?.apply { id = doc.id }
                }.toMutableList()
                onResult(dList)
            }
        }
    }
    fun saveDiagram(diagram: DiagramModel, onComplete: (Boolean) -> Unit) {
        val docRef = if (diagram.id.isBlank()) {
            diagramsCollection.document()
        } else {
            diagramsCollection.document(diagram.id)
        }

        val updatedDiagram = diagram.copy(id = docRef.id)

        docRef.set(updatedDiagram)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteDiagram(id: String, onComplete: (Boolean) -> Unit) {
        diagramsCollection.document(id).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


}