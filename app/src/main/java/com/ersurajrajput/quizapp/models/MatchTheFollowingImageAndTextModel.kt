    package com.ersurajrajput.quizapp.models


    // Represents one text-image pair to match
    data class MatchTextImagePair(
        var id: String = "",           // unique id for this pair
        var leftText: String = "",     // the text on the left side (e.g., "Apple")
        var rightImageUrl: String = "" // the image URL for the correct match
    )

    // Represents a single page (group of multiple pairs)
    data class MatchTextImagePage(
        var id: String = "",                           // unique id for the page
        var pairs: List<MatchTextImagePair> = emptyList() // list of pairs in that page
    )

    // Represents the entire quiz (with multiple pages)
    data class MatchTheFollowingImageAndTextModel(
        var id: String = "",                           // quiz id
        var title: String = "",                        // quiz title (e.g., "Fruits and Pictures")
        var desc: String = "",                         // short description
        var pages: List<MatchTextImagePage> = emptyList() // all pages
    )
