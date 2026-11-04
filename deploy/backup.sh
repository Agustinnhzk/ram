#!/bin/sh
# Copia de seguridad de la base: un archivo comprimido por día en deploy/backups/, se guardan los últimos 14.
# Se programa con cron (ver DEPLOY.md). A mano:  ./deploy/backup.sh
set -e
cd "$(dirname "$0")"
mkdir -p backups
archivo="backups/ram-$(date +%Y-%m-%d-%H%M).sql.gz"
docker compose exec -T db pg_dump -U ram ram | gzip > "$archivo"
find backups -name 'ram-*.sql.gz' -mtime +14 -delete
echo "Backup: $archivo"
