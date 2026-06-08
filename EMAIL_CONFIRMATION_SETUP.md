# Email Confirmation Setup

To use the custom confirmation page and return users to the Android app cleanly:

1. Deploy the `web/` folder to Firebase Hosting or another HTTPS host.
2. Copy the public URL for `email-confirmed.html`.
   Example: `https://your-site.web.app/email-confirmed.html`
3. Add this to `local.properties` or `gradle.properties`:

```properties
AUTH_CONFIRMATION_URL=https://your-site.web.app/email-confirmed.html
```

4. In Supabase Auth settings:
   - Set the Site URL to the same hosted page, or another real HTTPS page you control.
   - Add both of these to Redirect URLs:
     - `https://your-site.web.app/email-confirmed.html`
     - `arcadia://login-callback`
5. In Supabase `Authentication -> Email Templates -> Confirm signup`, use the template from `supabase/templates/confirm-signup-template.html`.
   - The important part is that the link uses `{{ .ConfirmationURL }}`.
   - Do not hardcode `localhost:3000`.

Result:
- The browser shows `Thank you for confirming your email`.
- After a short delay it redirects to `arcadia://login-callback`.
- The app opens and shows the same confirmation message on the login screen.
