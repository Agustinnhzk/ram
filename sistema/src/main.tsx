import "@fontsource-variable/exo-2/wght-italic.css";
import "@fontsource/barlow/400.css";
import "@fontsource/barlow/500.css";
import "@fontsource/barlow/600.css";
import "./estilos.css";
import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router";
import { App } from "./App";
import { ErrorDeApi } from "./api/cliente";
import { ProveedorDeAvisos } from "./componentes/Avisos";

/**
 * Si CUALQUIER pedido responde 401 (la sesión venció mientras trabajabas), se marca "sin sesión":
 * RutaProtegida lo ve y te manda a /ingresar. Así ninguna pantalla tiene que manejarlo por su cuenta.
 */
function siVencioLaSesion(error: Error) {
  if (error instanceof ErrorDeApi && error.sinSesion) consultas.setQueryData(["sesion"], null);
}

const consultas: QueryClient = new QueryClient({
  queryCache: new QueryCache({ onError: siVencioLaSesion }),
  mutationCache: new MutationCache({ onError: siVencioLaSesion }),
  defaultOptions: {
    queries: {
      // Un 401/403/404 no se arregla reintentando; un corte de red, quizás sí (una vez).
      retry: (intentos, error) => !(error instanceof ErrorDeApi && error.estado < 500) && intentos < 1,
      refetchOnWindowFocus: true,
    },
  },
});

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <QueryClientProvider client={consultas}>
      <BrowserRouter>
        <ProveedorDeAvisos>
          <App />
        </ProveedorDeAvisos>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
