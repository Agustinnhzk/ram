package com.ram.catalogo.adaptadores.persistencia;

import com.ram.catalogo.ProductoId;
import com.ram.catalogo.aplicacion.HistorialDePrecios;
import com.ram.catalogo.dominio.CambioDePrecio;
import com.ram.catalogo.dominio.Pesos;
import com.ram.usuarios.UsuarioId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class HistorialDePreciosJpa implements HistorialDePrecios {

    private final CambioDePrecioJpaRepository jpa;

    HistorialDePreciosJpa(CambioDePrecioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(CambioDePrecio c) {
        jpa.save(new CambioDePrecioJpaEntity(
                c.producto().valor(),
                c.anterior() == null ? null : c.anterior().valor(),
                c.nuevo() == null ? null : c.nuevo().valor(),
                c.motivo(),
                c.porcentaje(),
                c.usuario().valor(),
                c.nombreUsuario(),
                c.cuando()));
    }

    @Override
    public List<CambioDePrecio> deProducto(ProductoId producto, int limite) {
        return jpa.deProducto(producto.valor(), PageRequest.of(0, limite)).stream()
                .map(e -> new CambioDePrecio(
                        ProductoId.de(e.getProductoId()),
                        e.getPrecioAnterior() == null ? null : new Pesos(e.getPrecioAnterior()),
                        e.getPrecioNuevo() == null ? null : new Pesos(e.getPrecioNuevo()),
                        e.getMotivo(),
                        e.getPorcentaje(),
                        UsuarioId.de(e.getUsuarioId()),
                        e.getUsuarioNombre(),
                        e.getCambiadoEn()))
                .toList();
    }
}
