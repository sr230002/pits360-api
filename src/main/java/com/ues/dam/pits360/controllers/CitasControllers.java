package com.ues.dam.pits360.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.entities.Cita;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.services.CitasServices;
import com.ues.dam.pits360.utilities.HandlerResponse;
import com.ues.dam.pits360.validations.OnCreate;
import com.ues.dam.pits360.validations.OnUpdate;

import lombok.RequiredArgsConstructor;

/**
 * Recurso {@code /citas}: CRUD de citas de servicio.
 *
 * <p>
 * Las lecturas (GET) devuelven la entidad pelada: la envuelve
 * {@code ResponseWrapperAdvice} en {@link ApiResponse} con 200. Las
 * operaciones con código o mensaje propios (201 de creación, borrado) usan
 * {@link HandlerResponse}; los errores los traduce
 * {@code GlobalExceptionHandler}. El {@code DELETE} devuelve 200 porque el
 * envelope necesita cuerpo, nunca 204.
 */
@RestController
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitasControllers {

	private final CitasServices citasServices;

	/** 200 con la lista (envuelta por el advice); si está vacía devuelve {@code data: []}. */
	@GetMapping
	public List<Cita> getAll(@RequestParam(required = false) String estado) {
		return citasServices.getAll(estado);
	}

	/** 200 con la cita · 404 si no existe. */
	@GetMapping("/{id}")
	public Cita getById(@PathVariable String id) {
		return citasServices.getById(id);
	}

	/** 201 con la cita creada · 400 si el body no valida. */
	@PostMapping
	public ResponseEntity<ApiResponse<Cita>> create(
			@Validated(OnCreate.class) @RequestBody Cita cita) {
		return HandlerResponse.created(citasServices.create(cita), "Cita creada exitosamente");
	}

	/** 200 con la cita actualizada · 404 si no existe · 400 si el body no valida. */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Cita>> update(@PathVariable String id,
			@Validated(OnUpdate.class) @RequestBody Cita cita) {
		return HandlerResponse.ok(citasServices.update(id, cita), "Cita actualizada exitosamente");
	}

	/** 200 con {@code data=true} · 404 si no existe. */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable String id) {
		citasServices.delete(id);
		return HandlerResponse.ok(true, "Cita eliminada exitosamente");
	}

	/** 200 con la cita aprobada (CONFIRMADA) · 404/409. */
	@PostMapping("/{id}/aprobar")
	public ResponseEntity<ApiResponse<Cita>> aprobar(@PathVariable String id) {
		return HandlerResponse.ok(citasServices.aprobar(id), "Cita aprobada");
	}

	/** 200 con la cita rechazada (CANCELADA) · 404/409. */
	@PostMapping("/{id}/rechazar")
	public ResponseEntity<ApiResponse<Cita>> rechazar(@PathVariable String id) {
		return HandlerResponse.ok(citasServices.rechazar(id), "Cita rechazada");
	}

	/** 201 con la orden creada a partir de la cita · 404/409/400. */
	@PostMapping("/{id}/convertir")
	public ResponseEntity<ApiResponse<Orden>> convertir(@PathVariable String id,
			@RequestBody(required = false) Orden datos) {
		return HandlerResponse.created(citasServices.convertir(id, datos), "Orden creada a partir de la cita");
	}
}
