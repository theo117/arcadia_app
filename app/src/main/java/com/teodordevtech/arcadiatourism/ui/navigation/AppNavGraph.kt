package com.teodordevtech.arcadiatourism.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teodordevtech.arcadiatourism.data.model.isStudentRole
import com.teodordevtech.arcadiatourism.data.model.isTeacherRole
import com.teodordevtech.arcadiatourism.data.model.normalizedRole
import com.teodordevtech.arcadiatourism.ui.screens.admin.AdminDashboardScreen
import com.teodordevtech.arcadiatourism.ui.screens.admin.UploadDocumentScreen
import com.teodordevtech.arcadiatourism.ui.screens.auth.LoginScreen
import com.teodordevtech.arcadiatourism.ui.screens.auth.SignUpScreen
import com.teodordevtech.arcadiatourism.ui.screens.media.AudioPlayerScreen
import com.teodordevtech.arcadiatourism.ui.screens.media.ManageMediaScreen
import com.teodordevtech.arcadiatourism.ui.screens.media.MediaListScreen
import com.teodordevtech.arcadiatourism.ui.screens.media.TopicContentManagerScreen
import com.teodordevtech.arcadiatourism.ui.screens.media.UploadMediaScreen
import com.teodordevtech.arcadiatourism.ui.screens.media.VideoPlayerScreen
import com.teodordevtech.arcadiatourism.ui.screens.questions.AskQuestionScreen
import com.teodordevtech.arcadiatourism.ui.screens.questions.ManageQuestionsScreen
import com.teodordevtech.arcadiatourism.ui.screens.questions.QuestionsListScreen
import com.teodordevtech.arcadiatourism.ui.screens.quiz.AddQuestionToQuizScreen
import com.teodordevtech.arcadiatourism.ui.screens.quiz.CreateQuizScreen
import com.teodordevtech.arcadiatourism.ui.screens.quiz.QuizListScreen
import com.teodordevtech.arcadiatourism.ui.screens.quiz.QuizResultScreen
import com.teodordevtech.arcadiatourism.ui.screens.quiz.QuizScreen
import com.teodordevtech.arcadiatourism.ui.screens.student.StudentHomeScreen
import com.teodordevtech.arcadiatourism.ui.screens.topics.TopicDetailScreen
import com.teodordevtech.arcadiatourism.ui.screens.topics.TopicsScreen
import com.teodordevtech.arcadiatourism.ui.viewmodel.AuthViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.DocumentViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.MediaViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.NotificationViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuestionAnswerViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuizViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.TopicViewModel

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    topicViewModel: TopicViewModel,
    documentViewModel: DocumentViewModel,
    mediaViewModel: MediaViewModel,
    quizViewModel: QuizViewModel,
    questionAnswerViewModel: QuestionAnswerViewModel,
    notificationViewModel: NotificationViewModel
) {
    val navController = rememberNavController()
    val authState = authViewModel.uiState.collectAsStateWithLifecycle().value
    val topicState = topicViewModel.uiState.collectAsStateWithLifecycle().value
    val documentState = documentViewModel.uiState.collectAsStateWithLifecycle().value
    val mediaState = mediaViewModel.uiState.collectAsStateWithLifecycle().value
    val quizState = quizViewModel.uiState.collectAsStateWithLifecycle().value
    val questionAnswerState = questionAnswerViewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(authState.currentUser?.uid, authState.currentUser?.role) {
        val user = authState.currentUser ?: return@LaunchedEffect
        notificationViewModel.initializeForUser(user.uid, user.role)
    }

    LaunchedEffect(authState.currentUser?.role) {
        when {
            authState.currentUser?.role?.isTeacherRole() == true -> navController.navigate(AppDestination.AdminDashboard.route) {
                popUpTo(AppDestination.Login.route) { inclusive = true }
            }

            authState.currentUser?.role?.isStudentRole() == true -> navController.navigate(AppDestination.StudentHome.route) {
                popUpTo(AppDestination.Login.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(authState.shouldReturnToLogin) {
        if (authState.shouldReturnToLogin) {
            navController.navigate(AppDestination.Login.route) {
                popUpTo(AppDestination.SignUp.route) { inclusive = true }
            }
            authViewModel.onReturnedToLogin()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route
    ) {
        composable(AppDestination.Login.route) {
            LoginScreen(
                uiState = authState,
                onEmailChange = authViewModel::updateEmail,
                onPasswordChange = authViewModel::updatePassword,
                onLoginClick = authViewModel::login,
                onSignUpClick = {
                    authViewModel.clearFeedback()
                    navController.navigate(AppDestination.SignUp.route)
                }
            )
        }

        composable(AppDestination.SignUp.route) {
            SignUpScreen(
                uiState = authState,
                onFullNameChange = authViewModel::updateFullName,
                onEmailChange = authViewModel::updateEmail,
                onPasswordChange = authViewModel::updatePassword,
                onGradeChange = authViewModel::updateGrade,
                onSignUpClick = authViewModel::signUp,
                onBackToLoginClick = {
                    authViewModel.clearFeedback()
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestination.AdminDashboard.route) {
            AdminDashboardScreen(
                user = authState.currentUser,
                topicState = topicState,
                onLoadTopics = topicViewModel::loadTopics,
                onCreateTopic = { title, description ->
                    authState.currentUser?.uid?.let { uid ->
                        topicViewModel.createTopic(title, description, uid)
                    }
                },
                onDeleteTopic = topicViewModel::deleteTopic,
                onOpenTopics = {
                    navController.navigate(AppDestination.Topics.createRoute("teacher"))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(AppDestination.StudentHome.route) {
            StudentHomeScreen(
                user = authState.currentUser,
                onOpenTopics = {
                    navController.navigate(AppDestination.Topics.createRoute("student"))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            route = AppDestination.Topics.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = authState.currentUser?.role?.normalizedRole()
                ?: backStackEntry.arguments?.getString("role").orEmpty().normalizedRole()
            TopicsScreen(
                role = role,
                uiState = topicState,
                onLoadTopics = topicViewModel::loadTopics,
                onTopicClick = { topicId ->
                    val route = if (role.isTeacherRole()) {
                        AppDestination.TopicContentManager.createRoute(topicId)
                    } else {
                        AppDestination.TopicDetail.createRoute(topicId, role)
                    }
                    navController.navigate(route)
                },
                onDeleteTopic = topicViewModel::deleteTopic,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.TopicContentManager.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            TopicContentManagerScreen(
                topic = topicState.topics.firstOrNull { it.topicId == topicId },
                onManageDocuments = {
                    navController.navigate(AppDestination.UploadDocument.createRoute(topicId))
                },
                onManageQuizzes = {
                    navController.navigate(AppDestination.QuizList.createRoute(topicId, "teacher"))
                },
                onManageQuestions = {
                    navController.navigate(AppDestination.ManageQuestions.createRoute(topicId))
                },
                onUploadMedia = {
                    navController.navigate(AppDestination.UploadMedia.createRoute(topicId))
                },
                onManageMedia = {
                    navController.navigate(AppDestination.ManageMedia.createRoute(topicId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.TopicDetail.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            val role = authState.currentUser?.role?.normalizedRole()
                ?: backStackEntry.arguments?.getString("role").orEmpty().normalizedRole()
            TopicDetailScreen(
                role = role,
                topic = topicState.topics.firstOrNull { it.topicId == topicId },
                documentState = documentState,
                onLoadDocuments = { documentViewModel.loadDocuments(topicId) },
                onOpenQuizzes = {
                    navController.navigate(AppDestination.QuizList.createRoute(topicId, role))
                },
                onAskQuestion = {
                    navController.navigate(AppDestination.AskQuestion.createRoute(topicId))
                },
                onViewQuestions = {
                    navController.navigate(AppDestination.QuestionsList.createRoute(topicId))
                },
                onOpenMedia = {
                    navController.navigate(AppDestination.MediaList.createRoute(topicId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.UploadDocument.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            UploadDocumentScreen(
                topicId = topicId,
                uiState = documentState,
                onTitleChange = documentViewModel::updateTitle,
                onFileSelected = documentViewModel::updateSelectedFile,
                onUploadClick = {
                    authState.currentUser?.uid?.let { uid ->
                        documentViewModel.uploadDocument(topicId, uid) {
                            notificationViewModel.createNotification(
                                topicId = topicId,
                                title = "New Tourism Document",
                                message = "A new document was uploaded for this topic.",
                                eventType = "document_uploaded",
                                createdBy = uid
                            )
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.UploadMedia.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            UploadMediaScreen(
                topicId = topicId,
                uiState = mediaState,
                onTitleChange = mediaViewModel::updateTitle,
                onDescriptionChange = mediaViewModel::updateDescription,
                onMediaTypeChange = mediaViewModel::updateMediaType,
                onFileSelected = mediaViewModel::updateSelectedFile,
                onUploadClick = {
                    authState.currentUser?.uid?.let { uid ->
                        mediaViewModel.uploadMedia(topicId, uid) {
                            notificationViewModel.createNotification(
                                topicId = topicId,
                                title = "New ${mediaState.selectedMediaType.replaceFirstChar { it.uppercase() }} Uploaded",
                                message = "New learning media is available for this topic.",
                                eventType = "media_uploaded",
                                createdBy = uid
                            )
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.ManageMedia.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            ManageMediaScreen(
                topicId = topicId,
                uiState = mediaState,
                onLoadMedia = { mediaViewModel.loadMedia(topicId) },
                onDeleteMedia = { mediaItem -> mediaViewModel.deleteMedia(topicId, mediaItem) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.MediaList.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            MediaListScreen(
                topicId = topicId,
                uiState = mediaState,
                onLoadMedia = { mediaViewModel.loadMedia(topicId) },
                onMediaClick = { mediaId, mediaType ->
                    val route = if (mediaType == "video") {
                        AppDestination.VideoPlayer.createRoute(mediaId)
                    } else {
                        AppDestination.AudioPlayer.createRoute(mediaId)
                    }
                    navController.navigate(route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.VideoPlayer.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId").orEmpty()
            LaunchedEffect(mediaId) {
                mediaViewModel.loadMediaItem(mediaId)
            }
            VideoPlayerScreen(
                mediaItem = mediaState.selectedMediaItem,
                isLoading = mediaState.isLoading,
                message = mediaState.message,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.AudioPlayer.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId").orEmpty()
            LaunchedEffect(mediaId) {
                mediaViewModel.loadMediaItem(mediaId)
            }
            AudioPlayerScreen(
                mediaItem = mediaState.selectedMediaItem,
                isLoading = mediaState.isLoading,
                message = mediaState.message,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.QuizList.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            val role = authState.currentUser?.role?.normalizedRole()
                ?: backStackEntry.arguments?.getString("role").orEmpty().normalizedRole()
            QuizListScreen(
                topicId = topicId,
                role = role,
                uiState = quizState,
                onLoadQuizzes = { quizViewModel.loadQuizzes(topicId) },
                onCreateQuizClick = {
                    navController.navigate(AppDestination.CreateQuiz.createRoute(topicId))
                },
                onQuizClick = { quizId, quizTitle ->
                    val destination = if (role.isTeacherRole()) {
                        AppDestination.AddQuestionToQuiz.createRoute(quizId)
                    } else {
                        AppDestination.Quiz.createRoute(quizId, quizTitle)
                    }
                    navController.navigate(destination)
                },
                onDeleteQuizClick = { quizId ->
                    quizViewModel.deleteQuiz(topicId, quizId)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.CreateQuiz.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            CreateQuizScreen(
                topicId = topicId,
                uiState = quizState,
                onTitleChange = quizViewModel::updateQuizTitle,
                onCreateQuizClick = {
                    authState.currentUser?.uid?.let { uid ->
                        quizViewModel.createQuiz(topicId, uid) { quizId ->
                            notificationViewModel.createNotification(
                                topicId = topicId,
                                title = "New Quiz Added",
                                message = "A new quiz is available for this topic.",
                                eventType = "quiz_created",
                                createdBy = uid
                            )
                            navController.navigate(AppDestination.AddQuestionToQuiz.createRoute(quizId)) {
                                popUpTo(AppDestination.CreateQuiz.route) { inclusive = true }
                            }
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.AddQuestionToQuiz.route,
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId").orEmpty()
            AddQuestionToQuizScreen(
                quizId = quizId,
                uiState = quizState,
                onLoadQuestions = { quizViewModel.loadQuestionsForQuiz(quizId, shuffle = false) },
                onQuestionTextChange = quizViewModel::updateQuestionText,
                onOptionAChange = quizViewModel::updateOptionA,
                onOptionBChange = quizViewModel::updateOptionB,
                onOptionCChange = quizViewModel::updateOptionC,
                onOptionDChange = quizViewModel::updateOptionD,
                onCorrectAnswerChange = quizViewModel::updateCorrectAnswer,
                onAddQuestionClick = { quizViewModel.addQuestionToQuiz(quizId) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.Quiz.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType },
                navArgument("quizTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId").orEmpty()
            val quizTitle = Uri.decode(backStackEntry.arguments?.getString("quizTitle").orEmpty())
            QuizScreen(
                quizId = quizId,
                quizTitle = quizTitle,
                uiState = quizState,
                onLoadQuestions = { quizViewModel.loadQuestionsForQuiz(quizId) },
                onSelectAnswer = quizViewModel::selectAnswer,
                onSubmitClick = {
                    authState.currentUser?.uid?.let { uid ->
                        quizViewModel.submitQuiz(uid, quizId, quizTitle) {
                            navController.navigate(AppDestination.QuizResult.route) {
                                popUpTo(AppDestination.Quiz.route) { inclusive = true }
                            }
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppDestination.QuizResult.route) {
            QuizResultScreen(
                quizTitle = quizState.latestQuizTitle,
                score = quizState.latestScore,
                total = quizState.latestTotal,
                onBackToQuizzes = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.AskQuestion.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            AskQuestionScreen(
                topicId = topicId,
                uiState = questionAnswerState,
                onQuestionTextChange = questionAnswerViewModel::updateAskQuestionInput,
                onSubmitClick = {
                    authState.currentUser?.uid?.let { uid ->
                        questionAnswerViewModel.askQuestion(topicId, uid) {
                            navController.popBackStack()
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.QuestionsList.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            QuestionsListScreen(
                topicId = topicId,
                uiState = questionAnswerState,
                onLoadQuestions = { questionAnswerViewModel.loadQuestions(topicId) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.ManageQuestions.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId").orEmpty()
            ManageQuestionsScreen(
                topicId = topicId,
                uiState = questionAnswerState,
                onLoadQuestions = { questionAnswerViewModel.loadQuestions(topicId) },
                onAnswerChange = questionAnswerViewModel::updateDraftAnswer,
                onSubmitAnswer = { questionId ->
                    authState.currentUser?.uid?.let { uid ->
                        questionAnswerViewModel.answerQuestion(topicId, questionId, uid) {
                            notificationViewModel.createNotification(
                                topicId = topicId,
                                title = "Question Answered",
                                message = "A teacher answered a question in this topic.",
                                eventType = "question_answered",
                                createdBy = uid
                            )
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
