package com.ram.usuarios;

import java.util.Optional;
import java.util.UUID;

/**
 * Cuentas de la tienda para otros módulos (por ejemplo, clientes la usa para vincular la ficha del
 * taller con la cuenta con la que compra, ADR-014). Siempre del taller actual.
 */
public interface CuentasDeTienda {

    Optional<CuentaDeTienda> porId(UUID id);

    /** Vacío si el texto no es un correo válido o no hay una cuenta con ese correo. */
    Optional<CuentaDeTienda> porCorreo(String correo);

    /**
     * Borra la cuenta y cierra sus sesiones (para limpiar las pruebas, ADR-016). Lo llama clientes al borrar una ficha.
     *
     * @throws org.springframework.dao.DataIntegrityViolationException si la cuenta todavía tiene pedidos
     */
    void borrar(UUID id);
}
