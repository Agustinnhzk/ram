package com.ram.contactos.aplicacion;

import com.ram.contactos.ConsultaDeEmpresa;
import com.ram.contactos.Contactos;
import com.ram.contactos.Postulacion;
import com.ram.notificaciones.Adjunto;
import com.ram.notificaciones.Correos;
import com.ram.notificaciones.MensajeAlTaller;

import java.util.List;
import java.util.Objects;

/**
 * Arma los mails de "Trabajá con nosotros". El mail se manda en el momento (no "de fondo"): si falla,
 * la persona se entera y puede reintentar o escribir por WhatsApp, en vez de creer que lo mandó.
 */
public class ServicioContactos implements Contactos {

    private final Correos correos;

    public ServicioContactos(Correos correos) {
        this.correos = Objects.requireNonNull(correos);
    }

    @Override
    public void postularse(Postulacion p) {
        String nombre = Validar.obligatorio(p.nombre(), "el nombre", 120);
        String correo = Validar.correo(p.correo());
        String telefono = Validar.telefono(p.telefono());
        String area = Validar.opcional(p.area(), "el área", 100);
        String mensaje = Validar.opcional(p.mensaje(), "el mensaje", 2000);
        if (p.cv() == null) {
            throw new IllegalArgumentException("Falta el CV");
        }

        String texto = """
                Nueva postulación desde "Trabajá con nosotros".

                Nombre:    %s
                Correo:    %s
                Teléfono:  %s
                Área:      %s

                Mensaje:
                %s

                El CV va adjunto. Para responderle, respondé este mail.
                """.formatted(nombre, correo, telefono, area == null ? "(no indicó)" : area,
                mensaje == null ? "(sin mensaje)" : mensaje);

        correos.enviarAlTaller(new MensajeAlTaller(
                Validar.unaLinea("Postulación: " + nombre + (area == null ? "" : " — " + area)),
                texto,
                correo,
                List.of(new Adjunto(p.cv().nombre(), p.cv().tipo().tipoDeContenido(), p.cv().contenido()))));
    }

    @Override
    public void consultarComoEmpresa(ConsultaDeEmpresa c) {
        String empresa = Validar.obligatorio(c.empresa(), "el nombre de la empresa", 150);
        String contacto = Validar.obligatorio(c.contacto(), "el nombre de contacto", 120);
        String correo = Validar.correo(c.correo());
        String telefono = Validar.telefono(c.telefono());
        if (c.motivo() == null) {
            throw new IllegalArgumentException("Falta el motivo de la consulta");
        }
        String mensaje = Validar.obligatorio(c.mensaje(), "el mensaje", 2000);

        String texto = """
                Nueva consulta de una empresa desde "Trabajá con nosotros".

                Motivo:    %s
                Empresa:   %s
                Contacto:  %s
                Correo:    %s
                Teléfono:  %s

                Mensaje:
                %s

                Para responderle, respondé este mail.
                """.formatted(c.motivo().descripcion(), empresa, contacto, correo, telefono, mensaje);

        correos.enviarAlTaller(new MensajeAlTaller(
                Validar.unaLinea("Empresa: " + empresa + " — " + c.motivo().descripcion()),
                texto,
                correo,
                List.of()));
    }
}
