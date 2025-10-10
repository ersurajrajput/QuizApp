package com.ersurajrajput.quizapp.models

class FillInTheBlanksModel(
    var id: String = "",
    var title: String = "",
    var desc: String = "",
    var questions: List<FillInTheBlanksQuestions> = emptyList()
)
data class FillInTheBlanksQuestions(
    var id: String = "",
    var text: String = "",
    var ans:String = ""
)