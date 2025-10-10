package com.ersurajrajput.quizapp.models



// Represents a single matchable pair
data class MatchPair(
    var id: String = "",         // unique id for this pair
    var leftOption: String = "", // item on the left side
    var rightOption: String = "" // correct matching item on the right side
)

// Represents a single page of match-the-following pairs
data class MatchPairPage(
    var id: String = "",               // unique id for the page
    var pairs: List<MatchPair> = emptyList() // list of pairs on this page
)

// Represents the full match-the-following quiz
data class MatchTheFollowingModel(
    var id: String = "",                 // quiz id
    var title: String = "",              // quiz title
    var desc: String = "",               // quiz description
    var pages: List<MatchPairPage> = emptyList() // all pages of the quiz
)

