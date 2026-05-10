package com.teodordevtech.arcadiatourism.data.repository

import com.teodordevtech.arcadiatourism.data.model.Quiz
import com.teodordevtech.arcadiatourism.data.model.QuizQuestion
import com.teodordevtech.arcadiatourism.data.model.QuizResult
import com.teodordevtech.arcadiatourism.data.model.StudentQuizResult
import com.teodordevtech.arcadiatourism.data.model.User
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.util.UUID

class QuizRepository {
    private val supabase = SupabaseProvider.client

    suspend fun createQuiz(topicId: String, title: String, createdBy: String): Result<String> {
        return try {
            val quizId = UUID.randomUUID().toString()
            val quiz = Quiz(
                quizId = quizId,
                topicId = topicId,
                title = title,
                createdBy = createdBy,
                createdAt = System.currentTimeMillis()
            )
            supabase.from("quizzes").insert(quiz)
            Result.success(quizId)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun getQuizzesForTopic(topicId: String): Result<List<Quiz>> {
        return try {
            val quizzes = supabase.from("quizzes")
                .select {
                    filter {
                        eq("topic_id", topicId)
                    }
                    order(column = "created_at", order = Order.ASCENDING)
                }
                .decodeList<Quiz>()
            Result.success(quizzes)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun addQuestionToQuiz(
        quizId: String,
        questionText: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctAnswer: String
    ): Result<Unit> {
        return try {
            val nextOrder = supabase.from("quiz_questions")
                .select {
                    filter {
                        eq("quiz_id", quizId)
                    }
                    order(column = "question_order", order = Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<QuizQuestion>()?.questionOrder?.plus(1) ?: 1

            val question = QuizQuestion(
                questionId = UUID.randomUUID().toString(),
                quizId = quizId,
                questionText = questionText,
                optionA = optionA,
                optionB = optionB,
                optionC = optionC,
                optionD = optionD,
                correctAnswer = correctAnswer,
                questionOrder = nextOrder
            )
            supabase.from("quiz_questions").insert(question)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun getQuestionsForQuiz(quizId: String): Result<List<QuizQuestion>> {
        return try {
            Result.success(
                supabase.from("quiz_questions")
                    .select {
                        filter {
                            eq("quiz_id", quizId)
                        }
                        order(column = "question_order", order = Order.ASCENDING)
                    }
                    .decodeList<QuizQuestion>()
            )
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun deleteQuiz(quizId: String): Result<Unit> {
        return try {
            supabase.from("quiz_results").delete {
                filter {
                    eq("quiz_id", quizId)
                }
            }
            supabase.from("quiz_questions").delete {
                filter {
                    eq("quiz_id", quizId)
                }
            }
            supabase.from("quizzes").delete {
                filter {
                    eq("quiz_id", quizId)
                }
            }
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun submitQuizResult(
        studentId: String,
        quizId: String,
        score: Int,
        totalQuestions: Int
    ): Result<Unit> {
        return try {
            val result = QuizResult(
                resultId = UUID.randomUUID().toString(),
                studentId = studentId,
                quizId = quizId,
                score = score,
                totalQuestions = totalQuestions,
                submittedAt = System.currentTimeMillis()
            )
            supabase.from("quiz_results").insert(result)
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun getQuizResults(quizId: String): Result<List<StudentQuizResult>> {
        return try {
            val results = supabase.from("quiz_results")
                .select {
                    filter {
                        eq("quiz_id", quizId)
                    }
                    order(column = "submitted_at", order = Order.DESCENDING)
                }
                .decodeList<QuizResult>()

            val students = if (results.isEmpty()) {
                emptyMap()
            } else {
                results.map { it.studentId }
                    .distinct()
                    .chunked(100)
                    .flatMap { batch ->
                        supabase.from("users")
                            .select {
                                filter {
                                    isIn("uid", batch)
                                }
                            }
                            .decodeList<User>()
                    }
                    .associateBy { user -> user.uid }
            }

            Result.success(
                results.map { result ->
                    val student = students[result.studentId]
                    StudentQuizResult(
                        resultId = result.resultId,
                        studentId = result.studentId,
                        studentName = student?.fullName.orEmpty(),
                        studentEmail = student?.email.orEmpty(),
                        score = result.score,
                        totalQuestions = result.totalQuestions,
                        submittedAt = result.submittedAt
                    )
                }
            )
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
