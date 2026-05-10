package com.teodordevtech.arcadiatourism.ui.navigation

import android.net.Uri

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object SignUp : AppDestination("sign_up")
    data object AdminDashboard : AppDestination("admin_dashboard")
    data object StudentHome : AppDestination("student_home")
    data object Topics : AppDestination("topics/{role}") {
        fun createRoute(role: String): String = "topics/$role"
    }
    data object TopicDetail : AppDestination("topic_detail/{topicId}/{role}") {
        fun createRoute(topicId: String, role: String): String = "topic_detail/$topicId/$role"
    }
    data object TopicContentManager : AppDestination("topic_content_manager/{topicId}") {
        fun createRoute(topicId: String): String = "topic_content_manager/$topicId"
    }
    data object UploadDocument : AppDestination("upload_document/{topicId}") {
        fun createRoute(topicId: String): String = "upload_document/$topicId"
    }
    data object UploadMedia : AppDestination("upload_media/{topicId}") {
        fun createRoute(topicId: String): String = "upload_media/$topicId"
    }
    data object ManageMedia : AppDestination("manage_media/{topicId}") {
        fun createRoute(topicId: String): String = "manage_media/$topicId"
    }
    data object MediaList : AppDestination("media_list/{topicId}") {
        fun createRoute(topicId: String): String = "media_list/$topicId"
    }
    data object VideoPlayer : AppDestination("video_player/{mediaId}") {
        fun createRoute(mediaId: String): String = "video_player/$mediaId"
    }
    data object AudioPlayer : AppDestination("audio_player/{mediaId}") {
        fun createRoute(mediaId: String): String = "audio_player/$mediaId"
    }
    data object QuizList : AppDestination("quiz_list/{topicId}/{role}") {
        fun createRoute(topicId: String, role: String): String = "quiz_list/$topicId/$role"
    }
    data object CreateQuiz : AppDestination("create_quiz/{topicId}") {
        fun createRoute(topicId: String): String = "create_quiz/$topicId"
    }
    data object AddQuestionToQuiz : AppDestination("add_question_to_quiz/{quizId}") {
        fun createRoute(quizId: String): String = "add_question_to_quiz/$quizId"
    }
    data object Quiz : AppDestination("quiz/{quizId}/{quizTitle}") {
        fun createRoute(quizId: String, quizTitle: String): String {
            return "quiz/$quizId/${Uri.encode(quizTitle)}"
        }
    }
    data object QuizResult : AppDestination("quiz_result")
    data object AskQuestion : AppDestination("ask_question/{topicId}") {
        fun createRoute(topicId: String): String = "ask_question/$topicId"
    }
    data object QuestionsList : AppDestination("questions_list/{topicId}") {
        fun createRoute(topicId: String): String = "questions_list/$topicId"
    }
    data object ManageQuestions : AppDestination("manage_questions/{topicId}") {
        fun createRoute(topicId: String): String = "manage_questions/$topicId"
    }
}
