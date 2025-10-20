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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.ersurajrajput.quizapp.databinding.ActivityBasketBallGameBinding
import com.ersurajrajput.quizapp.repo.GamesRepo

/*
* NOTE: To use this code, you need to:
* 1. Add the Glide library dependency to your app's build.gradle file:
* implementation 'com.github.bumptech.glide:glide:4.12.0'
* annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
*
* 2. Add an ImageView to your `activity_basket_ball_game.xml` layout for the animation.
* It should be centered and initially hidden. Example:
* <ImageView
* android:id="@+id/ivBoy"
* android:layout_width="match_parent"
* android:layout_height="match_parent"
* android:visibility="gone"
* android:scaleType="centerCrop" />
*
* 3. Add a default static PNG for the initial state, e.g., 'R.drawable.basket_boy'.
*/
class BasketBallGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBasketBallGameBinding
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
    // Using lazy initialization for colors is good for avoiding context issues, but ensure R.color is available
    private val CORRECT_COLOR by lazy { ContextCompat.getColor(this, R.color.correct_green) }
    private val INCORRECT_COLOR by lazy { ContextCompat.getColor(this, R.color.wrong_red) }
    private val DEFAULT_CARD_BG = Color.WHITE
    // DEFAULT_STROKE_COLOR was removed as it referenced an undeclared class and was not used in the game logic.

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
        binding = ActivityBasketBallGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
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
        // Assuming R.raw.excellent and R.raw.common_u_can_do_batter_than_that exist
        correctSoundId = soundPool.load(this, R.raw.excellent, 1)
        incorrectSoundId = soundPool.load(this, R.raw.common_u_can_do_batter_than_that, 1)
    }

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private fun setupButtonClickListeners() {
        val optionCards = listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
        val optionCheckBoxes = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        optionCheckBoxes.forEach { it.isClickable = false } // Checkboxes are controlled by card click

        optionCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                // Set visual state immediately
                optionCheckBoxes.forEachIndexed { cbIndex, checkBox ->
                    checkBox.isChecked = index == cbIndex
                }
                // Update internal state and submit
                selectedOptionIndex = index
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
            if (gameModel != null) startGame()
            else {
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
        // Load a default static image.


        selectedOptionIndex = null
        val optionCards = listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)
        val optionCheckBoxes = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        optionCards.forEachIndexed { index, card ->
            question.options.getOrNull(index)?.let { option ->
                optionCheckBoxes[index].text = option.text
                card.visibility = View.VISIBLE

                // Reset colors and state
                card.setCardBackgroundColor(DEFAULT_CARD_BG)
                card.isEnabled = true // Re-enable clicks
                optionCheckBoxes[index].isChecked = false
            } ?: run { card.visibility = View.GONE }
        }
    }

    /**
     * Uses safe calls to ensure index is not null before proceeding.
     */
    private fun handleAnswerSubmission() {
        // Use a safe check for selectedOptionIndex
        val index = selectedOptionIndex ?: return

        // Disable all option cards immediately after selection
        listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card).forEach { it.isEnabled = false }

        val currentQuestion = gameModel?.questions?.getOrNull(currentQuestionIndex) ?: return
        val selectedOption = currentQuestion.options.getOrNull(index) ?: return
        val isCorrect = selectedOption.correct

        // Play animation first, then proceed with feedback
        playGifAnimation(isCorrect)
    }

    private fun playGifAnimation(isCorrect: Boolean) {
        val gifResource = if (isCorrect) R.drawable.b_correct else R.drawable.b_wrong
        val staticFallback = R.drawable.basket_bg // Fallback if GIF fails

        // *** FIX for "two boys" issue: Clear the ImageView before loading the GIF
        // This ensures the previous static image is completely removed.
        Glide.with(this).clear(binding.ivBoy)

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
                    // Fallback to static image and proceed with feedback
                    Glide.with(this@BasketBallGameActivity).load(staticFallback).into(binding.ivBoy)
                    proceedWithFeedback(isCorrect)
                    return false
                }

                override fun onResourceReady(resource: GifDrawable, model: Any, target: Target<GifDrawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    resource.setLoopCount(1)
                    resource.start() // Explicitly start the GIF
                    resource.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            super.onAnimationEnd(drawable)
                            // Once animation ends, load static image back and proceed
                            Glide.with(this@BasketBallGameActivity).load(staticFallback).into(binding.ivBoy)
                            proceedWithFeedback(isCorrect)
                        }
                    })
                    return false // Let Glide handle the drawing of the GIF
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

    /**
     * Displays a simple dialog with "Correct!" or "Wrong Answer".
     */
    private fun showFeedbackDialog(isCorrect: Boolean): Dialog {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textView = TextView(this).apply {
            text = if (isCorrect) "Correct!" else "Wrong Answer"
            textSize = 28f
            gravity = Gravity.CENTER
            setPadding(60, 50, 60, 50)
            setTextColor(Color.BLACK)
            // Assuming R.drawable.popup_bg exists for styling
            setBackgroundResource(R.drawable.popup_bg)
        }

        dialog.setContentView(textView)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

    /**
     * Updates score and highlights cards after the feedback dialog is dismissed.
     */
    private fun postPopupActions(isCorrect: Boolean) {
        // Safely get selected index and question, returning if null
        val selectedIndex = selectedOptionIndex
        val currentQuestion = gameModel?.questions?.getOrNull(currentQuestionIndex)
        if (selectedIndex == null || currentQuestion == null) return

        val optionCards = listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card)

        if (isCorrect) {
            score++
            binding.tvScore.text = score.toString()
            optionCards[selectedIndex].setCardBackgroundColor(CORRECT_COLOR)
            // Add animation for correct answer (rotation)
            optionCards[selectedIndex].animate().rotationBy(360f).setDuration(500).start()
        } else {
            // Highlight selected wrong answer as incorrect
            optionCards[selectedIndex].setCardBackgroundColor(INCORRECT_COLOR)

            // Reveal correct answer
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
        // Hide all game UI elements for a clean exit sequence
        binding.tvQuestion.visibility = View.INVISIBLE
        listOf(binding.option1Card, binding.option2Card, binding.option3Card, binding.option4Card).forEach {
            it.visibility = View.GONE
        }

        // Show the final score in a custom dialog
        showEndGameDialog()

        // After a delay, finish the activity to go back to the previous screen
        handler.postDelayed({
            finish()
        }, END_GAME_DIALOG_DURATION_MS)
    }

    private fun showEndGameDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val totalQuestions = gameModel?.questions?.size ?: 0
        val finalScoreText = "🎯 Game Over!\nYour Score: $score / $totalQuestions"

        val textView = TextView(this).apply {
            text = finalScoreText
            textSize = 28f
            gravity = Gravity.CENTER
            setPadding(80, 70, 80, 70)
            setTextColor(Color.BLACK)
            // Assuming R.drawable.popup_bg exists for styling
            setBackgroundResource(R.drawable.popup_bg)
        }

        dialog.setContentView(textView)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundPool.isInitialized) {
            soundPool.release()
        }
        // This is crucial to prevent memory leaks or crashes if the user exits while a delay is pending
        handler.removeCallbacksAndMessages(null)
    }
}
