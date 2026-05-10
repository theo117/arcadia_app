# Arcadia Tourism Phase 2

## Database Updates

Phase 2 keeps the existing Phase 1 collections:

- `users`
- `topics`
- `documents`

It adds five new collections:

### `quizzes`

```json
{
  "quizId": "auto-id",
  "topicId": "topic-id",
  "title": "Term 1 Quiz",
  "createdBy": "teacher-uid",
  "createdAt": 1714550400000
}
```

### `quizQuestions`

```json
{
  "questionId": "auto-id",
  "quizId": "quiz-id",
  "questionText": "Which type of tourism involves travel within South Africa?",
  "optionA": "Domestic tourism",
  "optionB": "Inbound tourism",
  "optionC": "Outbound tourism",
  "optionD": "Adventure tourism",
  "correctAnswer": "A"
}
```

### `quizResults`

```json
{
  "resultId": "auto-id",
  "studentId": "student-uid",
  "quizId": "quiz-id",
  "score": 4,
  "totalQuestions": 5,
  "submittedAt": 1714550400000
}
```

### `questions`

```json
{
  "questionId": "auto-id",
  "topicId": "topic-id",
  "studentId": "student-uid",
  "questionText": "What is the difference between domestic and regional tourism?",
  "status": "unanswered",
  "createdAt": 1714550400000
}
```

### `answers`

```json
{
  "answerId": "auto-id",
  "questionId": "question-id",
  "teacherId": "teacher-uid",
  "answerText": "Domestic tourism happens inside one country. Regional tourism happens across nearby countries in the same region.",
  "createdAt": 1714550400000
}
```

## Architecture

Phase 2 follows the same MVVM structure from Phase 1:

- `QuizRepository` handles `quizzes`, `quizQuestions`, and `quizResults`
- `QuestionAnswerRepository` handles `questions` and `answers`
- `QuizViewModel` manages teacher quiz creation and student quiz submission
- `QuestionAnswerViewModel` manages asking, loading, and answering topic questions

This keeps the Phase 2 code consistent with the existing repositories and viewmodels already used for authentication, topics, and documents.

## Integration With Phase 1

Phase 2 plugs into the existing topic flow:

1. User logs in the same way as Phase 1
2. User opens a topic from `TopicsScreen`
3. `TopicDetailScreen` now exposes extra actions
4. Teacher can:
   - upload documents
   - manage quizzes
   - manage student questions
5. Student can:
   - view documents
   - take quizzes
   - ask questions
   - view teacher answers

No Phase 1 collection names were changed, so existing topic and document data remains valid.

## New Screens

Student screens:

- `QuizListScreen`
- `QuizScreen`
- `QuizResultScreen`
- `AskQuestionScreen`
- `QuestionsListScreen`

Teacher screens:

- `CreateQuizScreen`
- `AddQuestionToQuizScreen`
- `ManageQuestionsScreen`

## Quiz Flow

Teacher flow:

1. Open topic
2. Tap `Manage Quizzes`
3. Tap `Create Quiz`
4. Enter quiz title
5. Add multiple-choice questions

Student flow:

1. Open topic
2. Tap `Take Quiz`
3. Choose a quiz
4. Answer all questions
5. Submit quiz
6. View score immediately

## Q&A Flow

Student flow:

1. Open topic
2. Tap `Ask A Question`
3. Submit question
4. Open `View Questions And Answers`
5. Read teacher replies

Teacher flow:

1. Open topic
2. Tap `Manage Questions`
3. Review student questions
4. Submit answers
5. Question status changes to `answered`

## Firestore Notes

You may need composite indexes for queries such as:

- `quizzes` by `topicId` and `createdAt`
- `questions` by `topicId` and `createdAt`
- `documents` by `topicId` and `uploadedAt`

If Firestore asks for an index during testing, use the Firebase Console link it provides and create the suggested index.
