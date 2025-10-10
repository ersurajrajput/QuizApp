package com.ersurajrajput.quizapp.screens.student.games.player

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
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.databinding.ActivityUnscrambledWordsGameBinding
import com.ersurajrajput.quizapp.repo.GamesRepo

class UnscrambledWordsGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnscrambledWordsGameBinding
    private lateinit var gamesRepo: GamesRepo
    private var gameModel: GameModel? = null

    // Sound
    private lateinit var soundPool: SoundPool
    private var correctSoundId = 0
    private var incorrectSoundId = 0

    // Game State
    private var currentQuestionIndex = 0
    private var score = 0
    private var currentQuestion: GameQuestion? = null
    private var correctWord = ""
    private var currentAnswer = StringBuilder()
    private val handler = Handler(Looper.getMainLooper())

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
        binding = ActivityUnscrambledWordsGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gamesRepo = GamesRepo()
        setupSoundPool()
        setupKeyboard()
        setupSubmitButton()
        setupDeleteButton()

        val gameId = intent.getStringExtra(GAME_ID)
        if (gameId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Game ID not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        fetchGameDetails(gameId)
    }

    private fun fetchGameDetails(gameId: String) {
        // You can add a progress bar here if you like
        gamesRepo.getGameById(gameId) { game ->
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
        displayNextWord()
    }

    private fun displayNextWord() {
        if (currentQuestionIndex >= (gameModel?.questions?.size ?: 0)) {
            endGame()
            return
        }

        currentQuestion = gameModel?.questions?.get(currentQuestionIndex)
        correctWord = currentQuestion?.options?.firstOrNull { it.correct }?.text ?: ""

        if (correctWord.isEmpty()) {
            // Skip invalid question
            currentQuestionIndex++
            displayNextWord()
            return
        }

        val hint = currentQuestion?.questionText ?: ""
        val scrambledWord = getScrambledWord(correctWord)

        binding.tvWordHint.text = hint
        binding.tvScrambledWord.text = scrambledWord

        currentAnswer.clear()
        createLetterBoxes(correctWord.length)
        updateBoxes()
    }

    private fun getScrambledWord(word: String): String {
        val letters = word.toMutableList()
        if (letters.size <= 1) return word
        var scrambled: String
        do {
            letters.shuffle()
            scrambled = letters.joinToString("")
        } while (scrambled == word)
        return scrambled
    }


    private fun createLetterBoxes(count: Int) {
        binding.boxContainer.removeAllViews()
        val margin = 8

        repeat(count) {
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(margin, margin, margin, margin) }
                text = "_"
                textSize = 30f
                setTextColor(Color.BLACK)
                setBackgroundResource(R.drawable.white_box_background)
                setPadding(20, 10, 20, 10)
            }
            binding.boxContainer.addView(tv)
        }
    }

    private fun setupKeyboard() {
        val keyboardContainer = binding.keyboardContainer
        for (i in 0 until keyboardContainer.childCount) {
            val row = keyboardContainer.getChildAt(i) as? LinearLayout
            row?.let {
                for (j in 0 until it.childCount) {
                    (it.getChildAt(j) as? ImageView)?.setOnClickListener { view ->
                        onLetterClicked(view as ImageView)
                    }
                }
            }
        }
    }

    private fun onLetterClicked(key: ImageView) {
        val resName = resources.getResourceEntryName(key.id)
        val letter = resName.replace("key", "").uppercase()

        if (currentAnswer.length < correctWord.length) {
            currentAnswer.append(letter)
            updateBoxes()
        }
    }

    private fun setupDeleteButton() {
        binding.keyDelete.setOnClickListener {
            if (currentAnswer.isNotEmpty()) {
                currentAnswer.deleteCharAt(currentAnswer.length - 1)
                updateBoxes()
            }
        }
    }


    private fun updateBoxes() {
        for (i in 0 until binding.boxContainer.childCount) {
            val tv = binding.boxContainer.getChildAt(i) as TextView
            tv.text = if (i < currentAnswer.length) currentAnswer[i].toString() else "_"
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (currentAnswer.length != correctWord.length) {
                Toast.makeText(this, "Please fill all the boxes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val isCorrect = currentAnswer.toString().equals(correctWord, ignoreCase = true)

        playSound(if (isCorrect) correctSoundId else incorrectSoundId)
        val dialog = showFeedbackDialog(isCorrect)

        handler.postDelayed({
            if (dialog.isShowing) dialog.dismiss()
            postFeedbackActions(isCorrect)
        }, DIALOG_DURATION_MS)
    }

    private fun postFeedbackActions(isCorrect: Boolean) {
        if (isCorrect) {
            score += 10
            binding.tvScore.text = score.toString()
        }
        currentQuestionIndex++
        handler.postDelayed({
            displayNextWord()
        }, FEEDBACK_VISIBLE_DURATION_MS)
    }

    private fun endGame() {
        // Hide game UI
        binding.keyboardContainer.visibility = View.GONE
        binding.boxContainer.visibility = View.GONE
        binding.tvScrambledWord.visibility = View.INVISIBLE
        binding.tvWordHint.text = "Game Over!"

        showEndGameDialog()

        handler.postDelayed({
            finish()
        }, END_GAME_DIALOG_DURATION_MS)
    }

    // --- Sound and Dialog Functions (Consistent with other games) ---

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

    private fun showFeedbackDialog(isCorrect: Boolean): Dialog {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val textView = TextView(this).apply {
            text = if (isCorrect) "Correct!" else "Wrong!"
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

    private fun showEndGameDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val finalScoreText = "🎯 Game Over!\nYour Score: $score"
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
