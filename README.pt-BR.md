# solaris-memory-assistent

🇧🇷 Português (este arquivo) · 🇺🇸 [English](README.md)

Assistente pessoal de memória privada: registre pequenos fatos do cotidiano
por comandos explícitos (`/add`, `/add-temp`) e recupere-os depois
perguntando em linguagem natural. Não é um editor de notas, um gestor de
tarefas, nem um assistente genérico — sua única função é lembrar o que foi
explicitamente registrado, e sempre mostrar o registro que sustenta cada
resposta. Construído a partir de um documento de requisitos próprio
(visão de produto, requisitos funcionais/não funcionais, critérios de
aceite) que não faz parte deste repositório — os comentários no código e
este README citam ele como RF-xx/RNF-xx/CT-xx e números de seção, para
rastreabilidade.

## Por que "Solaris"?

O nome vem do filme *Solaris* (1972), de Andrei Tarkovsky, no qual um
oceano senciente reconstrói fragmentos das memórias de quem o visita — não
como um arquivo fiel, mas recriando e transformando aquilo que encontra na
mente humana.

Este projeto inverte essa premissa de propósito. Onde o Solaris do filme
confunde memória e invenção, aqui a linha entre as duas precisa ficar
nítida: toda resposta remete a algo que o usuário registrou explicitamente,
e a camada de recuperação (RAG) é pensada para distinguir uma lembrança
registrada de qualquer interpretação gerada pelo modelo (ver RF-04/CT-05/
CT-11 abaixo — não existe etapa de síntese capaz de inventar um fato, só
recuperação). Mesmo assim o nome cabe bem, porque é exatamente essa tensão
— entre recordar, reconstruir e compreender — que o produto precisa
resolver corretamente toda vez que responde a uma pergunta.

*Solaris — uma memória que responde de volta.*

## Decisões tomadas nesta fase

(além do que o documento de requisitos especifica)

- **Frontend**: Compose Multiplatform (Kotlin), rodando em Android e
  Desktop a partir da mesma base de código — o documento especifica
  Android nativo, mas o uso real exige acesso também pelo desktop.
- **Escopo**: backend M1 (Núcleo local, seção 14 do documento) primeiro,
  depois uma primeira fatia de UI (navegação + tema + chat + memórias +
  ajustes) ligada a ele.
- **Criptografia de campo**: o backend vai rodar num servidor compartilhado
  com outras duas pessoas, então o campo `content` das memórias é
  criptografado com AES-256-GCM *na aplicação*, não só no disco — mesmo
  alguém com acesso direto ao Postgres (psql, dump) só vê texto cifrado.
  Isso decide a favor de um dos pontos abertos da seção 15 do documento
  ("Criptografia por campo além de disco"). Consequência técnica: busca por
  texto não pode mais acontecer em SQL (`LIKE` não funciona sobre texto
  cifrado) — o motor de busca busca os candidatos ativos pelas colunas não
  cifradas (status, datas) e filtra por texto já descriptografado na
  aplicação. Isso é adequado para o volume pessoal de memórias que o
  documento assume; não escalaria para milhões de linhas.
- **Dark mode**: suportado desde esta primeira versão da UI, com tokens de
  cor semânticos (fundo, superfície, texto, borda, destaque — ver
  `theme/Color.kt`) e dois conjuntos de valores, mesmo padrão de theming do
  tema Obsidian "retronotes" usado como referência
  (`https://github.com/sr-campelo/retronotes`: variáveis tipo
  `--background-primary`/`--text-normal` redefinidas por modo). A
  alternância fica em Ajustes (seção 10.3 do documento) e troca o app
  inteiro, testado rodando o app desktop de verdade.
- **Embeddings da busca semântica ficam sem criptografia** no pgvector,
  mesmo sendo derivados do mesmo texto que `content` criptografa —
  tradeoff aceito conscientemente, já que o pgvector precisa dos floats
  crus para calcular distância; não tem como buscar um vetor cifrado com
  ele.

## O que existe

`backend/` — API Spring Boot (Java 17) cobrindo os marcos M1 e M3 do
roteiro:

- `POST /api/v1/memories` — cria memória permanente (sem `ttlDays`) ou
  temporária (com `ttlDays`, expira em `ttlDays × 24h` exatas).
- `GET /api/v1/memories` — lista paginada, filtros `status`
  (active/expired/all), `type` (permanent/temporary/all) e `q` (busca
  textual).
- `GET /api/v1/memories/{id}`, `PATCH /api/v1/memories/{id}`,
  `DELETE /api/v1/memories/{id}`.
- `POST /api/v1/chat/query` — busca em linguagem natural, híbrida (M3):
  texto exato primeiro (RF-05 — nunca é substituído por similaridade, só
  complementado), e só quando isso não encontra nada é que entra a busca
  semântica via embeddings (pgvector + Ollama). A resposta é sempre o
  conteúdo literal de uma memória encontrada, nunca texto gerado — não há
  como "inventar" fatos porque não há síntese por LLM ainda, só
  recuperação. Quando a resposta vem só da busca semântica, isso é
  sinalizado explicitamente nos `warnings` (RF-13), nunca apresentado como
  se fosse um achado exato.
- `POST /api/v1/auth/login`, `POST /api/v1/auth/logout` — usuário único
  (JWT + watermark de revogação em `auth_state`), com limitador de
  tentativas de login.
