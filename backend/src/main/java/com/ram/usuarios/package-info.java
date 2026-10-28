/**
 * Módulo de usuarios: quién puede entrar y con qué permisos (ADR-006).
 *
 * <p>Hay dos tipos de cuenta que nunca se mezclan:
 * <ul>
 *   <li><b>Personal</b> del taller (Dueño, Técnico, Mostrador): entra al sistema de gestión.</li>
 *   <li><b>Cliente</b> de la tienda: compra y ve sus reparaciones.</li>
 * </ul>
 *
 * <p>API pública para otros módulos: {@link com.ram.usuarios.SesionActual} (quién está logueado),
 * {@link com.ram.usuarios.UsuarioAutenticado}, {@link com.ram.usuarios.UsuarioId},
 * {@link com.ram.usuarios.Rol} y {@link com.ram.usuarios.TipoDeCuenta}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Usuarios")
package com.ram.usuarios;
