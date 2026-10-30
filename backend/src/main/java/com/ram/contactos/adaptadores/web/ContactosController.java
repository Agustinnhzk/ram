package com.ram.contactos.adaptadores.web;

import com.ram.archivos.ArchivoSubido;
import com.ram.archivos.TipoDeArchivo;
import com.ram.contactos.ConsultaDeEmpresa;
import com.ram.contactos.Contactos;
import com.ram.contactos.MotivoDeEmpresa;
import com.ram.contactos.Postulacion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * "Trabajá con nosotros", sin login.
 *
 * <pre>
 * POST /api/publico/contactos/postulacion   multipart: nombre, correo, telefono, area?, mensaje?, cv (archivo)
 * POST /api/publico/contactos/empresa       JSON: {empresa, contacto, correo, telefono, motivo, mensaje}
 *   202  recibido (el mail salió)
 *   400  datos_invalidos / archivo_invalido, con "detalle" para mostrar
 *   413  archivo de más de 5 MB
 *   429  demasiados envíos desde la misma IP
 *   503  el mail no salió: que lo intente de nuevo o escriba por WhatsApp
 * </pre>
 *
 * <p><b>Campo trampa ("sitioWeb"):</b> el formulario lo tiene escondido. Una persona no lo ve y lo deja vacío;
 * los robots que llenan formularios completan todo. Si viene con algo, se responde 202 como si nada,
 * pero no se manda: el robot no aprende que lo descubrimos.
 */
@RestController
class ContactosController {

    private static final Set<TipoDeArchivo> TIPOS_DE_CV = EnumSet.of(TipoDeArchivo.PDF, TipoDeArchivo.DOC, TipoDeArchivo.DOCX);
    private static final Map<String, String> RECIBIDO = Map.of("estado", "recibido");
    private static final Map<String, String> DEMASIADOS = Map.of(
            "error", "demasiados_envios", "detalle", "Ya recibimos varios mensajes desde tu conexión. Probá de nuevo en una hora.");

    private final Contactos contactos;
    private final LimitadorDeEnvios limitador;

    ContactosController(Contactos contactos, LimitadorDeEnvios limitador) {
        this.contactos = contactos;
        this.limitador = limitador;
    }

    @PostMapping(value = "/api/publico/contactos/postulacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Map<String, String>> postularse(@RequestParam(required = false) String nombre,
                                                   @RequestParam(required = false) String correo,
                                                   @RequestParam(required = false) String telefono,
                                                   @RequestParam(required = false) String area,
                                                   @RequestParam(required = false) String mensaje,
                                                   @RequestParam(required = false) String sitioWeb,
                                                   @RequestPart(required = false) MultipartFile cv,
                                                   HttpServletRequest request) throws IOException {
        if (esRobot(sitioWeb)) {
            return ResponseEntity.accepted().body(RECIBIDO);
        }
        if (cv == null || cv.isEmpty()) {
            throw new IllegalArgumentException("Falta el CV");
        }
        // Se revisa el archivo ANTES de contar el envío: un archivo mal elegido no le gasta un intento a la persona.
        ArchivoSubido archivo = ArchivoSubido.revisar(cv.getOriginalFilename(), cv.getBytes(), TIPOS_DE_CV);
        if (!limitador.puedeEnviar(request.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(DEMASIADOS);
        }
        contactos.postularse(new Postulacion(nombre, correo, telefono, area, mensaje, archivo));
        return ResponseEntity.accepted().body(RECIBIDO);
    }

    @PostMapping(value = "/api/publico/contactos/empresa", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, String>> empresa(@RequestBody ConsultaJson consulta, HttpServletRequest request) {
        if (esRobot(consulta.sitioWeb())) {
            return ResponseEntity.accepted().body(RECIBIDO);
        }
        if (!limitador.puedeEnviar(request.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(DEMASIADOS);
        }
        contactos.consultarComoEmpresa(new ConsultaDeEmpresa(consulta.empresa(), consulta.contacto(), consulta.correo(),
                consulta.telefono(), consulta.motivo(), consulta.mensaje()));
        return ResponseEntity.accepted().body(RECIBIDO);
    }

    private static boolean esRobot(String campoTrampa) {
        return campoTrampa != null && !campoTrampa.isBlank();
    }

    record ConsultaJson(String empresa, String contacto, String correo, String telefono, MotivoDeEmpresa motivo,
                        String mensaje, String sitioWeb) {
    }
}
