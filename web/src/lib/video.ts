/**
 * Los videos de los trabajos llegan del backend siempre limpios (ver backend .../dominio/Video.java):
 *   https://www.instagram.com/reel/ID/   (o /p/ID/, /tv/ID/)
 *   https://www.youtube.com/watch?v=ID
 * Esto los traduce a lo que hace falta para mostrarlos incrustados.
 */
export type VideoIncrustable = {
  plataforma: "Instagram" | "YouTube";
  /** La dirección del reproductor que va en el iframe. */
  reproductor: string;
  /** El link para abrirlo en la app o en otra pestaña. */
  original: string;
  /** Instagram es vertical (reel), YouTube horizontal. */
  vertical: boolean;
};

export function videoIncrustable(link: string | null): VideoIncrustable | null {
  if (!link) return null;
  const instagram = link.match(/^https:\/\/www\.instagram\.com\/(reel|p|tv)\/([A-Za-z0-9_-]+)\/$/);
  if (instagram) {
    return {
      plataforma: "Instagram",
      reproductor: `https://www.instagram.com/${instagram[1]}/${instagram[2]}/embed`,
      original: link,
      vertical: true,
    };
  }
  const youtube = link.match(/^https:\/\/www\.youtube\.com\/watch\?v=([A-Za-z0-9_-]{11})$/);
  if (youtube) {
    return {
      plataforma: "YouTube",
      // youtube-nocookie: YouTube no guarda cookies de quien mira hasta que le da play.
      reproductor: `https://www.youtube-nocookie.com/embed/${youtube[1]}?autoplay=1&rel=0`,
      original: link,
      vertical: false,
    };
  }
  return null;
}
