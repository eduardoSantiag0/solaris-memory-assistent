#!/usr/bin/env bash
# Generates a bcrypt hash for APP_PASSWORD_HASH without needing bcrypt or
# htpasswd installed on the host — a fresh cloud VM usually has neither.
# The password goes in over stdin, not a command argument or a file on
# disk, so it never shows up in `ps aux` output or leaves a temp file behind.
#
# Usage: ./generate-password-hash.sh
set -euo pipefail

CONTAINER_RUNTIME="${CONTAINER_RUNTIME:-docker}"

read -r -s -p "Senha (não aparece na tela nem em 'ps'): " PASSWORD
echo
if [ -z "$PASSWORD" ]; then
  echo "Senha vazia, cancelado." >&2
  exit 1
fi

HASH="$(printf '%s' "$PASSWORD" | "$CONTAINER_RUNTIME" run --rm -i python:3.12-slim \
  sh -c 'pip install --quiet --disable-pip-version-check bcrypt 1>&2 && python3 -c "
import bcrypt, sys
password = sys.stdin.buffer.read()
print(bcrypt.hashpw(password, bcrypt.gensalt()).decode())
"')"
unset PASSWORD

echo
echo "APP_PASSWORD_HASH (\$ já dobrado em \$\$ — cole direto no .env):"
echo "${HASH//\$/\$\$}"
