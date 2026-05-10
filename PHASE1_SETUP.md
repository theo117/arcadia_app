# Arcadia Tourism Phase 1

## Architecture

This app uses a simple MVVM structure:

- `data/model` for Firestore data classes
- `data/repository` for Firebase Authentication, Firestore, and Storage logic
- `ui/viewmodel` for UI state and business logic
- `ui/screens` for Jetpack Compose screens
- `ui/navigation` for role-based navigation
- `ui/theme` for app colors and Material theme

App flow:

1. Open app
2. Login with email and password
3. Read the user's Firestore profile from `users`
4. Check `role`
5. Teacher goes to `AdminDashboardScreen`
6. Student goes to `StudentHomeScreen`

## Folder Structure

```text
app/
  src/main/
    java/com/teodordevtech/arcadiatourism/
      data/
        model/
        remote/
        repository/
      ui/
        navigation/
        screens/
          admin/
          auth/
          student/
          topics/
        theme/
        viewmodel/
      MainActivity.kt
```

## Firestore Structure

### `users`

```json
{
  "uid": "firebase-auth-uid",
  "fullName": "Teacher Name",
  "email": "teacher@school.co.za",
  "role": "teacher",
  "grade": "12"
}
```

### `topics`

```json
{
  "topicId": "auto-id",
  "title": "Domestic, Regional and International Tourism",
  "description": "Grade 12 CAPS module starter topic",
  "createdBy": "teacher-uid",
  "createdAt": 1714550400000
}
```

### `documents`

```json
{
  "documentId": "auto-id",
  "topicId": "topic-id",
  "title": "Term 1 Notes",
  "fileUrl": "https://...",
  "fileType": "pdf",
  "uploadedBy": "teacher-uid",
  "uploadedAt": 1714550400000
}
```

## Phase 1 Curriculum Topic

Phase 1 implements one Grade 12 Tourism curriculum topic:

- `Domestic, Regional and International Tourism`

This is the initial topic because it is a real CAPS Tourism section and can later expand into:

- multiple modules
- subtopics
- quizzes
- audio and video
- question and answer

## Firebase Setup

1. Create a Firebase project.
2. Add Android app package `com.teodordevtech.arcadiatourism`.
3. Download `google-services.json`.
4. Place it inside `app/`.
5. Enable Email/Password in Firebase Authentication.
6. Create Firestore Database.
7. Create Firebase Storage.
8. Add one teacher account and 20 to 30 student accounts.
9. Create a matching Firestore profile in `users/{uid}` for each account.

## Future Expansion

Keep the current top-level collections as required:

- `users`
- `topics`
- `documents`

Later, expand each topic with subcollections such as:

- `topics/{topicId}/subtopics`
- `topics/{topicId}/quizzes`
- `topics/{topicId}/media`
- `topics/{topicId}/questions`