- `GET /api/v1/health`.

Detalhes relevantes:
- Expiração de memórias é sempre filtrada na consulta (`expiresAt is null
  or expiresAt > now()`), nunca depende de um job de limpeza ter rodado
  (seção 5.1).
- `clientRequestId` dá idempotência em `POST /memories` (reenvio após
  falha de rede não duplica) e em `POST /chat/query` (cache curto em
  memória).
- Expressões temporais simples em português (`ontem`, `hoje`, `semana
  passada`) resolvem contra `eventAt`, nunca contra `createdAt` (RF-12).
- **Embeddings (M3)**: ao criar/editar o texto de uma memória, um embedding
  (Ollama, modelo `nomic-embed-text`, 768 dimensões) é gerado em background
  (`EmbeddingExecutor`) e salvo numa coluna `vector` do Postgres (extensão
  `pgvector`, índice HNSW) — nunca na mesma transação da escrita nem
  bloqueando a resposta HTTP (RNF-07), e uma falha/indisponibilidade do
  Ollama nunca impede o registro da memória (RF-10). Opcional via
  `app.embeddings.enabled` (default `true` aqui; desliga se não houver
  Ollama no ambiente).

`frontend/` — app Compose Multiplatform (Kotlin, Android e Desktop a
partir do mesmo módulo `composeApp`):

- Design system mínimo em `components/` (`PixelButton`, `RetroTextField`,
  `RetroChatBubble`, `MemoryCard`) seguindo a estética "Colorful Retro
  Macintosh Pixel UI" do documento (seção 11): contornos pretos grossos,
  sombras rígidas deslocadas (nunca blur), sem cantos muito arredondados.
  Paleta oficial da seção 11.2 em `theme/Color.kt`, com variante escura
  derivada (ver decisão de dark mode acima).
- Três telas (`screens/`): Chat (comandos `/add`, `/add-temp`, perguntas em
  linguagem natural), Memórias (lista, prazo restante, exclusão), Ajustes
  (tema, servidor, login). Navegação por abas inferior — layout pensado
  para celular, mas sem exigir os três painéis simultâneos em telas
  grandes (seção 11.3).
- `network/ApiClient.kt` fala com o backend acima via Ktor.
- `settings/AppSettings.kt` persiste tema, URL do servidor e sessão
  (token) localmente — SharedPreferences no Android, `java.util.prefs` no
  Desktop, mesma API via `multiplatform-settings`. Ao abrir o app com uma
  sessão salva, ele tenta reusá-la antes de cair na tela de login.
  Guardado em texto puro: protege contra "esqueci de persistir", não
  contra o aparelho roubado — diferente da criptografia do backend, que
  defende contra outras contas no mesmo servidor (uma ameaça mais
  relevante aqui). Testado em `desktopTest` contra o caminho real de
  produção (sem precisar automatizar clique na janela, que se mostrou
  pouco confiável neste ambiente).

`deploy/` — stack de produção para a VM alvo (`falcon.whatbox.ca`,
confirmado que tem IP público de verdade, não está atrás de CGNAT):
Postgres+pgvector, a API e WireGuard via Docker Compose, com só a porta
UDP do WireGuard exposta à internet (RNF-01). Ver `deploy/README.md` para
o passo a passo — **ainda não verificado contra a VM real**, já que esta
sessão não tem acesso a ela.

## Rodando localmente

```bash
docker compose up -d                 # Postgres (com pgvector) em localhost:5434
ollama pull nomic-embed-text          # só se for usar busca semântica (M3)
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
| `APP_EMBEDDINGS_ENABLED` | `false` desliga a busca semântica (M3) por completo |
| `APP_OLLAMA_URL` | onde está o Ollama (default `http://localhost:11434`) |

### Testes

```bash
cd backend
mvn test
```

Usa Testcontainers (requer Docker rodando) para testar contra um Postgres
real, incluindo os casos de aceite do documento (CT-01 a CT-11; CT-12 é de
UI e CT-07 depende do cliente, não testáveis aqui) e uma verificação de que
o conteúdo realmente está cifrado na coluna. O teste de busca semântica
(`SemanticSearchIntegrationTest`) fala com o Ollama real da máquina e se
pula sozinho quando ele não está acessível, em vez de falhar.

Nota: `reuseForks=false` no Surefire (uma JVM por classe de teste) — sem
isso, a última classe a rodar numa suíte completa ocasionalmente não
conseguia conectar no próprio container Postgres (connection refused logo
após ele subir saudável), mesmo cada classe tendo seu container isolado;
isolar o processo eliminou o problema.

### Frontend

```bash
cd frontend
./gradlew :composeApp:run            # app desktop
./gradlew :composeApp:assembleDebug  # APK debug (precisa do Android SDK)
```

## Próximos passos (não implementados ainda)

- Fonte bitmap/pixel real para a tipografia (hoje usa fonte do sistema em
  negrito como aproximação) e dithering decorativo (seção 11.1/11.6).
- Verificar o stack `deploy/` (WireGuard + Docker Compose) contra a VM
  real (ainda sem acesso a ela nesta sessão).
- Backups criptografados: `deploy/backup.sh` já existe, mas a política de
  retenção (apagar de vez vs. manter histórico arquivado) segue sendo um
  ponto aberto do próprio documento (seção 15) — o default atual é um
  palpite razoável, não essa decisão tomada por você.
