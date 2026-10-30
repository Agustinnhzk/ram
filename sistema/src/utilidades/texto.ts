/** "Cámaras" → "camaras": para buscar sin importar tildes ni mayúsculas (igual que la tienda y el backend). */
export function normalizar(texto: string): string {
  return texto.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase().trim();
}
