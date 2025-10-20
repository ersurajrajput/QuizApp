package com.ersurajrajput.quizapp.screens.student.activity.studentplayer

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.databinding.ActivityMcqimageBinding
import com.ersurajrajput.quizapp.models.ImageMCQModel
import com.ersurajrajput.quizapp.repo.ImageMcqRepo
import android.view.Gravity
import android.widget.TextView
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge

class MCQImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMcqimageBinding
    private val repo = ImageMcqRepo()

    private var mcqModel: ImageMCQModel? = null
    private var currentQuestionIndex = 0
    private var selectedOptionIndex: Int? = null
    private var score = 0
    private var isAnswerSubmittedForCurrentQuestion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivityMcqimageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val activityId = intent.getStringExtra("ID")
        if (activityId == null) {
            Toast.makeText(this, "Activity ID not provided", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        repo.getActivityById(activityId) { model ->
            if (model == null) {
                Toast.makeText(this, "Failed to load activity", Toast.LENGTH_LONG).show()
                finish()
            } else {
                mcqModel = model
                showQuestion()
            }
        }

        binding.option1CheckBox.setOnClickListener { onOptionSelected(0) }
        binding.option2CheckBox.setOnClickListener { onOptionSelected(1) }
        binding.option3CheckBox.setOnClickListener { onOptionSelected(2) }
        binding.option4CheckBox.setOnClickListener { onOptionSelected(3) }

        binding.nextButton.setOnClickListener {
            if (currentQuestionIndex < (mcqModel?.questions?.size ?: 0) - 1) {
                currentQuestionIndex++
                selectedOptionIndex = null
                resetOptions()
                showQuestion()
            } else {
                Toast.makeText(this, "Quiz Completed! Score: $score", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        binding.submitButton.setOnClickListener {
            handleAnswerSubmission { /* Do nothing */ }
        }
        backBtn()
    }

    fun backBtn(){
        val btn = findViewById<ImageView>(R.id.btnBack)
        btn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showQuestion() {
        val question = mcqModel?.questions?.get(currentQuestionIndex) ?: return
        isAnswerSubmittedForCurrentQuestion = false

        binding.nextButton.visibility = View.INVISIBLE // Hide next button for new question
        binding.questionTextView.text = question.text
        binding.progressTextView.text = "${currentQuestionIndex + 1} of ${mcqModel?.questions?.size}"

        loadImage(binding.imageView1, question.options.getOrNull(0)?.imageUrl)
        loadImage(binding.imageView2, question.options.getOrNull(1)?.imageUrl)
        loadImage(binding.imageView3, question.options.getOrNull(2)?.imageUrl)
        loadImage(binding.imageView4, question.options.getOrNull(3)?.imageUrl)
    }

    private fun loadImage(imageView: ImageView, url: String?) {
        if (!url.isNullOrEmpty()) {
            Glide.with(this).load(url).into(imageView)
        } else {
            imageView.setImageResource(android.R.color.darker_gray)
        }
    }

    private fun onOptionSelected(index: Int) {
        selectedOptionIndex = index
        binding.option1CheckBox.isChecked = index == 0
        binding.option2CheckBox.isChecked = index == 1
        binding.option3CheckBox.isChecked = index == 2
        binding.option4CheckBox.isChecked = index == 3
    }

    private fun resetOptions() {
        selectedOptionIndex = null
        binding.option1CheckBox.isChecked = false
        binding.option2CheckBox.isChecked = false
        binding.option3CheckBox.isChecked = false
        binding.option4CheckBox.isChecked = false
    }

    private fun handleAnswerSubmission(onContinue: () -> Unit) {
        if (selectedOptionIndex == null) {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            return
        }

        binding.nextButton.visibility = View.VISIBLE // Show next button after submission

        val question = mcqModel?.questions?.get(currentQuestionIndex) ?: return
        val isCorrect = selectedOptionIndex == question.correctOptionIndex

        if (!isAnswerSubmittedForCurrentQuestion) {
            if (isCorrect) {
                score++
            }
            isAnswerSubmittedForCurrentQuestion = true
        }

        showResultDialog(isCorrect, onContinue)
    }

    private fun showResultDialog(isCorrect: Boolean, onContinue: () -> Unit) {
        val title: String
        val soundResId: Int

        if (isCorrect) {
            title = "Correct!"
            soundResId = R.raw.excellent
        } else {
            title = "Wrong Answer"
            soundResId = R.raw.common_u_can_do_batter_than_that
        }

        val titleTextView = TextView(this).apply {
            text = title
            gravity = Gravity.CENTER
            textSize = 24f
            setTextColor(Color.BLACK)
            setPadding(40, 80, 40, 80)
        }

        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(titleTextView)
        builder.setCancelable(false)

        val dialog = builder.create()
        val backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.popup_bg)
        dialog.window?.setBackgroundDrawable(backgroundDrawable)

        dialog.setOnDismissListener {
            onContinue()
        }
        dialog.show()

        var soundPlayed = false
        try {
            val mediaPlayer = MediaPlayer.create(this, soundResId)
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener {
                    it.release()
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
                mediaPlayer.start()
                soundPlayed = true
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing sound. Resource not found.", Toast.LENGTH_SHORT).show()
        }

        if (!soundPlayed) {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }
}

