package com.ersurajrajput.quizapp.screens.student.activity.studentplayer

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.models.FillInTheBlanksModel
import com.ersurajrajput.quizapp.models.FillInTheBlanksQuestions
import com.ersurajrajput.quizapp.repo.FillInTheBlanksRepo

class FillIntheBlankActivity : AppCompatActivity() {

    private val repo = FillInTheBlanksRepo()
    private var activityModel: FillInTheBlanksModel? = null
    private var currentPage = 0
    private var score = 0
    private val questionsPerPage = 4

    // UI references
    private lateinit var tvQuestionTitle: TextView
    private lateinit var questionRows: List<LinearLayout>
    private lateinit var frontTextViews: List<TextView>
    private lateinit var endTextViews: List<TextView>
    private lateinit var answerEditTexts: List<EditText>
    private lateinit var optionTextViews: List<TextView>
    private var optionsContainer: LinearLayout? = null // MODIFIED: Changed to nullable to prevent crash
    private lateinit var submitButton: AppCompatButton
    private lateinit var nextButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_fill_inthe_blank)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        val activityId = intent.getStringExtra("ID")
        if (activityId == null) {
            Toast.makeText(this, "Activity ID not provided", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadQuiz(activityId)

        submitButton.setOnClickListener { checkAnswers() }
        nextButton.setOnClickListener { nextPage() }
        backBtn()
    }

    fun backBtn(){
        val btn = findViewById<ImageView>(R.id.btnBack)
        btn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initViews() {
        tvQuestionTitle = findViewById(R.id.tvQuestionTitle)
        submitButton = findViewById(R.id.submitButton)
        nextButton = findViewById(R.id.nextButton)

        questionRows = listOf(
            findViewById(R.id.row1),
            findViewById(R.id.row2),
            findViewById(R.id.row3),
            findViewById(R.id.row4)
        )
        frontTextViews = listOf(
            findViewById(R.id.tvFrontQ1),
            findViewById(R.id.tvFrontQ2),
            findViewById(R.id.tvFrontQ3),
            findViewById(R.id.tvFrontQ4)
        )
        endTextViews = listOf(
            findViewById(R.id.tvEndQ1),
            findViewById(R.id.tvEndQ2),
            findViewById(R.id.tvEndQ3),
            findViewById(R.id.tvEndQ4)
        )
        answerEditTexts = listOf(
            findViewById(R.id.etAnswer1),
            findViewById(R.id.etAnswer2),
            findViewById(R.id.etAnswer3),
            findViewById(R.id.etAnswer4)
        )

        optionsContainer = findViewById(R.id.optionsContainer)
        optionTextViews = listOf(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3),
            findViewById(R.id.option4)
        )


        answerEditTexts.forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    checkIfAllFieldsFilled()
                }
            })
        }
    }

    private fun loadQuiz(activityId: String) {
        repo.getQuizById(activityId) { model ->
            if (model != null) {
                activityModel = model
                displayCurrentPage()
            } else {
                Toast.makeText(this, "Failed to load quiz", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayCurrentPage() {
        val quiz = activityModel ?: return
        tvQuestionTitle.text = quiz.title

        val questionsOnPage = getCurrentPageQuestions()
        val correctAnswers = questionsOnPage.map { it.ans.trim() }.shuffled() // Get answers and shuffle them

        questionRows.forEach { it.visibility = View.GONE }
        optionTextViews.forEach { it.visibility = View.GONE }

        // Make the options container visible to show the word bank
        optionsContainer?.visibility = View.VISIBLE

        // Populate the option TextViews with shuffled answers (word bank)
        correctAnswers.forEachIndexed { index, answer ->
            if (index < optionTextViews.size) {
                optionTextViews[index].text = answer
                optionTextViews[index].visibility = View.VISIBLE
            }
        }

        questionsOnPage.forEachIndexed { index, question ->
            questionRows[index].visibility = View.VISIBLE
            val parts = question.text.split("____")
            frontTextViews[index].text = parts.getOrNull(0) ?: ""
            endTextViews[index].text = if (parts.size > 1) parts[1] else ""

            answerEditTexts[index].apply {
                setText("")
                isEnabled = true
                backgroundTintList = null
            }
        }

        submitButton.isEnabled = false
        nextButton.isEnabled = false

        val totalPages = (quiz.questions.size + questionsPerPage - 1) / questionsPerPage
        if (currentPage >= totalPages - 1) {
            nextButton.text = "Finish"
        } else {
            nextButton.text = "Next"
        }
    }

    private fun checkIfAllFieldsFilled() {
        val questionsOnPage = getCurrentPageQuestions()
        if (questionsOnPage.isEmpty()) return

        val allFilled = answerEditTexts.take(questionsOnPage.size)
            .all { it.text.toString().trim().isNotEmpty() }

        submitButton.isEnabled = allFilled
    }

    private fun checkAnswers() {
        val questionsOnPage = getCurrentPageQuestions()
        if (questionsOnPage.isEmpty()) return

        var correctOnPage = 0
        questionsOnPage.forEachIndexed { index, question ->
            val userAnswer = answerEditTexts[index].text.toString().trim()
            val correctAnswer = question.ans.trim()

            answerEditTexts[index].isEnabled = false
            if (userAnswer.equals(correctAnswer, ignoreCase = true)) {
                score++
                correctOnPage++
                answerEditTexts[index].backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            } else {
                answerEditTexts[index].backgroundTintList = ColorStateList.valueOf(Color.RED)
            }
        }

        submitButton.isEnabled = false
        nextButton.isEnabled = true

        val allCorrect = correctOnPage == questionsOnPage.size
        showResultDialogAndPlaySound(allCorrect)
    }

    private fun showResultDialogAndPlaySound(allCorrect: Boolean) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val layout = FrameLayout(this).apply {
            setBackgroundResource(R.drawable.popup_bg)
            setPadding(100, 100, 100, 100)
        }

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = if (allCorrect) "Excellent!" else "Keep Trying!"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        val msg = TextView(this).apply {
            text = if (allCorrect) "You got all answers correct!" else "You can do better!"
            textSize = 18f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        textLayout.addView(title)
        textLayout.addView(msg)
        layout.addView(textLayout)

        dialog.setContentView(layout)

        val soundResId = if (allCorrect) R.raw.excellent else R.raw.common_u_can_do_batter_than_that
        playSound(soundResId, dialog)
        dialog.show()
    }

    private fun playSound(soundResourceId: Int, dialog: Dialog) {
        try {
            val mediaPlayer = MediaPlayer.create(this, soundResourceId)
            mediaPlayer?.setOnCompletionListener { mp ->
                dialog.dismiss()
                mp.release()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            dialog.dismiss()
        }
    }

    private fun nextPage() {
        val totalQuestions = activityModel?.questions?.size ?: 0
        val totalPages = (totalQuestions + questionsPerPage - 1) / questionsPerPage

        currentPage++
        if (currentPage < totalPages) {
            displayCurrentPage()
        } else {
            showFinalScore()
        }
    }

    private fun showFinalScore() {
        val totalQuestions = activityModel?.questions?.size ?: 0
        AlertDialog.Builder(this)
            .setTitle("Quiz Complete!")
            .setMessage("Your final score is: $score / $totalQuestions")
            .setPositiveButton("Finish") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun getCurrentPageQuestions(): List<FillInTheBlanksQuestions> {
        val questions = activityModel?.questions ?: return emptyList()
        val start = currentPage * questionsPerPage
        val end = minOf(start + questionsPerPage, questions.size)
        return if (start < end) questions.subList(start, end) else emptyList()
    }
}

