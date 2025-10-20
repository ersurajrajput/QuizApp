package com.ersurajrajput.quizapp.models

data class DragAndDropModel(
    var id:String = "",
    var title:String = "",
    var desc: String = "",
    var pages: List<DragAndDropPages> = emptyList()
)
data class DragAndDropPages(
    var id: String = "",
    var options: List<DragAndDropOptions> = emptyList()
)
data class DragAndDropOptions(
    var id: String = "",
    var name: String = "",
    var imageUri: String="",
)
