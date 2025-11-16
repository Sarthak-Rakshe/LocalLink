# LocalLink Frontend (React + Vite + Tailwind)

Modern frontend for the Spring Boot microservices backend, using React Router, TanStack Query, Axios, and PayPal Sandbox. Tailwind CSS v4 is enabled.

## Quick start

1. Create a `.env.local` file in this folder with:

```
VITE_API_BASE_URL=http://localhost:8080
VITE_PAYPAL_CLIENT_ID=YOUR_SANDBOX_CLIENT_ID
```

2. Install dependencies and run:

```powershell
npm install
npm run dev
```

3. Build for production:

```powershell
npm run build
npm run preview
```

## What’s included

- React Router (central router in `src/app/routes.jsx`)
- Global providers in `src/app/AppProviders.jsx` (Auth, Query, PayPal, Toasts)
- Axios client with `withCredentials` in `src/services/apiClient.js`
- Basic auth flow scaffolding (`AuthContext`, login page)

## Design system overview

This UI has a lightweight design system powered by Tailwind v4 and small reusable components.

- Tokens: defined in `src/index.css` under `@theme` (brand colors, radii, fonts). The default font is Inter from Google Fonts.
- Layout: `AppShell` wraps all protected routes with `Navbar`, `SideBar`, and a `Footer`. Content uses a responsive container (`container-page`).
- Primitives:
  - `Button` (variants: `primary`, `secondary`, `outline`, `ghost`, `danger`; sizes: `sm|md|lg`)
  - `Input`, `Label`, `HelpText`
  - `Card` with optional `title`, `description`, and `action`
  - Utility classes like `.container-page`, `.card`, `.btn-*` in `index.css`

Guidelines:

- Prefer the shared components over ad‑hoc classes for consistency and accessibility.
- Use semantic headings and keep page content within `container-page` for proper spacing.
- Brand color is Indigo; use `text-indigo-*`, `bg-indigo-*`, and the `primary` button variant for primary actions.

## Environment & cookies

- The backend should set an HTTP-only cookie on login.
- CORS must allow credentials and the frontend origin.
- All API requests use `withCredentials: true`.

## Next steps

See `FRONTEND_PLAN.md` for the full phase-wise implementation plan.
