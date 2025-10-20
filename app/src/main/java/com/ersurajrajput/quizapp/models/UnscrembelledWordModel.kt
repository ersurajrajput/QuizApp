package com.ersurajrajput.quizapp.models

data class UnscrembelledWordModel(
    var id: String = "",
    var title: String = "",
    var desc: String = "",
    var qustions: List<Question> = emptyList()

)
data class Question(
    var id: String = "",
    var hint: String = "",
    var word:String = "",
)