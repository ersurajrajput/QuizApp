package com.ersurajrajput.quizapp.models

// Activity containing multiple image-based MCQs
data class ImageMCQModel(
    var id: String = "",
    var title: String = "",
    var desc: String = "",
    var questions: List<ImageMCQQuestion> = emptyList()
)

// Single image-based MCQ question
data class ImageMCQQuestion(
    var id: String = "",
    var text: String = "", // Question text
    var options: List<ImageMCQOption> = emptyList(), // 4 image options
    var correctOptionIndex: Int = 0 // Index of the correct image (0 to 3)
)

// Single image option
data class ImageMCQOption(
    var id: String = "",
    var imageUrl: String = "" // URL of the image
)
