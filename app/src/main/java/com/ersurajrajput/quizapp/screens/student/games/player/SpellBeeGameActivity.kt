package com.ersurajrajput.quizapp.screens.student.activity.games

import GameModel
import GameQuestion
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.databinding.ActivitySpellBeeGameBinding
import com.ersurajrajput.quizapp.repo.GamesRepo
import com.google.android.material.card.MaterialCardView

class SpellBeeGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpellBeeGameBinding
    private lateinit var gamesRepo: GamesRepo
    private var gameModel: GameModel? = null

    // Sound
    private lateinit var soundPool: SoundPool
    private var correctSoundId = 0
    private var incorrectSoundId = 0

    // Game State
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedOptionIndex: Int? = null
    private val handler = Handler(Looper.getMainLooper())

    // UI Colors
    private val CORRECT_COLOR by lazy { ContextCompat.getColor(this, R.color.correct_green) }
    private val INCORRECT_COLOR by lazy { ContextCompat.getColor(this, R.color.wrong_red) }
    private val DEFAULT_CARD_BG = Color.WHITE
    private val GOLD_STROKE_COLOR by lazy { ContextCompat.getColor(this, R.color.card_default_stroke) }


    // Timings
    private val DIALOG_DURATION_MS = 1500L
    private val FEEDBACK_VISIBLE_DURATION_MS = 1000L
    private val END_GAME_DIALOG_DURATION_MS = 3000L

    companion object {
        const val GAME_ID = "GAME_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivitySpellBeeGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gamesRepo = GamesRepo()
        setupSoundPool()
        setupButtonClickListeners()

        val gameId = intent.getStringExtra(GAME_ID)
        if (gameId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Game ID not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchGameDetails(gameId)
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttributes).build()
        correctSoundId = soundPool.load(this, R.raw.excellent, 1)
        incorrectSoundId = soundPool.load(this, R.raw.oh_no_think_and_do_it_again, 1)
    }

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private fun setupButtonClickListeners() {
        val optionCards = listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
        optionCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectedOptionIndex = index
                handleAnswerSubmission()
            }
        }
    }

    private fun fetchGameDetails(gameId: String) {
        binding.progressBar.visibility = View.VISIBLE
        gamesRepo.getGameById(gameId) { game ->
            binding.progressBar.visibility = View.GONE
            if (game != null) {
                gameModel = game
                startGame()
            } else {
                Toast.makeText(this, "Game not found!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startGame() {
        score = 0
        currentQuestionIndex = 0
        binding.tvScore.text = score.toString()
        displayQuestion()
    }

    private fun displayQuestion() {
        val question = gameModel?.questions?.getOrNull(currentQuestionIndex)
        if (question == null) {
            endGame()
            return
        }
        setupOptions(question)
    }

    private fun setupOptions(question: GameQuestion) {
        selectedOptionIndex = null
        val optionCards = listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
        val optionTexts = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        optionCards.forEach { card ->
            card.setCardBackgroundColor(DEFAULT_CARD_BG)
            card.strokeColor = GOLD_STROKE_COLOR
            card.isEnabled = true
        }

        optionTexts.forEachIndexed { index, textView ->
            question.options.getOrNull(index)?.let {
                textView.text = it.text
            }
        }
    }

    private fun handleAnswerSubmission() {
        if (selectedOptionIndex == null) return

        listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
            .forEach { it.isEnabled = false }

        val currentQuestion = gameModel?.questions?.getOrNull(currentQuestionIndex) ?: return
        val selectedOption = currentQuestion.options.getOrNull(selectedOptionIndex!!) ?: return
        val isCorrect = selectedOption.correct

        proceedWithFeedback(isCorrect)
    }

    private fun proceedWithFeedback(isCorrect: Boolean) {
        playSound(if (isCorrect) correctSoundId else incorrectSoundId)
        val dialog = showFeedbackDialog(isCorrect)

        handler.postDelayed({
            if (dialog.isShowing) dialog.dismiss()
            postPopupActions(isCorrect)
        }, DIALOG_DURATION_MS)
    }

    private fun showFeedbackDialog(isCorrect: Boolean): Dialog {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textView = TextView(this).apply {
            text = if (isCorrect) "Correct!" else "Not Quite"
            textSize = 28f
            gravity = Gravity.CENTER
            setPadding(60, 50, 60, 50)
            setTextColor(Color.BLACK)
            setBackgroundResource(R.drawable.popup_bg)
        }
        dialog.setContentView(textView)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

    private fun postPopupActions(isCorrect: Boolean) {
        val selectedIndex = selectedOptionIndex!!
        val optionCards = listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
        val currentQuestion = gameModel?.questions?.getOrNull(currentQuestionIndex) ?: return

        if (isCorrect) {
            score++
            binding.tvScore.text = score.toString()
            optionCards[selectedIndex].setCardBackgroundColor(CORRECT_COLOR)
        } else {
            optionCards[selectedIndex].setCardBackgroundColor(INCORRECT_COLOR)
            val correctIndex = currentQuestion.options.indexOfFirst { it.correct }
            if (correctIndex != -1) {
                optionCards[correctIndex].setCardBackgroundColor(CORRECT_COLOR)
            }
        }

        handler.postDelayed({
            loadNextQuestionOrEndGame()
        }, FEEDBACK_VISIBLE_DURATION_MS)
    }

    private fun loadNextQuestionOrEndGame() {
        currentQuestionIndex++
        displayQuestion()
    }

    private fun endGame() {

        listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
            .forEach { it.visibility = View.GONE }

        showEndGameDialog()

        handler.postDelayed({
            finish()
        }, END_GAME_DIALOG_DURATION_MS)
    }

    private fun showEndGameDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val finalScoreText = "🎯 Game Over!\nYour Score: $score / ${gameModel?.questions?.size}"
        val textView = TextView(this).apply {
            text = finalScoreText
            textSize = 28f
            gravity = Gravity.CENTER
            setPadding(80, 70, 80, 70)
            setTextColor(Color.BLACK)
            setBackgroundResource(R.drawable.popup_bg)
        }

        dialog.setContentView(textView)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
        handler.removeCallbacksAndMessages(null)
    }
}
