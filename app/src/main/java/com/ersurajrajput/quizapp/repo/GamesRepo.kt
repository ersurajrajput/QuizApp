package com.ersurajrajput.quizapp.repo

import GameModel
import com.ersurajrajput.quizapp.models.MyMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class GamesRepo {

    private val db = FirebaseFirestore.getInstance()
    private val gamesCollection = db.collection("games")

    /**
     * Save a game to Firestore
     */
    fun getGameList(onResult: (MutableList<GameModel>) -> Unit): ListenerRegistration {
        return gamesCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                // Handle the error appropriately in your app
                return@addSnapshotListener
            }
            if (querySnapshot != null) {
                val gameList = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(GameModel::class.java)?.apply { id = doc.id }
                }.toMutableList()
                onResult(gameList)
            }
        }
    }
    fun saveGame(game: GameModel, onComplete: (Boolean, String?) -> Unit) {
        val docRef = if (game.id.isBlank()) {
            gamesCollection.document()
        } else {
            gamesCollection.document(game.id)
        }

        game.id = docRef.id

        docRef.set(game)
            .addOnSuccessListener {
                onComplete(true, game.id)
            }
            .addOnFailureListener { e ->
                onComplete(false, null)
            }
    }
    fun deleteGame(gameId: String, onComplete: (Boolean) -> Unit) {
        gamesCollection.document(gameId).delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                onComplete(false)
            }
    }
    fun editGame(id: String, game: GameModel, onComplete: (Boolean) -> Unit) {
        gamesCollection.document(id).set(game)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                onComplete(false)
            }
    }
    fun getGameById(gameId: String, onResult: (GameModel?) -> Unit) {
        gamesCollection.document(gameId).get()
            .addOnSuccessListener { documentSnapshot ->
                val game = documentSnapshot.toObject(GameModel::class.java)?.apply { id = documentSnapshot.id }
                onResult(game)
            }
            .addOnFailureListener { e ->
                onResult(null)
            }
    }

}
