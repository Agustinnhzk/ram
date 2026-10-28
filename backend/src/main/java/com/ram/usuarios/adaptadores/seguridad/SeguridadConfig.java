package com.ram.usuarios.adaptadores.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;

/**
 * Quién puede entrar a cada parte de la API (ADR-003 y ADR-006).
 *
 * <pre>
 * /api/publico/**          cualquiera
 * /api/tienda/**           clientes logueados (menos login y registro, que son abiertos)
 * /api/sistema/**          personal logueado (menos el login)
 * cualquier otra cosa      nadie
 * </pre>
 */
@Configuration
class SeguridadConfig {

    @Bean
    SecurityFilterChain reglasDeAcceso(HttpSecurity http, SecurityContextRepository repositorioDeContexto) throws Exception {
        return http
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/publico/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sistema/sesion").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tienda/sesion", "/api/tienda/registro").permitAll()
                        .requestMatchers("/api/sistema/**").hasRole("PERSONAL")
                        .requestMatchers("/api/tienda/**").hasRole("CLIENTE")
                        // Lo que no está explícitamente permitido, está prohibido.
                        .anyRequest().denyAll())
                // La sesión (quién está logueado) se lee y se guarda en la sesión HTTP, que vive en PostgreSQL.
                .securityContext(contexto -> contexto.securityContextRepository(repositorioDeContexto))
                // CSRF: como la sesión viaja en una cookie, otra web podría intentar mandar pedidos
                // "en nombre" del usuario. Para evitarlo, todo pedido que modifica algo tiene que traer
                // además el token del cookie XSRF-TOKEN en el header X-XSRF-TOKEN. Otra web no puede
                // leer esa cookie, así que no puede armar el header.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                // Es una API: sin sesión responde 401, no redirige a una página de login.
                .exceptionHandling(errores -> errores.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Spring guarda en la sesión el pedido rechazado, para "volver ahí" después de un login
                // con formulario. Una API no lo necesita, y eso creaba una sesión vacía por cada 401.
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .build();

    }

    @Bean
    SecurityContextRepository repositorioDeContexto() {
        return new HttpSessionSecurityContextRepository();
    }

    /** BCrypt, con el prefijo {bcrypt} en cada hash: si un día cambiamos de algoritmo, los hashes viejos siguen andando. */
    @Bean
    PasswordEncoder cifradorDeSpring() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * No usamos el login automático de Spring (UserDetailsService): el login lo hacen nuestros
     * casos de uso, que distinguen personal de clientes y respetan el taller.
     * Declarar este bean evita que Spring Boot cree un usuario "user" con contraseña aleatoria.
     */
    @Bean
    UserDetailsService sinUsuariosPorDefecto() {
        return nombre -> {
            throw new UsernameNotFoundException("El login se resuelve en ServicioDeAcceso");
        };
    }
}
