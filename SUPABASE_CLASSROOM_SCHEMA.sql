create table if not exists public.users (
  uid uuid primary key references auth.users(id) on delete cascade,
  full_name text not null default '',
  email text not null default '',
  role text not null default 'student',
  grade text not null default '',
  fcm_token text not null default ''
);

create table if not exists public.topics (
  topic_id text primary key,
  title text not null default '',
  description text not null default '',
  created_by uuid not null references public.users(uid) on delete cascade,
  created_at bigint not null
);

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.users (uid, full_name, email, role, grade)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'full_name', ''),
    coalesce(new.email, ''),
    coalesce(new.raw_user_meta_data ->> 'role', 'student'),
    coalesce(new.raw_user_meta_data ->> 'grade', '')
  )
  on conflict (uid) do update
  set
    full_name = excluded.full_name,
    email = excluded.email,
    role = excluded.role,
    grade = excluded.grade;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute procedure public.handle_new_user();

insert into public.users (uid, full_name, email, role, grade)
select
  au.id,
  coalesce(au.raw_user_meta_data ->> 'full_name', ''),
  coalesce(au.email, ''),
  coalesce(au.raw_user_meta_data ->> 'role', 'student'),
  coalesce(au.raw_user_meta_data ->> 'grade', '')
from auth.users au
on conflict (uid) do nothing;

create index if not exists topics_created_by_idx on public.topics(created_by);

create or replace function public.is_teacher_or_admin()
returns boolean
language sql
security definer
set search_path = public
as $$
  select exists (
    select 1
    from public.users
    where uid = auth.uid()
      and role in ('teacher', 'admin')
  );
$$;

alter table public.users enable row level security;
alter table public.topics enable row level security;

drop policy if exists "users can read own profile" on public.users;
create policy "users can read own profile"
on public.users
for select
to authenticated
using (auth.uid() = uid);

drop policy if exists "teachers and admins can read user profiles" on public.users;
create policy "teachers and admins can read user profiles"
on public.users
for select
to authenticated
using (public.is_teacher_or_admin());

drop policy if exists "users can update own profile" on public.users;
create policy "users can update own profile"
on public.users
for update
to authenticated
using (auth.uid() = uid)
with check (auth.uid() = uid);

drop policy if exists "topics readable by authenticated users" on public.topics;
create policy "topics readable by authenticated users"
on public.topics
for select
to authenticated
using (true);

