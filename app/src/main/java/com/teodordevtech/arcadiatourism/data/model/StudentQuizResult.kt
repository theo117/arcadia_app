package com.teodordevtech.arcadiatourism.data.model

data class StudentQuizResult(
    val resultId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentEmail: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val submittedAt: Long = 0L
) {
    val percentage: Int
        get() = if (totalQuestions == 0) 0 else (score * 100) / totalQuestions
}
