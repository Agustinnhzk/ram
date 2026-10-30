package com.ram.notificaciones.adaptadores;

import com.ram.notificaciones.Adjunto;
import com.ram.notificaciones.CorreoNoEnviadoException;
import com.ram.notificaciones.Correos;
import com.ram.notificaciones.MensajeAUnaPersona;
import com.ram.notificaciones.MensajeAlTaller;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;

/** Manda los mails por SMTP (Gmail en pruebas; un servicio de mails transaccionales en producción). */
class CorreosSmtp implements Correos {

    private final JavaMailSender enviador;
    private final CorreoProperties propiedades;

    CorreosSmtp(JavaMailSender enviador, CorreoProperties propiedades) {
        this.enviador = enviador;
        this.propiedades = propiedades;
    }

    @Override
    public void enviarAlTaller(MensajeAlTaller mensaje) {
        try {
            MimeMessage mail = enviador.createMimeMessage();
            // true = "multipart": hace falta para los adjuntos.
            MimeMessageHelper ayudante = new MimeMessageHelper(mail, true, StandardCharsets.UTF_8.name());
            ayudante.setFrom(propiedades.remitente());
            ayudante.setTo(propiedades.destinoDelTaller());
            if (mensaje.responderA() != null) {
                ayudante.setReplyTo(mensaje.responderA());
            }
            ayudante.setSubject(mensaje.asunto());
            ayudante.setText(mensaje.texto(), false);
            for (Adjunto adjunto : mensaje.adjuntos()) {
                ayudante.addAttachment(adjunto.nombre(), new ByteArrayResource(adjunto.contenido()), adjunto.tipoDeContenido());
            }
            enviador.send(mail);
        } catch (MessagingException | MailException e) {
            throw new CorreoNoEnviadoException("No se pudo mandar el mail: " + mensaje.asunto(), e);
        }
    }

    @Override
    public void enviarA(MensajeAUnaPersona mensaje) {
        try {
            MimeMessage mail = enviador.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mail, false, StandardCharsets.UTF_8.name());
            ayudante.setFrom(propiedades.remitente());
            ayudante.setTo(mensaje.destinatario());
            ayudante.setSubject(mensaje.asunto());
            ayudante.setText(mensaje.texto(), false);
            enviador.send(mail);
        } catch (MessagingException | MailException e) {
            throw new CorreoNoEnviadoException("No se pudo mandar el mail: " + mensaje.asunto(), e);
        }
    }
}
