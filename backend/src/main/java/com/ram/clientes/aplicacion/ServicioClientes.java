package com.ram.clientes.aplicacion;

import com.ram.clientes.ClienteId;
import com.ram.clientes.ClienteNoEncontradoException;
import com.ram.clientes.Clientes;
import com.ram.clientes.CuentaYaVinculadaException;
import com.ram.clientes.DatosCliente;
import com.ram.clientes.DatosDeFicha;
import com.ram.clientes.FichaDeCliente;
import com.ram.clientes.NuevoCliente;
import com.ram.clientes.SinPermisoException;
import com.ram.clientes.dominio.Cliente;
import com.ram.clientes.dominio.Correo;
import com.ram.clientes.dominio.NombreCompleto;
import com.ram.clientes.dominio.Telefono;
import com.ram.usuarios.CuentaDeTienda;
import com.ram.usuarios.CuentasDeTienda;
import com.ram.usuarios.Rol;
import com.ram.usuarios.SesionActual;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Casos de uso de clientes. Implementa la API pública del módulo.
 *
 * <p>Convierte lo que llega de afuera (Strings del formulario) en objetos del dominio,
 * que se validan solos, y devuelve {@link DatosCliente}: nunca expone el objeto de dominio.
 */
public class ServicioClientes implements Clientes {

    /** Suficiente para un autocompletado; si hay más, el usuario sigue escribiendo. */
    static final int MAXIMO_RESULTADOS = 20;

    /** Para la pantalla Clientes: una página larga. */
    static final int MAXIMO_LISTADO = 50;

    /** Con menos de 2 caracteres la búsqueda devolvería medio padrón de clientes. */
    static final int MINIMO_CARACTERES_BUSQUEDA = 2;

    /** Con menos de 3 números, buscar por teléfono no sirve. */
    static final int MINIMO_DIGITOS_TELEFONO = 3;

    private final ClientesRepositorio repositorio;
    private final CuentasDeTienda cuentas;
    private final SesionActual sesion;

