package com.ues.dam.pits360.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.entities.Adjunto;
import com.ues.dam.pits360.entities.OrdenAdjunto;
import com.ues.dam.pits360.services.AdjuntoServices;
import com.ues.dam.pits360.utilities.HandlerResponse;

import lombok.RequiredArgsConstructor;

/**
 * Recurso {@code /adjuntos}: subida real a Cloudinary sobre la tabla central
 * {@code adjunto}. multipart/form-data: el cliente manda la categoría de la
 * lista blanca y los ids de contexto; la carpeta {@code pits360/...} la
 * compone el servidor, nunca el cliente.
 *
 * <p>
 * Los errores (400 de límites de negocio, 404, 502 de Cloudinary, 413 del
 * límite multipart) los traduce {@code GlobalExceptionHandler} al envelope.
 */
@RestController
@RequestMapping("/adjuntos")
@RequiredArgsConstructor
public class AdjuntosControllers {

	private final AdjuntoServices adjuntoServices;

	/** 200 con el {@link Adjunto} persistido o 400/413/502 si algo falla. */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<Adjunto>> subir(
			@RequestParam("file") MultipartFile file,
			@RequestParam("categoria") String categoria,
			@RequestParam(value = "usuarioId", required = false) String usuarioId,
			@RequestParam(value = "vehiculoId", required = false) String vehiculoId,
			@RequestParam(value = "ordenId", required = false) String ordenId,
			@RequestParam(value = "descripcion", required = false) String descripcion) {
		return HandlerResponse.ok(
				adjuntoServices.subir(file, categoria, usuarioId, vehiculoId, ordenId, descripcion),
				"Adjunto subido exitosamente");
	}

	/** 200 con la lista de adjuntos del vehículo. */
	@GetMapping("/vehiculo/{vehiculoId}")
	public List<Adjunto> listarPorVehiculo(@PathVariable String vehiculoId) {
		return adjuntoServices.listarPorVehiculo(vehiculoId);
	}

	/** 200 con la lista de la pivote (descripcion, visible, esPrincipal) + su adjunto. */
	@GetMapping("/orden/{ordenId}")
	public List<OrdenAdjunto> listarPorOrden(@PathVariable String ordenId) {
		return adjuntoServices.listarPorOrden(ordenId);
	}

	/** 200 con {@code {adjuntoId, eliminado:true}} o 404 si no existe. */
	@DeleteMapping("/{adjuntoId}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> eliminar(@PathVariable String adjuntoId) {
		return HandlerResponse.ok(adjuntoServices.eliminar(adjuntoId), "Adjunto eliminado exitosamente");
	}
}
