package com.ersurajrajput.quizapp.screens.student.activity.studentplayer

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.models.TrueFalseActivityModel
import com.ersurajrajput.quizapp.models.TrueFalseQuestion
import com.ersurajrajput.quizapp.repo.TrueFalseActivityRepo

class TrueFalseActivity : AppCompatActivity() {

    private val repo = TrueFalseActivityRepo()
    private var activityModel: TrueFalseActivityModel? = null
    private var currentPage = 0
    private var score = 0
    private val questionsPerPage = 4

    // UI Components
    private lateinit var tvDesc: TextView
    private lateinit var questionTextViews: List<TextView>
    private lateinit var questionRadioGroups: List<RadioGroup>
    private lateinit var submitButton: AppCompatButton
    private lateinit var nextButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        setContentView(R.layout.activity_true_false)

        initializeViews()

        val activityId = intent.getStringExtra("ID") ?: "85952ab3-bc9e-49e9-be20-0ab12f687372"
        loadQuizData(activityId)

        submitButton.setOnClickListener { handleSubmit() }
        nextButton.setOnClickListener { showAnswers() }
        backBtn()
    }
    fun backBtn(){
        var btn = findViewById<ImageView>(R.id.btnBack)
        btn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun initializeViews() {
        tvDesc = findViewById(R.id.descriptionText)
        submitButton = findViewById(R.id.submitButton)
        nextButton = findViewById(R.id.nextButton)

        questionTextViews = listOf(
            findViewById(R.id.questionText1),
            findViewById(R.id.questionText2),
            findViewById(R.id.questionText3),
            findViewById(R.id.questionText4)
        )

        questionRadioGroups = listOf(
            findViewById(R.id.radioGroup1),
            findViewById(R.id.radioGroup2),
            findViewById(R.id.radioGroup3),
            findViewById(R.id.radioGroup4)
        )
    }

    private fun loadQuizData(activityId: String) {
        repo.getActivityById(activityId) { model ->
            if (model != null) {
                activityModel = model
                tvDesc.text = model.desc
                displayCurrentPage()
            } else {
                Toast.makeText(this, "Failed to load quiz.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayCurrentPage() {
        val questions = activityModel?.questions ?: return
        val startIndex = currentPage * questionsPerPage
        val endIndex = minOf(startIndex + questionsPerPage, questions.size)
        val questionsOnPage = questions.subList(startIndex, endIndex)

        // Reset UI
        questionTextViews.forEach { it.visibility = View.GONE }
        questionRadioGroups.forEach {
            it.setOnCheckedChangeListener(null) // Clear listener
            it.clearCheck()
            it.visibility = View.GONE
            for (i in 0 until it.childCount) {
                val radioButton = it.getChildAt(i) as RadioButton
                radioButton.isEnabled = true
                radioButton.setTextColor(Color.BLACK)
                radioButton.buttonTintList = null
            }
        }

        submitButton.text = "Submit"
        nextButton.text = "Next"
        nextButton.visibility = View.VISIBLE

        // Disable buttons until all questions are answered
        submitButton.isEnabled = false
        nextButton.isEnabled = false

        // Populate UI
        questionsOnPage.forEachIndexed { index, question ->
            questionTextViews[index].text = question.text
            questionTextViews[index].visibility = View.VISIBLE
            questionRadioGroups[index].visibility = View.VISIBLE
        }

        // Add listeners to enable buttons once all questions are answered
        questionRadioGroups.take(questionsOnPage.size).forEach { radioGroup ->
            radioGroup.setOnCheckedChangeListener { _, _ -> checkIfAllAnswered() }
        }
    }

    private fun checkIfAllAnswered() {
        val questionsOnPage = getCurrentPageQuestions()
        if (questionsOnPage.isEmpty()) return

        val allAnswered = questionRadioGroups.take(questionsOnPage.size).all { it.checkedRadioButtonId != -1 }

        submitButton.isEnabled = allAnswered
        nextButton.isEnabled = allAnswered
    }

    private fun handleSubmit() {
        val questionsOnPage = getCurrentPageQuestions()
        if (questionsOnPage.isEmpty()) return

        var correctAnswersOnPage = 0
        questionsOnPage.forEachIndexed { index, question ->
            val radioGroup = questionRadioGroups[index]
            val selectedId = radioGroup.checkedRadioButtonId
            val selectedRadioButton = findViewById<RadioButton>(selectedId)
            val selectedAnswer = selectedRadioButton.text.toString().equals("true", ignoreCase = true)
            if (selectedAnswer == question.correctAnswer) {
                score++
                correctAnswersOnPage++
            }
        }

        val allCorrect = correctAnswersOnPage == questionsOnPage.size
        val dialog = showPageResultDialog(allCorrect)
        val soundResId = if (allCorrect) R.raw.excellent else R.raw.common_u_can_do_batter_than_that
        playSound(soundResId, dialog)

        showAnswers()
        submitButton.text = "Next Page"
        submitButton.setOnClickListener { goToNextPage() }
    }

    private fun showAnswers() {
        val questionsOnPage = getCurrentPageQuestions()
        if (questionsOnPage.isEmpty()) return

        questionsOnPage.forEachIndexed { index, question ->
            val radioGroup = questionRadioGroups[index]
            val trueButton = radioGroup.getChildAt(0) as RadioButton
            val falseButton = radioGroup.getChildAt(1) as RadioButton

            trueButton.isEnabled = false
            falseButton.isEnabled = false

            val green = ColorStateList.valueOf(Color.GREEN)
            val red = ColorStateList.valueOf(Color.RED)

            if (question.correctAnswer) {
                trueButton.setTextColor(Color.GREEN)
                trueButton.buttonTintList = green
            } else {
                falseButton.setTextColor(Color.GREEN)
                falseButton.buttonTintList = green
            }

            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedButton = findViewById<RadioButton>(selectedId)
                val selectedAnswer = selectedButton.text.toString().equals("true", ignoreCase = true)
                if (selectedAnswer != question.correctAnswer) {
                    selectedButton.setTextColor(Color.RED)
                    selectedButton.buttonTintList = red
                }
            }
        }

        nextButton.visibility = View.GONE
        submitButton.isEnabled = true
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

    private fun showPageResultDialog(allCorrect: Boolean): Dialog {
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
            text = if (allCorrect) "You got all the answers correct!" else "You can do better!"
            textSize = 18f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        textLayout.addView(title)
        textLayout.addView(msg)
        layout.addView(textLayout)

        dialog.setContentView(layout)
        dialog.show()
        return dialog
    }

    private fun goToNextPage() {
        currentPage++
        val totalPages = (activityModel?.questions?.size?.plus(questionsPerPage - 1))?.div(questionsPerPage) ?: 0
        if (currentPage < totalPages) {
            displayCurrentPage()
            submitButton.setOnClickListener { handleSubmit() }
        } else showFinalScore()
    }

    private fun showFinalScore() {
        val total = activityModel?.questions?.size ?: 0
        AlertDialog.Builder(this)
            .setTitle("Quiz Finished!")
            .setMessage("Your score: $score / $total")
            .setPositiveButton("Finish") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun getCurrentPageQuestions(): List<TrueFalseQuestion> {
        val questions = activityModel?.questions ?: return emptyList()
        val start = currentPage * questionsPerPage
        val end = minOf(start + questionsPerPage, questions.size)
        return questions.subList(start, end)
    }
}

