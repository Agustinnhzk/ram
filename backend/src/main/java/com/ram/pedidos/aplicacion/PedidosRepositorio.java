package com.ram.pedidos.aplicacion;

import com.ram.pedidos.DatosDePago;
import com.ram.pedidos.EstadoPedido;
import com.ram.pedidos.PedidoId;
import com.ram.pedidos.ResumenDePedido;
import com.ram.pedidos.dominio.Pedido;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Puerto de salida: dónde viven los pedidos. Todo sobre el taller actual. */
public interface PedidosRepositorio {

    void guardar(Pedido pedido);

    /** Con sus líneas, sus comprobantes y los archivos. */
    void borrar(PedidoId id);

    /** El archivo de un comprobante recién subido (va aparte de los datos, ver V8__pedidos.sql). */
    void guardarArchivo(UUID comprobante, byte[] contenido);

    Optional<Pedido> porId(PedidoId id);

    Optional<byte[]> archivo(UUID comprobante);

    /** El número para el próximo pedido del taller: el último + 1 (o 1 si es el primero). */
    int siguienteNumero();

    /** Pedidos de esa cuenta que esperan el pago (para frenar a quien hace pedidos sin fin). */
    int cantidadEsperandoPago(UUID cuenta);

    /** Del más nuevo al más viejo. */
    List<ResumenDePedido> resumenes(Set<EstadoPedido> estados, int limite);

    List<ResumenDePedido> resumenesDeCuenta(UUID cuenta, int limite);

    Optional<DatosDePago> datosDePago();

    /** El precio del armado de PC del taller (ajustes de la tienda). Vacío si no lo ofrece. */
    Optional<Long> precioDelArmado();

    void guardarPrecioDelArmado(Long precio);
}
