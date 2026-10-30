import { Route, Routes } from "react-router";
import { Marco } from "./componentes/Marco";
import { RutaProtegida } from "./componentes/RutaProtegida";
import { Ficha as FichaDeCliente } from "./paginas/clientes/Ficha";
import { Listado as ListadoDeClientes } from "./paginas/clientes/Listado";
import { Ingresar } from "./paginas/Ingresar";
import { Formulario as FormularioDeInstalacion } from "./paginas/instalaciones/Formulario";
import { Listado as ListadoDeInstalaciones } from "./paginas/instalaciones/Listado";
import { Monitoreo } from "./paginas/Monitoreo";
import { Inicio } from "./paginas/Inicio";
import { NoEncontrada } from "./paginas/NoEncontrada";
import { Detalle as DetalleDePedido } from "./paginas/pedidos/Detalle";
import { Listado as ListadoDePedidos } from "./paginas/pedidos/Listado";
import { Precios } from "./paginas/Precios";
import { Formulario as FormularioDeProducto } from "./paginas/productos/Formulario";
import { Listado as ListadoDeProductos } from "./paginas/productos/Listado";
import { Promociones } from "./paginas/Promociones";
import { Comprobante } from "./paginas/reparaciones/Comprobante";
import { Detalle } from "./paginas/reparaciones/Detalle";
import { Listado } from "./paginas/reparaciones/Listado";
import { NuevoIngreso } from "./paginas/reparaciones/NuevoIngreso";
import { Stock } from "./paginas/stock/Stock";

/**
 * Las pantallas del sistema. Todo lo que está adentro de <RutaProtegida> necesita login;
 * <Marco> pone el menú alrededor.
 */
export function App() {
  return (
    <Routes>
      <Route path="/ingresar" element={<Ingresar />} />
      <Route element={<RutaProtegida />}>
        {/* El comprobante va sin el menú: es una hoja para imprimir. */}
        <Route path="reparaciones/:id/comprobante" element={<Comprobante />} />
        <Route element={<Marco />}>
          <Route index element={<Inicio />} />
          <Route path="precios" element={<Precios />} />
          <Route path="productos" element={<ListadoDeProductos />} />
          <Route path="productos/nuevo" element={<FormularioDeProducto />} />
          <Route path="productos/:id" element={<FormularioDeProducto />} />
          <Route path="promociones" element={<Promociones />} />
          <Route path="pedidos" element={<ListadoDePedidos />} />
          <Route path="pedidos/:id" element={<DetalleDePedido />} />
          <Route path="reparaciones" element={<Listado />} />
          <Route path="reparaciones/nueva" element={<NuevoIngreso />} />
          <Route path="reparaciones/:id" element={<Detalle />} />
          <Route path="clientes" element={<ListadoDeClientes />} />
          <Route path="clientes/:id" element={<FichaDeCliente />} />
          <Route path="stock" element={<Stock />} />
          <Route path="instalaciones" element={<ListadoDeInstalaciones />} />
          <Route path="instalaciones/nueva" element={<FormularioDeInstalacion />} />
          <Route path="instalaciones/:id" element={<FormularioDeInstalacion />} />
          <Route path="monitoreo" element={<Monitoreo />} />
          <Route path="*" element={<NoEncontrada />} />
        </Route>
      </Route>
    </Routes>
  );
}
