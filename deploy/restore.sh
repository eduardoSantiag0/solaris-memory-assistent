#!/usr/bin/env bash
# Decrypts and restores a backup produced by backup.sh into the running
# `postgres` service. Destructive by nature (drops and recreates the
# database) — confirms before running unless -y is passed.
#
# Usage: ./restore.sh path/to/backup.sql.gz.enc [-y]
set -euo pipefail
cd "$(dirname "$0")"

BACKUP_FILE="${1:?Uso: ./restore.sh caminho/para/backup.sql.gz.enc [-y]}"
ASSUME_YES="${2:-}"

if [ -f .env ]; then
  set -a; source .env; set +a
fi
: "${BACKUP_PASSPHRASE:?Defina BACKUP_PASSPHRASE no .env}"

if [ "$ASSUME_YES" != "-y" ]; then
  read -r -p "Isso vai APAGAR o banco solaris_memory atual e substituir pelo conteúdo de $BACKUP_FILE. Continuar? [y/N] " confirm
  [ "$confirm" = "y" ] || [ "$confirm" = "Y" ] || { echo "Cancelado."; exit 1; }
fi

TMP_SQL="$(mktemp)"
trap 'rm -f "$TMP_SQL"' EXIT

openssl enc -d -aes-256-cbc -pbkdf2 -salt -pass env:BACKUP_PASSPHRASE -in "$BACKUP_FILE" \
  | gunzip > "$TMP_SQL"

docker compose exec -T postgres psql -U solaris -d postgres \
  -c "DROP DATABASE IF EXISTS solaris_memory;" \
  -c "CREATE DATABASE solaris_memory;"

docker compose exec -T postgres psql -U solaris -d solaris_memory < "$TMP_SQL"

echo "Restaurado a partir de $BACKUP_FILE"
