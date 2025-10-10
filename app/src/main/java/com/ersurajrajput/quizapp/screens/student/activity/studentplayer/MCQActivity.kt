package com.ersurajrajput.quizapp.screens.student.activity.studentplayer

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.databinding.ActivityMcqactivityBinding
import com.ersurajrajput.quizapp.models.MCQActivityModel
import com.ersurajrajput.quizapp.repo.MCQActivityRepo
import kotlinx.coroutines.launch

class MCQActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMcqactivityBinding
    private val repo = MCQActivityRepo()
    private var mcqActivityModel: MCQActivityModel? = null
    private var currentQuestionIndex = 0
    private lateinit var optionCheckBoxes: List<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMcqactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemBars()

        val activityId = intent.getStringExtra("ID")
        if (activityId == null) {
            Toast.makeText(this, "Error: Activity ID not provided.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        optionCheckBoxes = listOf(
            binding.option1CheckBox,
            binding.option2CheckBox,
            binding.option3CheckBox,
            binding.option4CheckBox
        )

        loadActivityData(activityId)
        setupClickListeners()
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
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun loadActivityData(id: String) {
        lifecycleScope.launch {
            try {
                mcqActivityModel = repo.getActivityById(id)
                if (mcqActivityModel != null && mcqActivityModel!!.qList.isNotEmpty()) {
                    displayQuestion(currentQuestionIndex)
                } else {
                    Toast.makeText(this@MCQActivity, "Failed to load quiz or quiz is empty.", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MCQActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun displayQuestion(index: Int) {
        mcqActivityModel?.let { model ->
            if (index < model.qList.size) {
                val question = model.qList[index]
                clearCheckBoxes()
                binding.questionTextView.text = question.qTitle
                binding.progressTextView.text = "${index + 1} of ${model.qList.size}"

                question.answerOption.forEachIndexed { optionIndex, option ->
                    if (optionIndex < optionCheckBoxes.size) {
                        optionCheckBoxes[optionIndex].text = option.text
                        optionCheckBoxes[optionIndex].visibility = View.VISIBLE
                    }
                }

                for (i in question.answerOption.size until optionCheckBoxes.size) {
                    optionCheckBoxes[i].visibility = View.GONE
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.submitButton.setOnClickListener {
            val question = mcqActivityModel?.qList?.getOrNull(currentQuestionIndex) ?: return@setOnClickListener
            var isCorrect = true
            var anyOptionSelected = false

            question.answerOption.forEachIndexed { index, option ->
                if (index < optionCheckBoxes.size) {
                    val checkBox = optionCheckBoxes[index]
                    if (checkBox.isChecked) {
                        anyOptionSelected = true
                    }
                    if (checkBox.isChecked != option.correct) {
                        isCorrect = false
                    }
                }
            }

            if (!anyOptionSelected) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showResultDialog(isCorrect)
        }

        binding.nextButton.setOnClickListener {
            mcqActivityModel?.let {
                if (currentQuestionIndex < it.qList.size - 1) {
                    currentQuestionIndex++
                    displayQuestion(currentQuestionIndex)
                } else {
                    Toast.makeText(this, "You have reached the end of the quiz!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showResultDialog(isCorrect: Boolean) {
        val titleText = if (isCorrect) "Excellent!" else "Keep Trying!"
        val messageText = if (isCorrect) "That's the correct answer!" else "You can do better!"
        val soundResId = if (isCorrect) R.raw.excellent else R.raw.common_u_can_do_batter_than_that

        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
        }

        val layout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            setBackgroundResource(R.drawable.popup_bg)
            setPadding(100, 100, 100, 100)
        }

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val titleView = TextView(this).apply {
            text = titleText
            textSize = 24f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val messageView = TextView(this).apply {
            text = messageText
            textSize = 18f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        textLayout.addView(titleView)
        textLayout.addView(messageView)
        layout.addView(textLayout)
        dialog.setContentView(layout)
        dialog.show()

        playSound(soundResId, dialog)
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

    private fun clearCheckBoxes() {
        optionCheckBoxes.forEach { it.isChecked = false }
    }
}

