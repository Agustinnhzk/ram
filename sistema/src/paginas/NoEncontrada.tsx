import { Link } from "react-router";

export function NoEncontrada() {
  return (
    <div className="py-16 text-center">
      <p className="font-display text-6xl font-extrabold italic text-oro">404</p>
      <h1 className="mt-3 text-3xl">Esta pantalla no existe</h1>
      <Link to="/" className="mt-6 inline-block text-oro underline underline-offset-4">
        Ir al inicio
      </Link>
    </div>
  );
}
