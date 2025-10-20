package com.ersurajrajput.quizapp.models

data class TrueFalseActivityModel(
    var id: String = "",
    var title: String = "",
    var desc: String = "",
    var questions: List<TrueFalseQuestion> = emptyList()
)

data class TrueFalseQuestion(
    var id: String = "",
    var text: String = "",
    var correctAnswer: Boolean =false
)
