/**
 * Módulo de clientes del taller.
 *
 * <p>API pública (lo que pueden usar otros módulos, por ejemplo reparaciones al registrar un ingreso):
 * {@link com.ram.clientes.Clientes}, {@link com.ram.clientes.ClienteId},
 * {@link com.ram.clientes.NuevoCliente}, {@link com.ram.clientes.DatosCliente}
 * y {@link com.ram.clientes.ClienteNoEncontradoException}.
 *
 * <p>Cada cliente pertenece a un taller. El filtro por taller lo aplica la persistencia
 * automáticamente (ADR-005): ni este módulo ni quien lo use tiene que pasar el taller.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Clientes")
package com.ram.clientes;
