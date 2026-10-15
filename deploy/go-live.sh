#!/usr/bin/env bash
# One-shot deploy to the Oracle VM once you have its public/reserved IP —
# automates README.md steps 3 (copy project) and 5 (docker compose up).
# Steps 2 (install Docker on the VM) and 4 (fill APP_USER/APP_PASSWORD_HASH
# in deploy/.env via ./generate-password-hash.sh) still need to happen once
# by hand first — this script refuses to run until those are done.
#
# Usage: ./go-live.sh <IP> [ssh-user=ubuntu] [caminho-chave-ssh]
set -euo pipefail
cd "$(dirname "$0")"

IP="${1:?Uso: ./go-live.sh <IP> [usuario=ubuntu] [caminho-chave-ssh]}"
SSH_USER="${2:-ubuntu}"
SSH_KEY="${3:-}"
SSH_OPTS=()
[ -n "$SSH_KEY" ] && SSH_OPTS=(-i "$SSH_KEY")

if [ ! -f .env ]; then
  echo "deploy/.env não existe. Copie de .env.example e preencha antes de rodar isso." >&2
  exit 1
fi
if ! grep -qE "^APP_USER=.+" .env || ! grep -qE "^APP_PASSWORD_HASH=.+" .env; then
  echo "Preencha APP_USER e APP_PASSWORD_HASH em deploy/.env (./generate-password-hash.sh) antes de subir." >&2
  exit 1
fi

sed -i "s|^WIREGUARD_SERVER_URL=.*|WIREGUARD_SERVER_URL=${IP}|" .env

cd ..
echo "Copiando backend/ e deploy/ para ${SSH_USER}@${IP}..."
ssh "${SSH_OPTS[@]}" "${SSH_USER}@${IP}" "mkdir -p ~/solaris-memory-assistent"
scp "${SSH_OPTS[@]}" -r backend deploy "${SSH_USER}@${IP}:~/solaris-memory-assistent/"

echo "Subindo o stack na VM..."
ssh "${SSH_OPTS[@]}" "${SSH_USER}@${IP}" "cd ~/solaris-memory-assistent/deploy && docker compose up -d --build && docker compose ps"

echo
echo "Pronto. Para pegar a config do WireGuard (QR code):"
echo "  ssh ${SSH_OPTS[*]-} ${SSH_USER}@${IP} 'cd ~/solaris-memory-assistent/deploy && docker compose exec wireguard /app/show-peer 1'"
