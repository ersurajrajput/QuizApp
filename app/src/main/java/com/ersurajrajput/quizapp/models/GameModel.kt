data class GameAnswerOption(
    var id: String = "",
    var text: String = "",
    var correct: Boolean = false
)

data class GameQuestion(
    var id: String = "",
    var questionText: String = "",
    var options: List<GameAnswerOption> = emptyList()
)

data class GameModel(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var createdBy: String = "",
    var gameType: String = "",   // ← can be "SpellBee", "Quiz", etc.
    var questions: List<GameQuestion> = emptyList()
)
