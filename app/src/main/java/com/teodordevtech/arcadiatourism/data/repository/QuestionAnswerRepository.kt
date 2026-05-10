package com.teodordevtech.arcadiatourism.data.repository

import com.teodordevtech.arcadiatourism.data.model.Answer
import com.teodordevtech.arcadiatourism.data.model.QuestionWithAnswer
import com.teodordevtech.arcadiatourism.data.model.TopicQuestion
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.util.UUID

class QuestionAnswerRepository {
    private val supabase = SupabaseProvider.client

    suspend fun askQuestion(topicId: String, studentId: String, questionText: String): Result<Unit> {
        return try {
            val question = TopicQuestion(
                questionId = UUID.randomUUID().toString(),
                topicId = topicId,
                studentId = studentId,
                questionText = questionText,
                status = "unanswered",
                createdAt = System.currentTimeMillis()
            )
            supabase.from("questions").insert(question)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun getQuestionsForTopic(topicId: String): Result<List<QuestionWithAnswer>> {
        return try {
            val questions = supabase.from("questions")
                .select {
                    filter {
                        eq("topic_id", topicId)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<TopicQuestion>()
            val answers = loadAnswersForQuestions(questions.map { it.questionId })

            val combined = questions.map { question ->
                QuestionWithAnswer(
                    question = question,
                    answer = answers[question.questionId]
                )
            }
            Result.success(combined)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun loadAnswersForQuestions(questionIds: List<String>): Map<String, Answer> {
        if (questionIds.isEmpty()) return emptyMap()

        return questionIds.chunked(100).flatMap { batch ->
            supabase.from("answers")
                .select {
                    filter {
                        isIn("question_id", batch)
                    }
                }
                .decodeList<Answer>()
        }.associateBy { answer -> answer.questionId }
    }

    suspend fun answerQuestion(questionId: String, teacherId: String, answerText: String): Result<Unit> {
        return try {
            val answer = Answer(
                answerId = UUID.randomUUID().toString(),
                questionId = questionId,
                teacherId = teacherId,
                answerText = answerText,
                createdAt = System.currentTimeMillis()
            )

            supabase.from("answers").insert(answer)
            supabase.from("questions").update(
                {
                    set("status", "answered")
                }
            ) {
                filter {
                    eq("question_id", questionId)
                }
            }

            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
