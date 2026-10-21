package com.ues.dam.pits360.configs;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ues.dam.pits360.dtos.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuración de Spring Security: API stateless con JWT.
 * Los matchers son relativos al context-path (el context-path se elimina antes).
 *
 * <p>
 * El 401/403 que escribe Spring Security usa el mismo envelope que el resto de
 * la API: {@code {"data":null,"message":"...","method":"..."}}.
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtFilter jwtFilter;
	private final ObjectMapper objectMapper;

	/** BCrypt puro: el hash se guarda sin el prefijo {@code {bcrypt}}. */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(AbstractHttpConfigurer::disable) // las cabeceras CORS las pone el JwtFilter
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register",
								"/auth/verify-token", "/auth/logout").permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(e -> e
						.authenticationEntryPoint((request, response, authException) ->
								writeError(request, response, HttpServletResponse.SC_UNAUTHORIZED,
										"No autenticado: envía un token Bearer válido"))
						.accessDeniedHandler((request, response, accessDeniedException) ->
								writeError(request, response, HttpServletResponse.SC_FORBIDDEN,
										"No tienes permisos para este recurso")))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}

	/** Escribe el error con el envelope {@code {data:null, message, method}}. */
	private void writeError(HttpServletRequest request, HttpServletResponse response, int status, String msj)
			throws IOException {
		ApiResponse<Object> body = new ApiResponse<>(null, msj, request.getMethod());
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), body);
	}
}
