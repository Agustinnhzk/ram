package com.ram.contactos.aplicacion;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.contactos.ConsultaDeEmpresa;
import com.ram.contactos.MotivoDeEmpresa;
import com.ram.contactos.Postulacion;
import com.ram.notificaciones.Correos;
import com.ram.notificaciones.MensajeAUnaPersona;
import com.ram.notificaciones.MensajeAlTaller;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioContactosTest {

    private final List<MensajeAlTaller> enviados = new ArrayList<>();
    private final ServicioContactos servicio = new ServicioContactos(new Correos() {
        @Override
        public void enviarAlTaller(MensajeAlTaller mensaje) {
            enviados.add(mensaje);
        }

        @Override
        public void enviarA(MensajeAUnaPersona mensaje) {
            throw new AssertionError("Contactos no le escribe a personas");
        }
    });
    private final ArchivoSubido cv = new ArchivoSubido("CV Ana.pdf", TipoDeArchivo.PDF, "%PDF-1.7".getBytes());

    @Test
    void unaPostulacionLlegaConElCvAdjuntoYSePuedeResponderAlPostulante() {
        servicio.postularse(new Postulacion("  Ana López ", "ana@mail.com", "341 555-1234", "Reparación de notebooks",
                "Tengo 2 años de experiencia", cv));

        MensajeAlTaller mail = enviados.getFirst();
        assertThat(mail.asunto()).isEqualTo("Postulación: Ana López — Reparación de notebooks");
        assertThat(mail.responderA()).isEqualTo("ana@mail.com");
        assertThat(mail.texto()).contains("Ana López", "341 555-1234", "Tengo 2 años de experiencia");
        assertThat(mail.adjuntos()).singleElement().satisfies(a -> {
            assertThat(a.nombre()).isEqualTo("CV Ana.pdf");
            assertThat(a.tipoDeContenido()).isEqualTo("application/pdf");
        });
    }

    @Test
    void elAreaYElMensajeSonOpcionales_elCvNo() {
        servicio.postularse(new Postulacion("Ana", "ana@mail.com", "3415551234", null, " ", cv));
        assertThat(enviados.getFirst().texto()).contains("(no indicó)", "(sin mensaje)");

        assertThatThrownBy(() -> servicio.postularse(new Postulacion("Ana", "ana@mail.com", "3415551234", null, null, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("CV");
    }

    @Test
    void validaCorreoYTelefono_yNoMandaNada() {
        assertThatThrownBy(() -> servicio.postularse(new Postulacion("Ana", "ana@", "3415551234", null, null, cv)))
                .hasMessageContaining("correo");
        assertThatThrownBy(() -> servicio.postularse(new Postulacion("Ana", "ana@mail.com", "123", null, null, cv)))
                .hasMessageContaining("teléfono");
        assertThatThrownBy(() -> servicio.postularse(new Postulacion("Ana", "ana@mail.com", "llamame al 341", null, null, cv)))
                .hasMessageContaining("teléfono");
        assertThat(enviados).isEmpty();
    }

    @Test
    void unaEmpresaQueQuiereTercerizarSuIt() {
        servicio.consultarComoEmpresa(new ConsultaDeEmpresa("Estudio Pérez", "Juan Pérez", "juan@estudio.com",
                "+54 9 341 555-0000", MotivoDeEmpresa.TERCERIZAR_IT, "Somos 12 PCs y un servidor"));

        MensajeAlTaller mail = enviados.getFirst();
        assertThat(mail.asunto()).isEqualTo("Empresa: Estudio Pérez — Tercerizar el servicio de IT");
        assertThat(mail.responderA()).isEqualTo("juan@estudio.com");
        assertThat(mail.texto()).contains("Juan Pérez", "Somos 12 PCs y un servidor");
        assertThat(mail.adjuntos()).isEmpty();
    }

    @Test
    void unaEmpresaTieneQueDecirElMotivoYElMensaje() {
        assertThatThrownBy(() -> servicio.consultarComoEmpresa(new ConsultaDeEmpresa("Estudio", "Juan", "juan@estudio.com",
                "3415550000", null, "Hola"))).hasMessageContaining("motivo");
        assertThatThrownBy(() -> servicio.consultarComoEmpresa(new ConsultaDeEmpresa("Estudio", "Juan", "juan@estudio.com",
                "3415550000", MotivoDeEmpresa.ALIANZA_COMERCIAL, ""))).hasMessageContaining("mensaje");
    }

    @Test
    void unSaltoDeLineaEnElNombreNoRompeElAsunto() {
        servicio.consultarComoEmpresa(new ConsultaDeEmpresa("Estudio\r\nBcc: spam@x.com", "Juan", "juan@estudio.com",
                "3415550000", MotivoDeEmpresa.OTRO, "Hola"));

        assertThat(enviados.getFirst().asunto()).doesNotContain("\r", "\n");
    }
}