drop policy if exists "teachers can create topics" on public.topics;
create policy "teachers can create topics"
on public.topics
for insert
to authenticated
with check (
  auth.uid() = created_by and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "teachers can delete topics" on public.topics;
create policy "teachers can delete topics"
on public.topics
for delete
to authenticated
using (
  auth.uid() = created_by or exists (
    select 1
    from public.users
    where uid = auth.uid() and role = 'admin'
  )
);

create table if not exists public.questions (
  question_id text primary key,
  topic_id text not null references public.topics(topic_id) on delete cascade,
  student_id uuid not null references public.users(uid) on delete cascade,
  question_text text not null default '',
  status text not null default 'unanswered',
  created_at bigint not null
);

create table if not exists public.answers (
  answer_id text primary key,
  question_id text not null unique references public.questions(question_id) on delete cascade,
  teacher_id uuid not null references public.users(uid) on delete cascade,
  answer_text text not null default '',
  created_at bigint not null
);

create table if not exists public.quizzes (
  quiz_id text primary key,
  topic_id text not null references public.topics(topic_id) on delete cascade,
  title text not null default '',
  created_by uuid not null references public.users(uid) on delete cascade,
  created_at bigint not null
);

create table if not exists public.quiz_questions (
  question_id text primary key,
  quiz_id text not null references public.quizzes(quiz_id) on delete cascade,
  question_text text not null default '',
  option_a text not null default '',
  option_b text not null default '',
  option_c text not null default '',
  option_d text not null default '',
  correct_answer text not null default 'A',
  question_order integer not null
);

create table if not exists public.quiz_results (
  result_id text primary key,
  student_id uuid not null references public.users(uid) on delete cascade,
  quiz_id text not null references public.quizzes(quiz_id) on delete cascade,
  score integer not null default 0,
  total_questions integer not null default 0,
  submitted_at bigint not null
);

create table if not exists public.documents (
  document_id text primary key,
  topic_id text not null references public.topics(topic_id) on delete cascade,
  title text not null default '',
  file_url text not null default '',
  storage_path text not null default '',
  file_size_bytes bigint not null default 0,
  file_type text not null default 'document',
  uploaded_by uuid not null references public.users(uid) on delete cascade,
  uploaded_at bigint not null
);

create table if not exists public.media (
  media_id text primary key,
  topic_id text not null references public.topics(topic_id) on delete cascade,
  title text not null default '',
  description text not null default '',
  media_type text not null default 'video',
  file_url text not null default '',
  storage_path text not null default '',
  file_size_bytes bigint not null default 0,
  uploaded_by uuid not null references public.users(uid) on delete cascade,
  uploaded_at bigint not null
);

alter table public.documents
  add column if not exists storage_path text not null default '';

alter table public.documents
  add column if not exists file_size_bytes bigint not null default 0;

alter table public.media
  add column if not exists storage_path text not null default '';

alter table public.media
  add column if not exists file_size_bytes bigint not null default 0;

create table if not exists public.notifications (
  notification_id text primary key,
  topic_id text not null references public.topics(topic_id) on delete cascade,
  title text not null default '',
  message text not null default '',
  event_type text not null default '',
  target_role text not null default 'student',
  created_by uuid not null references public.users(uid) on delete cascade,
  created_at bigint not null
);

create unique index if not exists quiz_results_student_quiz_unique
  on public.quiz_results(student_id, quiz_id);

create index if not exists questions_topic_id_idx on public.questions(topic_id);
create index if not exists answers_question_id_idx on public.answers(question_id);
create index if not exists quizzes_topic_id_idx on public.quizzes(topic_id);
create index if not exists quiz_questions_quiz_id_order_idx on public.quiz_questions(quiz_id, question_order);
create index if not exists quiz_results_quiz_id_idx on public.quiz_results(quiz_id);
create index if not exists documents_topic_id_idx on public.documents(topic_id);
create index if not exists media_topic_id_idx on public.media(topic_id);
create index if not exists notifications_topic_id_idx on public.notifications(topic_id);

alter table public.questions enable row level security;
alter table public.answers enable row level security;
alter table public.quizzes enable row level security;
alter table public.quiz_questions enable row level security;
alter table public.quiz_results enable row level security;
alter table public.documents enable row level security;
alter table public.media enable row level security;
alter table public.notifications enable row level security;

drop policy if exists "questions read for authenticated users" on public.questions;
create policy "questions read for authenticated users"
on public.questions
for select
to authenticated
using (true);

drop policy if exists "students can ask questions" on public.questions;
create policy "students can ask questions"
on public.questions
for insert
to authenticated
with check (
  auth.uid() = student_id and exists (
    select 1
    from public.users
    where uid = auth.uid() and role = 'student'
  )
);

drop policy if exists "answers read for authenticated users" on public.answers;
create policy "answers read for authenticated users"
on public.answers
for select
to authenticated
using (true);

drop policy if exists "teachers can answer questions" on public.answers;
create policy "teachers can answer questions"
on public.answers
for insert
to authenticated
with check (
  auth.uid() = teacher_id and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "teachers can update question status" on public.questions;
create policy "teachers can update question status"
on public.questions
for update
to authenticated
using (
  exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
)
with check (
  exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "quizzes readable by authenticated users" on public.quizzes;
create policy "quizzes readable by authenticated users"
on public.quizzes
for select
to authenticated
using (true);

drop policy if exists "teachers can create quizzes" on public.quizzes;
create policy "teachers can create quizzes"
on public.quizzes
for insert
to authenticated
with check (
  auth.uid() = created_by and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "teachers can delete quizzes" on public.quizzes;
create policy "teachers can delete quizzes"
on public.quizzes
for delete
to authenticated
using (
  auth.uid() = created_by or exists (
    select 1
    from public.users
    where uid = auth.uid() and role = 'admin'
  )
);

drop policy if exists "quiz questions readable by authenticated users" on public.quiz_questions;
create policy "quiz questions readable by authenticated users"
on public.quiz_questions
for select
to authenticated
using (true);

drop policy if exists "teachers can manage quiz questions" on public.quiz_questions;
create policy "teachers can manage quiz questions"
on public.quiz_questions
for insert
to authenticated
with check (
  exists (
    select 1
    from public.quizzes q
    join public.users u on u.uid = auth.uid()
    where q.quiz_id = quiz_id
      and (q.created_by = auth.uid() or u.role = 'admin')
  )
);

drop policy if exists "quiz results readable by authenticated users" on public.quiz_results;
create policy "quiz results readable by authenticated users"
on public.quiz_results
for select
to authenticated
using (
  auth.uid() = student_id or exists (
    select 1
    from public.quizzes q
    join public.users u on u.uid = auth.uid()
    where q.quiz_id = quiz_id
      and (q.created_by = auth.uid() or u.role in ('teacher', 'admin'))
  )
);

drop policy if exists "students can submit quiz results" on public.quiz_results;
create policy "students can submit quiz results"
on public.quiz_results
for insert
to authenticated
with check (
  auth.uid() = student_id and exists (
    select 1
    from public.users
    where uid = auth.uid() and role = 'student'
  )
);

drop policy if exists "students can update own quiz results" on public.quiz_results;
create policy "students can update own quiz results"
on public.quiz_results
for update
to authenticated
using (auth.uid() = student_id)
with check (auth.uid() = student_id);

drop policy if exists "documents readable by authenticated users" on public.documents;
create policy "documents readable by authenticated users"
on public.documents
for select
to authenticated
using (true);

drop policy if exists "teachers can insert documents" on public.documents;
create policy "teachers can insert documents"
on public.documents
for insert
to authenticated
with check (
  auth.uid() = uploaded_by and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "media readable by authenticated users" on public.media;
create policy "media readable by authenticated users"
on public.media
for select
to authenticated
using (true);

drop policy if exists "teachers can manage media" on public.media;
create policy "teachers can manage media"
on public.media
for insert
to authenticated
with check (
  auth.uid() = uploaded_by and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "teachers can delete media" on public.media;
create policy "teachers can delete media"
on public.media
for delete
to authenticated
using (
  auth.uid() = uploaded_by or exists (
    select 1
    from public.users
    where uid = auth.uid() and role = 'admin'
  )
);

drop policy if exists "notifications readable by authenticated users" on public.notifications;
create policy "notifications readable by authenticated users"
on public.notifications
for select
to authenticated
using (true);

drop policy if exists "teachers can insert notifications" on public.notifications;
create policy "teachers can insert notifications"
on public.notifications
for insert
to authenticated
with check (
  auth.uid() = created_by and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

insert into storage.buckets (id, name, public)
values ('topic-documents', 'topic-documents', true)
on conflict (id) do nothing;

insert into storage.buckets (id, name, public)
values ('topic-media', 'topic-media', true)
on conflict (id) do nothing;

drop policy if exists "public document downloads" on storage.objects;
create policy "public document downloads"
on storage.objects
for select
to public
using (bucket_id = 'topic-documents');

drop policy if exists "teachers can upload topic documents" on storage.objects;
create policy "teachers can upload topic documents"
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'topic-documents' and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "teachers can delete topic documents" on storage.objects;
create policy "teachers can delete topic documents"
on storage.objects
for delete
to authenticated
using (
  bucket_id = 'topic-documents' and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "public media downloads" on storage.objects;
create policy "public media downloads"
on storage.objects
for select
to public
using (bucket_id = 'topic-media');

drop policy if exists "teachers can upload topic media" on storage.objects;
create policy "teachers can upload topic media"
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'topic-media' and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);

drop policy if exists "teachers can delete topic media" on storage.objects;
create policy "teachers can delete topic media"
on storage.objects
for delete
to authenticated
using (
  bucket_id = 'topic-media' and exists (
    select 1
    from public.users
    where uid = auth.uid() and role in ('teacher', 'admin')
  )
);
