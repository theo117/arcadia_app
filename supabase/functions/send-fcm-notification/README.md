# send-fcm-notification

Supabase Edge Function that receives a database webhook from `public.notifications`
and sends a Firebase Cloud Messaging push to the matching classroom topic.

## What it sends

- `target_role = "student"` -> FCM topic `grade12_student`
- `target_role = "teacher"` -> FCM topic `grade12_teacher`
- `target_role = "admin"` -> FCM topic `grade12_admin`

The Android app already subscribes users to:

- `grade12_student`
- `grade12_teacher`
- `grade12_admin`

through `NotificationRepository.subscribeToRoleTopic(...)`.

## Required Supabase secrets

Set these in your Supabase project:

```bash
supabase secrets set NOTIFICATION_WEBHOOK_SECRET=your-random-shared-secret
supabase secrets set FIREBASE_SERVICE_ACCOUNT_JSON='{"type":"service_account","project_id":"..."}'
supabase secrets set FCM_TOPIC_PREFIX=grade12_
```

`FCM_TOPIC_PREFIX` is optional. The function defaults to `grade12_`.

## Firebase requirement

Use a Firebase service account JSON with permission to send FCM HTTP v1 messages.

## Deploy

```bash
supabase functions deploy send-fcm-notification --no-verify-jwt
```

`--no-verify-jwt` is important because the function is called by a database webhook,
not directly by a logged-in app user.

## After deploy

Run the SQL in `SUPABASE_NOTIFICATION_AUTOMATION.sql` and replace:

- `<your-project-ref>`
- `<your-webhook-secret>`

Then every inserted row in `public.notifications` will trigger a push.
