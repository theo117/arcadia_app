create extension if not exists pg_net;

drop trigger if exists notify_send_fcm on public.notifications;

create trigger notify_send_fcm
after insert on public.notifications
for each row
execute function supabase_functions.http_request(
  'https://<your-project-ref>.supabase.co/functions/v1/send-fcm-notification',
  'POST',
  '{"Content-Type":"application/json","x-webhook-secret":"<your-webhook-secret>"}',
  '{}',
  '5000'
);
