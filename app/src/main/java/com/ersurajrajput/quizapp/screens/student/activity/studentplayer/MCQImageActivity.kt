package com.ersurajrajput.quizapp.screens.student.activity.studentplayer

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.databinding.ActivityMcqimageBinding
import com.ersurajrajput.quizapp.models.ImageMCQModel
import com.ersurajrajput.quizapp.repo.ImageMcqRepo

// NOTE: The 'lifecycleScope' and 'kotlinx.coroutines.launch' imports were removed as they are no longer used.

class MCQImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMcqimageBinding
    private val repo = ImageMcqRepo()

    private var mcqModel: ImageMCQModel? = null
    private var currentQuestionIndex = 0
    private var selectedOptionIndex: Int? = null
    private var score = 0

    // To prevent checking the same answer multiple times and adding to the score.
    private var isAnswerSubmittedForCurrentQuestion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            if (selectedOptionIndex == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // This function now handles showing the result dialog before executing the next step.
            handleAnswerSubmission {
                // This block is the 'onContinue' action, which runs after the dialog is dismissed.
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
        }

        binding.submitButton.setOnClickListener {
            if (selectedOptionIndex == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // The onContinue action is empty because we don't want to move to the next question.
            handleAnswerSubmission { /* Do nothing */ }
        }
        backBtn()
    }
    fun backBtn(){
        var btn = findViewById<ImageView>(R.id.btnBack)
        btn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showQuestion() {
        val question = mcqModel?.questions?.get(currentQuestionIndex) ?: return
        isAnswerSubmittedForCurrentQuestion = false // Reset for the new question

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
            imageView.setImageResource(android.R.color.darker_gray) // Placeholder for missing images
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
        // In a real app, these would be R.raw.sound_file.
         val soundResId: Int

        if (isCorrect) {
            title = "Correct!"
            // TODO: Ensure you have a sound file named 'excellent.mp3' in your res/raw folder.
             soundResId = R.raw.excellent
        } else {
            title = "Wrong Answer"
            // TODO: Ensure you have a sound file named 'coomon_you_can_do_better_then_that.mp3' in your res/raw folder.
             soundResId = R.raw.common_u_can_do_batter_than_that
        }

        // The sound playing part is commented out as I cannot add the resources (R.raw.*).
        // To make it work, add audio files to your project's 'res/raw' directory and uncomment this block.

        try {
            val mediaPlayer = MediaPlayer.create(this, soundResId)
            mediaPlayer?.setOnCompletionListener { it.release() }
            mediaPlayer?.start()
        } catch (e: Exception) {
            // This toast helps in debugging if the sound file is missing.
            Toast.makeText(this, "Error playing sound. Resource not found.", Toast.LENGTH_SHORT).show()
        }


        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        // Programmatically create a drawable to simulate the 'popup_bg' background.
        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(if (isCorrect) Color.parseColor("#E9F7EF") else Color.parseColor("#FDEBE9")) // Light Green / Light Red
            cornerRadius = 40f // Rounded corners
            setStroke(5, if (isCorrect) Color.parseColor("#2ECC71") else Color.parseColor("#E74C3C")) // Green / Red border
        }

        dialog.window?.setBackgroundDrawable(backgroundDrawable)

        // The onContinue lambda is called when the dialog is dismissed.
        dialog.setOnDismissListener {
            onContinue()
        }

        dialog.show()
    }
}

