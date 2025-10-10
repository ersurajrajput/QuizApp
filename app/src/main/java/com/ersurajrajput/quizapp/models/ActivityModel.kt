sealed class ActivityQuestion {
    abstract val id: String
    abstract val type: String
    abstract val question: String
}

data class McqQuestion(
    override val id: String,
    override val type: String = "MCQ",
    override val question: String,
    val options: List<String>,
    val answer: String
) : ActivityQuestion()

data class MatchQuestion(
    override val id: String,
    override val type: String = "MATCH",
    override val question: String,
    val pairs: List<Pair<String, String>>
) : ActivityQuestion()

data class TrueFalseQuestion(
    override val id: String,
    override val type: String = "TRUE_FALSE",
    override val question: String,
    val answer: Boolean
) : ActivityQuestion()

data class FillBlankQuestion(
    override val id: String,
    override val type: String = "FILL_BLANK",
    override val question: String,
    val correctAnswers: List<String>
) : ActivityQuestion()
