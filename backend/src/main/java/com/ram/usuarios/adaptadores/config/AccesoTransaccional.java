package com.ram.usuarios.adaptadores.config;

import com.ram.usuarios.Rol;
import com.ram.usuarios.UsuarioId;
import com.ram.usuarios.aplicacion.Acceso;
import com.ram.usuarios.aplicacion.CifradorDeClaves;
import com.ram.usuarios.aplicacion.CuentasClientesRepositorio;
import com.ram.usuarios.aplicacion.PersonalRepositorio;
import com.ram.usuarios.aplicacion.ServicioDeAcceso;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Mismo patrón que ClientesTransaccionales, pero como subclase: ServicioDeAcceso es una clase
 * (no hay otras implementaciones), así que se extiende y cada método corre en su transacción.
 */
class AccesoTransaccional extends ServicioDeAcceso {

    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    AccesoTransaccional(PersonalRepositorio personal, CuentasClientesRepositorio cuentas,
                        CifradorDeClaves cifrador, PlatformTransactionManager transacciones) {
        super(personal, cuentas, cifrador);
        this.escritura = new TransactionTemplate(transacciones);
        this.lectura = new TransactionTemplate(transacciones);
        this.lectura.setReadOnly(true);
    }

    @Override
    public Acceso iniciarSesionPersonal(String correo, String clave) {
        return lectura.execute(estado -> super.iniciarSesionPersonal(correo, clave));
    }

    @Override
    public Acceso iniciarSesionCliente(String correo, String clave) {
        return lectura.execute(estado -> super.iniciarSesionCliente(correo, clave));
    }

    @Override
    public Acceso registrarCliente(String nombre, String correo, String clave) {
        return escritura.execute(estado -> super.registrarCliente(nombre, correo, clave));
    }

    @Override
    public UsuarioId darDeAltaPersonal(String nombre, String correo, String clave, Rol rol) {
        return escritura.execute(estado -> super.darDeAltaPersonal(nombre, correo, clave, rol));
    }

    @Override
    public boolean hayPersonal() {
        return Boolean.TRUE.equals(lectura.execute(estado -> super.hayPersonal()));
    }
}
