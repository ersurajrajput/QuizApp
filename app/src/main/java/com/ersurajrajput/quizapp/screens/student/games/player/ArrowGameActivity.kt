package com.ersurajrajput.quizapp.screens.student.activity.games

import GameModel
import GameQuestion
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.databinding.ActivityArrowGameBinding
import com.ersurajrajput.quizapp.repo.GamesRepo

class ArrowGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArrowGameBinding
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
    private val DEFAULT_STROKE_COLOR by lazy { ContextCompat.getColor(this, R.color.card_default_stroke) }

    // Timings
    private val DIALOG_DURATION_MS = 1500L
    private val FEEDBACK_VISIBLE_DURATION_MS = 1000L
    private val END_GAME_DIALOG_DURATION_MS = 3000L

    companion object {
        const val GAME_ID = "GAME_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArrowGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Hide buttons as they are no longer used
        binding.btnSubmit.visibility = View.GONE
        binding.btnNext.visibility = View.GONE

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
    }

    private fun setupSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttributes).build()
        correctSoundId = soundPool.load(this, R.raw.excellent, 1)
        incorrectSoundId = soundPool.load(this, R.raw.common_u_can_do_batter_than_that, 1)
    }

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private fun setupButtonClickListeners() {
        val optionCards = listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
        val optionCheckBoxes = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        optionCheckBoxes.forEach { it.isClickable = false }

        optionCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectedOptionIndex = index
                optionCheckBoxes.forEachIndexed { cbIndex, checkBox ->
                    checkBox.isChecked = index == cbIndex
                }
                handleAnswerSubmission()
            }
        }
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun fetchGameDetails(gameId: String) {
        binding.progressBar.visibility = View.VISIBLE
        gamesRepo.getGameList { games ->
            binding.progressBar.visibility = View.GONE
            gameModel = games.find { it.id == gameId }
            if (gameModel != null) {
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
        binding.tvQuestion.text = question.questionText
        setupOptions(question)
    }

    private fun setupOptions(question: GameQuestion) {
        // Show the static boy image at the start of each question
        binding.ivBoy.visibility = View.VISIBLE
        // Load a default static image. Assumes you have 'arrow_boy.png' in drawables.
        Glide.with(this).load(R.drawable.final_boy).into(binding.ivBoy)

        selectedOptionIndex = null
        val optionCards = listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
        val optionCheckBoxes = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        optionCards.forEachIndexed { index, card ->
            val option = question.options.getOrNull(index)
            if (option != null) {
                optionCheckBoxes[index].text = option.text
                card.visibility = View.VISIBLE
                card.strokeColor = DEFAULT_STROKE_COLOR
                card.setCardBackgroundColor(DEFAULT_CARD_BG)
                card.isEnabled = true
                optionCheckBoxes[index].isChecked = false
            } else {
                card.visibility = View.GONE
            }
        }
    }

    private fun handleAnswerSubmission() {
        if (selectedOptionIndex == null) return

        listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card).forEach { it.isEnabled = false }

        val currentQuestion = gameModel?.questions?.getOrNull(currentQuestionIndex) ?: return
        val selectedOption = currentQuestion.options.getOrNull(selectedOptionIndex!!) ?: return
        val isCorrect = selectedOption.correct

        // Play the correct or incorrect GIF animation
        playGifAnimation(isCorrect)
    }

    private fun playGifAnimation(isCorrect: Boolean) {
        val gifResource = if (isCorrect) R.drawable.final_arrow_correct else R.drawable.final_arrow_wrong
        Glide.with(this)
            .asGif()
            .load(gifResource)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    // Fallback if GIF fails, proceed with feedback immediately
                    proceedWithFeedback(isCorrect)
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable,
                    model: Any,
                    target: Target<GifDrawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    resource.setLoopCount(1)
                    resource.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            super.onAnimationEnd(drawable)
                            // Animation is done, now show feedback
                            proceedWithFeedback(isCorrect)
                        }
                    })
                    return false
                }
            })
            .into(binding.ivBoy)
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
            text = if (isCorrect) "Correct!" else "Wrong Answer"
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
        binding.ivBoy.visibility = View.GONE // Hide the boy image at the end
        binding.tvQuestion.visibility = View.INVISIBLE
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

