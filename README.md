# solaris-memory-assistent

Assistente pessoal de memória privada. Especificação completa em
`Zerkalo_Documento_de_Requisitos_v1.docx`. Este README cobre o que já existe
no repositório e as decisões de arquitetura tomadas além do documento.

## Decisões tomadas nesta fase

- **Frontend**: Compose Multiplatform (Kotlin), rodando em Android e Desktop
  a partir da mesma base de código — o documento especifica Android nativo,
  mas o uso real exige acesso também pelo desktop. Ainda não implementado
  (ver "Próximos passos").
- **Escopo desta entrega**: apenas o backend (M1 — Núcleo local do roadmap,
  seção 14 do documento). Frontend vem depois.
- **Criptografia de campo**: o backend vai rodar num servidor compartilhado
  com outras duas pessoas, então o campo `content` das memórias é
  criptografado com AES-256-GCM *na aplicação*, não só no disco — mesmo
  alguém com acesso direto ao Postgres (psql, dump) só vê texto cifrado. Isso
  decide a favor de um dos pontos abertos da seção 15 do documento
  ("Criptografia por campo além de disco"). Consequência técnica: busca por
  texto não pode mais acontecer em SQL (`LIKE` não funciona sobre texto
  cifrado) — o motor de busca busca os candidatos ativos pelas colunas não
  cifradas (status, datas) e filtra por texto já descriptografado na
  aplicação. Isso é adequado para o volume pessoal de memórias que o
  documento assume; não escalaria para milhões de linhas.
- **Dark mode**: quando a UI (Compose Multiplatform) for implementada, terá
  suporte a tema claro/escuro desde a primeira versão, usando tokens de cor
  semânticos (fundo, superfície, texto, borda, destaque) com dois conjuntos
  de valores — um padrão de theming já comum em apps com paleta
  personalizada (ex.: tema Obsidian "retronotes", usado como referência:
  `https://github.com/sr-campelo/retronotes`). A alternância fica em
  Ajustes, como já previsto na seção 10.3 do documento.

## O que existe

`backend/` — API Spring Boot (Java 17) cobrindo o M1 do roadmap:

- `POST /api/v1/memories` — cria memória permanente (sem `ttlDays`) ou
  temporária (com `ttlDays`, expira em `ttlDays × 24h` exatas).
- `GET /api/v1/memories` — lista paginada, filtros `status` (active/expired/all),
  `type` (permanent/temporary/all) e `q` (busca textual).
- `GET /api/v1/memories/{id}`, `PATCH /api/v1/memories/{id}`, `DELETE /api/v1/memories/{id}`.
- `POST /api/v1/chat/query` — busca em linguagem natural. M1 não tem
  embeddings nem LLM (o documento permite começar só com Postgres e busca
  textual, seção 6); a resposta é sempre o conteúdo literal de uma memória
  encontrada, nunca texto gerado — não há como "inventar" fatos porque não
  há geração nenhuma ainda.
- `POST /api/v1/auth/login`, `POST /api/v1/auth/logout` — usuário único
  (JWT + watermark de revogação em `auth_state`), com limitador de tentativas
  de login.
- `GET /api/v1/health`.

Detalhes relevantes:
- Expiração de memórias é sempre filtrada na consulta (`expiresAt is null or
  expiresAt > now()`), nunca depende de um job de limpeza ter rodado (seção 5.1).
- `clientRequestId` dá idempotência em `POST /memories` (reenvio após falha
  de rede não duplica) e em `POST /chat/query` (cache curto em memória).
- Expressões temporais simples em português (`ontem`, `hoje`, `semana
  passada`) resolvem contra `eventAt`, nunca contra `createdAt` (RF-12).

## Rodando localmente

```bash
docker compose up -d                 # Postgres em localhost:5434
cd backend
mvn spring-boot:run
```

Variáveis de ambiente (todas têm default de desenvolvimento em
`application.yml` — **gere valores reais antes de qualquer uso além do seu
próprio laptop**):

| Variável | Para quê |
|---|---|
| `APP_ENCRYPTION_KEY` | chave AES-256 (base64) do campo `content`. Gerar com `python -c "import os,base64; print(base64.b64encode(os.urandom(32)).decode())"` |
| `APP_JWT_SECRET` | segredo de assinatura dos tokens (≥32 bytes) |
| `APP_USER` / `APP_PASSWORD_HASH` | usuário único; hash BCrypt da senha |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | conexão Postgres |

### Testes

```bash
cd backend
mvn test
```

Usa Testcontainers (requer Docker rodando) para testar contra um Postgres
real, incluindo os casos de aceite do documento (CT-01 a CT-11; CT-12 é de
UI e CT-07 depende do cliente, não testáveis aqui) e uma verificação de que
o conteúdo realmente está cifrado na coluna.

## Próximos passos (não implementados ainda)

- M2: frontend Compose Multiplatform (Android + Desktop), com dark mode.
- M2: WireGuard para acesso remoto.
- M3: embeddings locais via Ollama + pgvector para busca semântica.
- M4: backups criptografados, restauração testada, exclusão física vs.
  retenção de histórico (ponto aberto na seção 15 do documento).
