package com.ersurajrajput.quizapp.models

// Represents a single matchable image pair
data class MatchImagePair(
    var id: String = "",          // unique id for this pair
    var leftImageUrl: String = "",  // URL of image on the left
    var rightImageUrl: String = ""  // URL of correct matching image on the right
)

// Represents a single page of image match-the-following pairs
data class MatchImagePairPage(
    var id: String = "",                      // unique id for the page
    var pairs: List<MatchImagePair> = emptyList() // list of image pairs on this page
)

// Represents the full image match-the-following quiz
data class MatchTheFollowingImageModel(
    var id: String = "",                         // quiz id
    var title: String = "",                      // quiz title
    var desc: String = "",                       // quiz description
    var pages: List<MatchImagePairPage> = emptyList() // all pages of the quiz
)