    public ServicioClientes(ClientesRepositorio repositorio, CuentasDeTienda cuentas, SesionActual sesion) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.cuentas = Objects.requireNonNull(cuentas);
        this.sesion = Objects.requireNonNull(sesion);
    }

    @Override
    public ClienteId registrar(NuevoCliente nuevo) {
        Cliente cliente = Cliente.registrar(
                new NombreCompleto(nuevo.nombreCompleto()),
                new Telefono(nuevo.telefono()),
                Correo.opcional(nuevo.correo()));
        repositorio.guardar(cliente);
        return cliente.id();
    }

    @Override
    public List<DatosCliente> buscarPorNombre(String texto) {
        String normalizado = NombreCompleto.normalizar(texto);
        if (normalizado.length() < MINIMO_CARACTERES_BUSQUEDA) {
            return List.of();
        }
        return repositorio.buscarPorNombre(normalizado, MAXIMO_RESULTADOS).stream()
                .map(ServicioClientes::aDatos)
                .toList();
    }

    @Override
    public Optional<DatosCliente> obtener(ClienteId id) {
        return repositorio.buscarPorId(id).map(ServicioClientes::aDatos);
    }

    @Override
    public List<DatosCliente> obtenerVarios(Collection<ClienteId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return repositorio.buscarVarios(ids).stream().map(ServicioClientes::aDatos).toList();
    }

    @Override
    public void actualizarContacto(ClienteId id, String telefono, String correo) {
        Cliente cliente = existente(id);
        cliente.actualizarContacto(new Telefono(telefono), Correo.opcional(correo));
        repositorio.guardar(cliente);
    }

    // ─── Ficha completa ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<FichaDeCliente> listar(String texto) {
        String limpio = texto == null ? "" : texto.strip();
        List<Cliente> encontrados;
        if (limpio.isEmpty()) {
            encontrados = repositorio.ultimos(MAXIMO_LISTADO);
        } else if (limpio.matches("[0-9 ()+\\-.]+")) {
            String digitos = limpio.replaceAll("\\D", "");
            encontrados = digitos.length() < MINIMO_DIGITOS_TELEFONO ? List.of() : repositorio.buscarPorTelefono(digitos, MAXIMO_LISTADO);
        } else {
            String normalizado = NombreCompleto.normalizar(limpio);
            encontrados = normalizado.length() < MINIMO_CARACTERES_BUSQUEDA ? List.of()
                    : repositorio.buscarPorNombre(normalizado, MAXIMO_LISTADO);
        }
        // En el listado alcanza con saber SI tiene cuenta: no se consulta cada una.
        return encontrados.stream()
                .map(c -> aFicha(c, c.cuentaTienda().map(id -> new CuentaDeTienda(id, null, null)).orElse(null)))
                .toList();
    }

    @Override
    public Optional<FichaDeCliente> ficha(ClienteId id) {
        return repositorio.buscarPorId(id)
                .map(c -> aFicha(c, c.cuentaTienda().flatMap(cuentas::porId).orElse(null)));
    }

    @Override
    public void editar(ClienteId id, DatosDeFicha datos) {
        Objects.requireNonNull(datos);
        Cliente cliente = existente(id);
        cliente.corregirNombre(new NombreCompleto(datos.nombreCompleto()));
        cliente.actualizarContacto(new Telefono(datos.telefono()), Correo.opcional(datos.correo()));
        cliente.cambiarNotas(datos.notas());
        repositorio.guardar(cliente);
    }

    @Override
    public void vincularCuenta(ClienteId id, String correoDeLaCuenta) {
        Cliente cliente = existente(id);
        CuentaDeTienda cuenta = cuentas.porCorreo(correoDeLaCuenta).orElseThrow(() ->
                new IllegalArgumentException("No hay una cuenta de la tienda con ese correo"));
        Optional<Cliente> otra = repositorio.porCuentaDeTienda(cuenta.id());
        if (otra.isPresent()) {
            if (otra.get().equals(cliente)) {
                return;
            }
            throw new CuentaYaVinculadaException(otra.get().nombre().valor());
        }
        cliente.vincularCuenta(cuenta.id(), Correo.opcional(cuenta.correo()));
        repositorio.guardar(cliente);
    }

    @Override
    public void desvincularCuenta(ClienteId id) {
        Cliente cliente = existente(id);
        cliente.desvincularCuenta();
        repositorio.guardar(cliente);
    }

    @Override
    public Optional<ClienteId> deLaCuenta(UUID cuenta) {
        return cuenta == null ? Optional.empty() : repositorio.porCuentaDeTienda(cuenta).map(Cliente::id);
    }

    @Override
    public Optional<ClienteId> vincularAlComprar(UUID cuenta, String telefono) {
        Optional<Cliente> yaVinculada = repositorio.porCuentaDeTienda(cuenta);
        if (yaVinculada.isPresent()) {
            return yaVinculada.map(Cliente::id);
        }
        Optional<CuentaDeTienda> datosDeLaCuenta = cuentas.porId(cuenta);
        if (datosDeLaCuenta.isEmpty()) {
            return Optional.empty();
        }
        CuentaDeTienda datos = datosDeLaCuenta.get();
        Optional<Correo> correo = correoValido(datos.correo());
        Optional<Telefono> tel = telefonoValido(telefono);

        // Una sola coincidencia, si no, no se adivina: mejor una ficha nueva que pegarle la cuenta a otra persona.
        Optional<Cliente> encontrada = correo.flatMap(c -> unica(repositorio.sinCuentaConCorreo(c.valor())))
                .or(() -> tel.flatMap(t -> unica(repositorio.sinCuentaConTelefono(t.valor()))));
        Cliente cliente;
        if (encontrada.isPresent()) {
            cliente = encontrada.get();
        } else {
            if (tel.isEmpty()) {
                return Optional.empty();
            }
            NombreCompleto nombre;
            try {
                nombre = new NombreCompleto(datos.nombre());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
            cliente = Cliente.registrar(nombre, tel.get(), correo);
        }
        cliente.vincularCuenta(cuenta, correo);
        repositorio.guardar(cliente);
        return Optional.of(cliente.id());
    }

    @Override
    public void borrar(ClienteId id) {
        if (!sesion.requerido().tieneAlgunRol(Rol.DUENO)) {
            throw new SinPermisoException("Solo el dueño puede borrar clientes");
        }
        Cliente cliente = existente(id);
        repositorio.borrar(id);
        // La cuenta va después de la ficha: la ficha la referencia. Si la cuenta tiene pedidos, la base frena todo.
        cliente.cuentaTienda().ifPresent(cuentas::borrar);
    }

    // ─── Ayudas ───────────────────────────────────────────────────────────────────────────────

    private Cliente existente(ClienteId id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    private static Optional<Cliente> unica(List<Cliente> encontradas) {
        return encontradas.size() == 1 ? Optional.of(encontradas.getFirst()) : Optional.empty();
    }

    private static Optional<Correo> correoValido(String valor) {
        try {
            return Correo.opcional(valor);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Optional<Telefono> telefonoValido(String valor) {
        try {
            return valor == null || valor.isBlank() ? Optional.empty() : Optional.of(new Telefono(valor));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static DatosCliente aDatos(Cliente c) {
        return new DatosCliente(
                c.id(),
                c.nombre().valor(),
                c.telefono().valor(),
                c.correo().map(Correo::valor).orElse(null));
    }

    private static FichaDeCliente aFicha(Cliente c, CuentaDeTienda cuenta) {
        return new FichaDeCliente(c.id(), c.nombre().valor(), c.telefono().valor(), c.correo().map(Correo::valor).orElse(null),
                c.notas().orElse(null), cuenta, c.creadoEn());
    }
}
