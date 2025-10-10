package com.ersurajrajput.quizapp.models

class MCQActivityModel(
    var id:String = "",
    var title:String = "",
    var desc: String = "",
    var qList: List<Questions> = emptyList()
)

data class Questions(
    var id: String = "",
    var qTitle: String = "",
    var answerOption: List<AnswerOption> = emptyList()
)
data class AnswerOption(
    var id: String = "",
    var text: String = "",
    var correct: Boolean = false
)