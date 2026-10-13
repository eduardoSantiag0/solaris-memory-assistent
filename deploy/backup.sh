#!/usr/bin/env bash
# Encrypted backup of the Postgres database (RNF-03/RNF-11).
#
# Goes through `docker compose exec`, not a direct TCP connection — in
# production Postgres' port is never published to the host (see
# docker-compose.yml), so this is the only path in from outside the stack.
#
# Usage: ./backup.sh [output-dir]   (default: ./backups)
set -euo pipefail
cd "$(dirname "$0")"

OUTPUT_DIR="${1:-./backups}"
RETENTION_COUNT="${BACKUP_RETENTION_COUNT:-14}"

if [ -f .env ]; then
  set -a; source .env; set +a
fi
: "${BACKUP_PASSPHRASE:?Defina BACKUP_PASSPHRASE no .env (openssl rand -base64 32) antes de rodar backups}"

mkdir -p "$OUTPUT_DIR"
TIMESTAMP="$(date -u +%Y%m%d-%H%M%S)"
OUT_FILE="$OUTPUT_DIR/solaris-memory-$TIMESTAMP.sql.gz.enc"

docker compose exec -T postgres pg_dump -U solaris solaris_memory \
  | gzip \
  | openssl enc -aes-256-cbc -pbkdf2 -salt -pass env:BACKUP_PASSPHRASE \
  > "$OUT_FILE"

echo "Backup salvo em $OUT_FILE"

# Retention: keep only the N most recent (doc section 15 leaves the real
# policy — apagar de vez vs. manter histórico — as an open decision; this is
# a sane default, not that decision made for you).
ls -1t "$OUTPUT_DIR"/solaris-memory-*.sql.gz.enc 2>/dev/null \
  | tail -n "+$((RETENTION_COUNT + 1))" \
  | xargs -r rm -v
