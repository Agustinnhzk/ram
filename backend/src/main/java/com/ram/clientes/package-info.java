/**
 * Módulo de clientes del taller.
 *
 * <p>API pública (lo que pueden usar otros módulos, por ejemplo reparaciones al registrar un ingreso):
 * {@link com.ram.clientes.Clientes}, {@link com.ram.clientes.ClienteId},
 * {@link com.ram.clientes.NuevoCliente}, {@link com.ram.clientes.DatosCliente}, {@link com.ram.clientes.FichaDeCliente},
 * {@link com.ram.clientes.DatosDeFicha}, {@link com.ram.clientes.ClienteNoEncontradoException}
 * y {@link com.ram.clientes.CuentaYaVinculadaException}.
 *
 * <p>Una ficha puede estar vinculada a una cuenta de la tienda (ADR-014): se vincula sola con el primer pedido,
 * o a mano desde el sistema.
 *
 * <p>Cada cliente pertenece a un taller. El filtro por taller lo aplica la persistencia
 * automáticamente (ADR-005): ni este módulo ni quien lo use tiene que pasar el taller.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Clientes")
package com.ram.clientes;
