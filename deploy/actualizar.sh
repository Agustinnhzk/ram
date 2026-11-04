#!/bin/sh
# Baja lo último de GitHub y vuelve a levantar todo con los cambios.
# Uso (en el servidor):  ./deploy/actualizar.sh
set -e
cd "$(dirname "$0")/.."
git pull
cd deploy
docker compose up -d --build
# Borra las imágenes viejas que quedaron sin usar, para que no se llene el disco.
docker image prune -f
echo "Listo. La tienda tarda 1 o 2 minutos más en compilarse: docker compose logs -f web"
