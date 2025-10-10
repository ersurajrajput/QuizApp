package com.ersurajrajput.quizapp.models

class DictionaryModel(
    var id: String = "",
    var title: String = "",
    var desc: String = "",
    var vocabularyWord: List<VocabularyWord> = emptyList()
)
data class VocabularyWord(var word: String="", var definition: String="", var imageUrl: String="")
