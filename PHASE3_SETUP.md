# Arcadia Tourism Phase 3

## Updated Architecture

Phase 3 extends the existing topic-based MVVM structure with two new flows:

- `media`
  - short video and audio uploads per topic
  - playback screens for students
- `notifications`
  - Firebase Cloud Messaging token setup
  - Firestore notification records for classroom updates

This keeps the app scalable without changing the Phase 1 and Phase 2 collection structure.

## Firestore Updates

Phase 3 adds:

### `media`

```json
{
  "mediaId": "auto-id",
  "topicId": "topic-id",
  "title": "Tourism Terminology Clip",
  "description": "Short explanation of domestic vs regional tourism",
  "mediaType": "video",
  "fileUrl": "https://...",
  "storagePath": "media/videos/topic-id/file.mp4",
  "uploadedBy": "teacher-uid",
  "uploadedAt": 1714550400000
}
```

### `notifications`

```json
{
  "notificationId": "auto-id",
  "topicId": "topic-id",
  "title": "New Quiz Added",
  "message": "A new quiz is available for this topic.",
  "eventType": "quiz_created",
  "targetRole": "student",
  "createdBy": "teacher-uid",
  "createdAt": 1714550400000
}
```

The `users` document can now also include:

```json
{
  "fcmToken": "device-token"
}
```

## Storage Structure

Use Firebase Storage folders:

- `/media/videos/{topicId}/{fileName}`
- `/media/audio/{topicId}/{fileName}`

Documents remain in:

- `/topic_documents/{topicId}/{fileName}`

## New Classes

Data layer:

- `MediaItem`
- `NotificationItem`
- `MediaRepository`
- `NotificationRepository`

ViewModels:

- `MediaViewModel`
- `NotificationViewModel`

Messaging:

- `ArcadiaFirebaseMessagingService`

## New Screens

Teacher:

- `TopicContentManagerScreen`
- `UploadMediaScreen`
- `ManageMediaScreen`

Student:

- updated `TopicDetailScreen`
- `MediaListScreen`
- `VideoPlayerScreen`
- `AudioPlayerScreen`

## How It Connects To Phase 1 And Phase 2

Phase 1 gave each topic:

- login-based access
- documents

Phase 2 added:

- quizzes
- questions and answers

Phase 3 keeps the same topic as the central unit and adds:

- short videos
- audio explanations
- notification records
- FCM client setup

That means one Tourism topic now supports:

- notes
- quizzes
- Q&A
- videos
- audio

## Notification Logic

For MVP, the app now does three basic things:

1. Requests notification permission on Android 13+
2. Stores the user FCM token in Firestore
3. Subscribes the device to a role topic such as `grade12_student` or `grade12_teacher`

The app also creates Firestore notification records when:

- a document is uploaded
- a quiz is created
- a question is answered
- media is uploaded

For actual push delivery in Firebase Cloud Messaging, use one of these simple MVP options:

1. Send a topic notification manually from Firebase Console to `grade12_student`
2. Add a small Cloud Function later that watches the `notifications` collection and sends FCM automatically

This keeps Phase 3 simple while preparing the app for proper push automation later.

## Media Guidelines

- keep videos short
- keep audio short
- prefer compressed MP4 and MP3/M4A files
- avoid large files for mobile data usage

## Firestore Index Notes

You may need indexes for:

- `media` by `topicId` and `uploadedAt`
- `notifications` by `createdAt`

If Firestore prompts for an index during testing, create the suggested index from the Firebase Console link.
