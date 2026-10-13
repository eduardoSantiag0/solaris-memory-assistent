# solaris-memory-assistent

🇺🇸 English (this file) · 🇧🇷 [Português](README.pt-BR.md)

A private personal memory assistant: register small everyday facts through
explicit commands (`/add`, `/add-temp`) and retrieve them later by asking in
natural language. It's not a note-taking app, a task manager, or a
general-purpose assistant — its only job is to remember what you explicitly
told it to, and to always show the record behind its answer. Built from a
private Portuguese requirements document (product vision, functional/
non-functional requirements, acceptance criteria) that isn't part of this
repository — code comments and this README cite it as RF-xx/RNF-xx/CT-xx
and section numbers for traceability.

## Why "Solaris"?

The name comes from Andrei Tarkovsky's 1972 film *Solaris*, in which a
sentient ocean reconstructs fragments of its visitors' memories — not as a
faithful archive, but by recreating and transforming what it finds in the
human mind.

This project deliberately inverts that premise. Where the film's Solaris
blurs memory and invention together, this system is built to keep that line
sharp: every answer traces back to something the user explicitly wrote
down, and the retrieval layer (RAG) is designed to distinguish a recorded
memory from anything the model might generate (see RF-04/CT-05/CT-11 below
— there's no synthesis step that could invent a fact, only retrieval). The
name still fits, though, because that tension — between remembering,
reconstructing, and understanding — is exactly what the product has to get
right every time it answers a question.

*Solaris — a memory that speaks back.*

## Decisions made in this phase

(beyond what the requirements document specifies)

- **Frontend**: Compose Multiplatform (Kotlin), running on Android and
  Desktop from the same codebase — the doc specifies native Android only,
  but real usage needs desktop access too.
- **Scope**: backend M1 (local core, doc section 14) first, then a first
  UI slice (navigation + theme + chat + memories + settings) wired to it.
- **Field-level encryption**: the backend runs on a server shared with two
  other people, so the `content` field is encrypted with AES-256-GCM *in
  the application*, not just on disk — even someone with direct Postgres
  access (psql, a dump) only sees ciphertext. This resolves one of the
  doc's own open points (section 15, "field encryption beyond disk").
  Technical consequence: text search can no longer happen in SQL (`LIKE`
  doesn't work on ciphertext) — the search engine fetches active
  candidates by the non-encrypted columns and matches decrypted text in
  the application layer. That's fine at the personal scale of memories the
  doc assumes; it wouldn't scale to millions of rows.
- **Dark mode**: supported from this first UI version, using semantic
  color tokens (background, surface, text, border, accent — see
  `theme/Color.kt`) with two value sets, the same theming pattern as the
  Obsidian theme "retronotes" used as a visual reference
  (`https://github.com/sr-campelo/retronotes`: variables like
  `--background-primary`/`--text-normal` redefined per mode). The toggle
  lives in Settings (doc section 10.3) and swaps the whole app, verified by
  actually running the desktop app.
- **Semantic search embeddings are stored unencrypted** in pgvector even
  though they're derived from the same text `content` encrypts — accepted
  tradeoff, since pgvector needs raw floats to compute distance; there's no
  way to search an encrypted vector with it.

## What's here

`backend/` — Spring Boot API (Java 17) covering roadmap milestones M1 and M3:

- `POST /api/v1/memories` — creates a permanent memory (no `ttlDays`) or a
  temporary one (with `ttlDays`, expiring in exactly `ttlDays × 24h`).
- `GET /api/v1/memories` — paginated list, filters `status`
  (active/expired/all), `type` (permanent/temporary/all), and `q` (text
  search).
- `GET /api/v1/memories/{id}`, `PATCH /api/v1/memories/{id}`,
  `DELETE /api/v1/memories/{id}`.
- `POST /api/v1/chat/query` — natural-language search, hybrid (M3): exact
  text first (RF-05 — never overridden by similarity, only supplemented),
  and only when that finds nothing does semantic search kick in
  (embeddings via pgvector + Ollama). The answer is always the literal
  content of a memory that was found, never generated text — there's
  nothing that could "invent" facts because there's no LLM synthesis step,
  only retrieval. When an answer comes only from semantic search, that's
  flagged explicitly in `warnings` (RF-13), never presented as if it were
  an exact match.
- `POST /api/v1/auth/login`, `POST /api/v1/auth/logout` — single user (JWT
  + a revocation watermark in `auth_state`), with login rate limiting.
- `GET /api/v1/health`.

Relevant details:
- Memory expiration is always filtered at query time (`expiresAt is null or
  expiresAt > now()`), never depends on a cleanup job having run (section
  5.1).
- `clientRequestId` gives idempotency to `POST /memories` (a retry after a
  network failure won't duplicate it) and to `POST /chat/query` (short
  in-memory cache).
- Simple Portuguese relative-date expressions (`ontem`/yesterday,
  `hoje`/today, `semana passada`/last week) resolve against `eventAt`,
  never against `createdAt` (RF-12).
- **Embeddings (M3)**: when a memory's text is created/edited, an embedding
  (Ollama, `nomic-embed-text` model, 768 dimensions) is generated in the
  background (`EmbeddingExecutor`) and stored in a Postgres `vector` column
  (the `pgvector` extension, HNSW index) — never in the same transaction as
  the write, never blocking the HTTP response (RNF-07), and an
  unavailable/failing Ollama never prevents the memory from being saved
  (RF-10). Optional via `app.embeddings.enabled` (defaults to `true` here;
  turn it off if there's no Ollama in the environment).

`frontend/` — Compose Multiplatform app (Kotlin; Android and Desktop from
the same `composeApp` module):

- A minimal design system in `components/` (`PixelButton`, `RetroTextField`,
  `RetroChatBubble`, `MemoryCard`) following the doc's "Colorful Retro
  Macintosh Pixel UI" aesthetic (section 11): thick black outlines, rigid
  offset shadows (never blur), no heavily rounded corners. The official
  palette from section 11.2 lives in `theme/Color.kt`, with a derived dark
  variant (see the dark mode decision above).
- Three screens (`screens/`): Chat (`/add`, `/add-temp` commands, natural
  language questions), Memories (list, time remaining, delete), Settings
  (theme, server, login). Bottom tab navigation — laid out for mobile, but
  without requiring all three panels at once on larger screens (section
  11.3).
- `network/ApiClient.kt` talks to the backend above via Ktor.
- `settings/AppSettings.kt` persists theme, server URL, and session (token)
  locally — SharedPreferences on Android, `java.util.prefs` on Desktop,
  same API via `multiplatform-settings`. On launch with a saved session, it
  tries to reuse it before falling back to the login screen. Stored in
  plaintext: this protects against "forgot to persist it," not a stolen
  device — a different, lower-priority threat than the backend's
  encryption, which defends against other accounts on the same shared
  server. Tested in `desktopTest` against the real production code path
  (rather than automating a click on the window, which turned out to be
  unreliable in this environment).

`deploy/` — production stack for the target VM (`falcon.whatbox.ca`,
confirmed to have a real public IP, not behind CGNAT): Postgres+pgvector,
the API, and WireGuard via Docker Compose, with only the WireGuard UDP port
ever exposed to the internet (RNF-01). See `deploy/README.md` for the
step-by-step — **not yet verified against the actual VM**, since this
session has no access to it.

## Running locally

```bash
docker compose up -d                 # Postgres (with pgvector) on localhost:5434
ollama pull nomic-embed-text          # only needed for semantic search (M3)
cd backend
mvn spring-boot:run
```

Environment variables (all have development defaults in
`application.yml` — **generate real values before any use beyond your own
laptop**):

| Variable | What for |
|---|---|
| `APP_ENCRYPTION_KEY` | AES-256 key (base64) for the `content` field. Generate with `python -c "import os,base64; print(base64.b64encode(os.urandom(32)).decode())"` |
| `APP_JWT_SECRET` | token signing secret (≥32 bytes) |
| `APP_USER` / `APP_PASSWORD_HASH` | single user; BCrypt password hash |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Postgres connection |
| `APP_EMBEDDINGS_ENABLED` | `false` turns semantic search (M3) off entirely |
| `APP_OLLAMA_URL` | where Ollama is (default `http://localhost:11434`) |

### Tests

```bash
cd backend
mvn test
```

Uses Testcontainers (requires Docker running) to test against a real
Postgres, including the doc's acceptance scenarios (CT-01 through CT-11;
CT-12 is a UI test and CT-07 depends on the client, neither testable here)
and an explicit check that content really is encrypted in the column. The
semantic search test (`SemanticSearchIntegrationTest`) talks to the real
Ollama on the machine and skips itself when it isn't reachable, rather than
failing.

Note: Surefire runs with `reuseForks=false` (one JVM per test class) — without
it, the last class in a full suite run would occasionally fail to connect to
its own, otherwise-healthy, Postgres container (connection refused right
after it started); isolating the process eliminated the problem.

### Frontend

```bash
cd frontend
./gradlew :composeApp:run            # desktop app
./gradlew :composeApp:assembleDebug  # debug APK (needs the Android SDK)
```

## Next steps (not implemented yet)

- Real bitmap/pixel font for the typography (currently a bold system font
  as an approximation) and decorative dithering (sections 11.1/11.6).
- Verify the `deploy/` WireGuard + Docker Compose stack against the real
  VM (not yet accessible from this session).
- Encrypted backups: `deploy/backup.sh` exists but retention policy
  (delete for good vs. keep an archived history) is still an open decision
  from the doc itself (section 15) — current default is a sane guess, not
  that decision made for you.
