package com.teodordevtech.arcadiatourism.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teodordevtech.arcadiatourism.data.remote.SupabaseProvider
import com.teodordevtech.arcadiatourism.data.repository.AuthRepository
import com.teodordevtech.arcadiatourism.data.repository.DocumentRepository
import com.teodordevtech.arcadiatourism.data.repository.MediaRepository
import com.teodordevtech.arcadiatourism.data.repository.QuestionAnswerRepository
import com.teodordevtech.arcadiatourism.data.repository.NotificationRepository
import com.teodordevtech.arcadiatourism.data.repository.QuizRepository
import com.teodordevtech.arcadiatourism.data.repository.TopicRepository
import com.teodordevtech.arcadiatourism.ui.navigation.AppNavGraph
import com.teodordevtech.arcadiatourism.ui.viewmodel.AuthViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.AuthViewModelFactory
import com.teodordevtech.arcadiatourism.ui.viewmodel.DocumentViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.DocumentViewModelFactory
import com.teodordevtech.arcadiatourism.ui.viewmodel.MediaViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.MediaViewModelFactory
import com.teodordevtech.arcadiatourism.ui.viewmodel.NotificationViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.NotificationViewModelFactory
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuestionAnswerViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuestionAnswerViewModelFactory
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuizViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.QuizViewModelFactory
import com.teodordevtech.arcadiatourism.ui.viewmodel.TopicViewModel
import com.teodordevtech.arcadiatourism.ui.viewmodel.TopicViewModelFactory

@Composable
fun ArcadiaTourismApp() {
    NotificationPermissionEffect()

    val application = LocalContext.current.applicationContext as Application
    remember { SupabaseProvider.client }
    val authRepository = remember { AuthRepository() }
    val topicRepository = remember { TopicRepository() }
    val documentRepository = remember { DocumentRepository() }
    val mediaRepository = remember { MediaRepository() }
    val quizRepository = remember { QuizRepository() }
    val questionAnswerRepository = remember { QuestionAnswerRepository() }
    val notificationRepository = remember { NotificationRepository() }

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))
    val topicViewModel: TopicViewModel = viewModel(factory = TopicViewModelFactory(topicRepository))
    val documentViewModel: DocumentViewModel = viewModel(
        factory = DocumentViewModelFactory(application, documentRepository)
    )
    val mediaViewModel: MediaViewModel = viewModel(
        factory = MediaViewModelFactory(application, mediaRepository)
    )
    val quizViewModel: QuizViewModel = viewModel(factory = QuizViewModelFactory(quizRepository))
    val questionAnswerViewModel: QuestionAnswerViewModel = viewModel(
        factory = QuestionAnswerViewModelFactory(questionAnswerRepository)
    )
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(notificationRepository)
    )

    AppNavGraph(
        authViewModel = authViewModel,
        topicViewModel = topicViewModel,
        documentViewModel = documentViewModel,
        mediaViewModel = mediaViewModel,
        quizViewModel = quizViewModel,
        questionAnswerViewModel = questionAnswerViewModel,
        notificationViewModel = notificationViewModel
    )
}
