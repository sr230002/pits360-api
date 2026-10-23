package com.ues.dam.integration.firebase.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ues.dam.integration.firebase.dto.PerfilRequest;
import com.ues.dam.integration.firebase.dto.PerfilResultado;
import com.ues.dam.integration.firebase.service.PerfilServices;
import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;

import lombok.RequiredArgsConstructor;

/**
 * Actualización del perfil del usuario autenticado.
 *
 * <p>
 * El email sale del {@code SecurityContextHolder} (lo deja el {@code JwtFilter}),
 * nunca del cuerpo de la petición: así solo se puede editar el perfil propio.
 * La respuesta nunca incluye el hash de la contraseña.
 *
 * <p>
 * Respuesta: {@code {"data": {id, nombre, email, rol, telefono, ciudad,
 * fotoUrl}, "token": null, "message": "...", "method": "PUT"}}. El
 * {@code token} solo viene informado cuando el correo cambió (el JWT lleva el
 * email como subject).
 */
@RestController
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilController {

	private final PerfilServices perfilServices;

	/** PUT {@code /perfil}: actualiza los datos del usuario autenticado. */
	@PutMapping
	public ResponseEntity<PerfilResponse> actualizar(@RequestBody PerfilRequest request) {
		PerfilResultado resultado = perfilServices.actualizar(emailAutenticado(), request);
		return ResponseEntity.ok(new PerfilResponse(datosPublicos(resultado.usuario()), resultado.token(),
				"Perfil actualizado", ApiResponse.currentMethod()));
	}

	/** 401 si la petición no llega autenticada. */
	private String emailAutenticado() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getName() == null || auth.getName().isBlank()) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado: envía un token Bearer válido");
		}
		return auth.getName();
	}

	/** Datos públicos del usuario: sin hash de contraseña. */
	private Map<String, Object> datosPublicos(Usuario usuario) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("id", usuario.getId());
		data.put("nombre", usuario.getNombre());
		data.put("email", usuario.getEmail());
		data.put("rol", usuario.getRol());
		data.put("telefono", usuario.getTelefono());
		data.put("ciudad", usuario.getCiudad());
		data.put("fotoUrl", usuario.getFotoUrl());
		return data;
	}

	/**
	 * Envelope del perfil: el {@code data} con los datos públicos y el
	 * {@code token} a nivel raíz, como pide el contrato de {@code PUT /perfil}.
	 * Extiende el envelope estándar para conservar {@code message} y
	 * {@code method}.
	 */
	public static class PerfilResponse extends ApiResponse<Map<String, Object>> {

		private final String token;

		public PerfilResponse(Map<String, Object> data, String token, String message, String method) {
			super(data, message, method);
			this.token = token;
		}

		public String getToken() {
			return token;
		}
	}

}
