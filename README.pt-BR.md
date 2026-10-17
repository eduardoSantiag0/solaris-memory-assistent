<p align="center">
  <img src="design/solaris-handoff_v2/solaris-handoff_2/assets/banners/solaris-banner-readme.gif" alt="solaris" width="600" />
</p>

# 🪐 solaris-memory-assistent

🇧🇷 Português (este arquivo) · 🇺🇸 [English](README.md)

Assistente pessoal de memória privada: você registra pequenos fatos do
cotidiano por alguns comandos simples (`/add`, `/add-temp`) e pergunta por
eles depois, em linguagem natural. Não é um editor de notas, um gestor de
tarefas, nem um assistente genérico — ele só lembra o que você registrou de
verdade, e cada resposta sempre mostra o registro exato que a sustenta,
nunca um palpite.

## 🖼️ Capturas de tela

<p align="center">
  <img src="design/solaris-handoff_v2/solaris-handoff_2/referencia-png/02-chat.png" width="23%" alt="Tela de chat, tema claro" />
  <img src="design/solaris-handoff_v2/solaris-handoff_2/referencia-png/13-chat-escuro.png" width="23%" alt="Tela de chat, tema escuro" />
  <img src="design/solaris-handoff_v2/solaris-handoff_2/referencia-png/05-memorias.png" width="23%" alt="Lista de memórias" />
  <img src="design/solaris-handoff_v2/solaris-handoff_2/referencia-png/09-aparencia.png" width="23%" alt="Ajustes de aparência com as 5 paletas" />
</p>

## 🌊 Por que "Solaris"?

O nome vem do filme *Solaris* (Tarkovsky, 1972), em que um oceano senciente
reconstrói fragmentos de memória — não fielmente, mas recriando o que
encontra. Este projeto inverte essa ideia de propósito: toda resposta aqui
remonta a algo que você realmente registrou, nunca a algo inventado.

## 🧩 O que tem aqui

- **`backend/`** — API em Spring Boot: guarda memórias (permanentes ou
  temporárias), responde perguntas buscando primeiro por texto exato, e só
  cai pra busca semântica (via um modelo de embeddings local, Ollama)
  quando nada bate exato. Login de usuário único, conteúdo criptografado
  em repouso. Cada memória pode ter um anexo (imagem ou PDF) com descrição,
  e pode ser agrupada em **eventos** — uma coleção manual e com título de
  memórias já existentes; o evento em si não tem horário, ele é só a
  ordenação das suas memórias pelo momento em que cada uma aconteceu.
- **`frontend/`** — app em Compose Multiplatform (mesmo código roda em
  Android e Desktop): chat (com autocomplete de comandos `/add`,
  `/add-temp`, `/add-sensitive`, `/event`), lista de memórias, eventos, e
  configurações (tema, servidor, login).
- **`deploy/`** — stack em Docker Compose pra colocar tudo numa VM na
  nuvem, acessível só por um túnel WireGuard. Veja
  [`deploy/README.md`](deploy/README.md) pro passo a passo.

## 🚀 Rodando localmente

```bash
docker compose up -d                 # Postgres (pgvector) em localhost:5434
cd backend
mvn spring-boot:run
```

```bash
cd frontend
./gradlew :composeApp:run            # app desktop
./gradlew :composeApp:assembleDebug  # APK de debug
```

Testes do backend: `cd backend && mvn test`.

## 🛠️ Ainda não feito

- Fonte pixel-art de verdade (hoje é uma aproximação com IBM Plex Sans/Mono).
- Backup criptografado automático/agendado (`deploy/backup.sh` existe, mas
  ainda não está agendado).
