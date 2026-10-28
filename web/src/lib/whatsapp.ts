/**
 * Arma un link que abre WhatsApp con un mensaje ya escrito.
 * Funciona en el celular (abre la app) y en la compu (abre WhatsApp Web).
 */
export function linkWhatsApp(numero: string, mensaje: string): string {
  return `https://wa.me/${numero}?text=${encodeURIComponent(mensaje)}`;
}
