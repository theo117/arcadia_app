import { JWT } from "npm:google-auth-library@9"

type NotificationRecord = {
  notification_id: string
  topic_id: string
  title: string
  message: string
  event_type: string
  target_role: string
  created_by: string
  created_at: number
}

type WebhookPayload<T> = {
  type: "INSERT" | "UPDATE" | "DELETE"
  table: string
  schema: string
  record: T
  old_record: T | null
}

const webhookSecret = Deno.env.get("NOTIFICATION_WEBHOOK_SECRET") ?? ""
const fcmTopicPrefix = Deno.env.get("FCM_TOPIC_PREFIX") ?? "grade12_"
const firebaseServiceAccountJson = Deno.env.get("FIREBASE_SERVICE_ACCOUNT_JSON") ?? ""

function jsonResponse(status: number, body: Record<string, unknown>) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  })
}

async function getAccessToken(): Promise<string> {
  const serviceAccount = JSON.parse(firebaseServiceAccountJson)
  const jwtClient = new JWT({
    email: serviceAccount.client_email,
    key: serviceAccount.private_key,
    scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
  })

  const credentials = await jwtClient.authorize()
  if (!credentials.access_token) {
    throw new Error("Failed to obtain Firebase access token.")
  }

  return credentials.access_token
}

function buildTopic(role: string): string {
  return `${fcmTopicPrefix}${role.trim().toLowerCase()}`
}

Deno.serve(async (request) => {
  if (request.method !== "POST") {
    return jsonResponse(405, { error: "Method not allowed." })
  }

  const requestSecret = request.headers.get("x-webhook-secret") ?? ""
  if (!webhookSecret || requestSecret !== webhookSecret) {
    return jsonResponse(401, { error: "Unauthorized webhook request." })
  }

  if (!firebaseServiceAccountJson) {
    return jsonResponse(500, { error: "Missing FIREBASE_SERVICE_ACCOUNT_JSON secret." })
  }

  const payload = (await request.json()) as WebhookPayload<NotificationRecord>
  if (payload.type !== "INSERT") {
    return jsonResponse(200, { ignored: true, reason: "Only INSERT events are handled." })
  }

  const record = payload.record
  const serviceAccount = JSON.parse(firebaseServiceAccountJson)
  const accessToken = await getAccessToken()
  const fcmUrl = `https://fcm.googleapis.com/v1/projects/${serviceAccount.project_id}/messages:send`

  const message = {
    message: {
      topic: buildTopic(record.target_role),
      notification: {
        title: record.title,
        body: record.message,
      },
      data: {
        notificationId: record.notification_id,
        topicId: record.topic_id,
        eventType: record.event_type,
        targetRole: record.target_role,
        createdAt: String(record.created_at),
      },
      android: {
        priority: "high",
      },
    },
  }

  const response = await fetch(fcmUrl, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(message),
  })

  if (!response.ok) {
    const errorText = await response.text()
    console.error("FCM send failed", errorText)
    return jsonResponse(502, { error: "FCM send failed.", details: errorText })
  }

  const responseBody = await response.json()
  return jsonResponse(200, {
    delivered: true,
    topic: buildTopic(record.target_role),
    fcmResponse: responseBody,
  })
})
