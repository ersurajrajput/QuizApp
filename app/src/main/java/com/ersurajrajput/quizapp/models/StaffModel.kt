package com.ersurajrajput.quizapp.models

data class StaffModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var mobile: String = "",
    var role: String = "",
    var department: String = "staff",
    var password: String = ""
)
