package com.teodordevtech.arcadiatourism.data.model

data class QuestionWithAnswer(
    val question: TopicQuestion,
    val answer: Answer? = null
)
