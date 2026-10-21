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
import org.springframework.web.bind.annotation.RestController;

import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.services.OrdenesServices;
import com.ues.dam.pits360.utilities.HandlerResponse;
import com.ues.dam.pits360.validations.OnCreate;
import com.ues.dam.pits360.validations.OnUpdate;

import lombok.RequiredArgsConstructor;

/**
 * Recurso {@code /ordenes}: CRUD de órdenes de trabajo.
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
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenesControllers {

	private final OrdenesServices ordenesServices;

	/** 200 con la lista (envuelta por el advice); si está vacía devuelve {@code data: []}. */
	@GetMapping
	public List<Orden> getAll() {
		return ordenesServices.getAll();
	}

	/** 200 con la orden · 404 si no existe. */
	@GetMapping("/{id}")
	public Orden getById(@PathVariable String id) {
		return ordenesServices.getById(id);
	}

	/** 201 con la orden creada · 400 si el body no valida. */
	@PostMapping
	public ResponseEntity<ApiResponse<Orden>> create(
			@Validated(OnCreate.class) @RequestBody Orden orden) {
		return HandlerResponse.created(ordenesServices.create(orden), "Orden creada exitosamente");
	}

	/** 200 con la orden actualizada · 404 si no existe · 400 si el body no valida. */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Orden>> update(@PathVariable String id,
			@Validated(OnUpdate.class) @RequestBody Orden orden) {
		return HandlerResponse.ok(ordenesServices.update(id, orden), "Orden actualizada exitosamente");
	}

	/** 200 con {@code data=true} · 404 si no existe. */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable String id) {
		ordenesServices.delete(id);
		return HandlerResponse.ok(true, "Orden eliminada exitosamente");
	}

	/** 200 con las órdenes del vehículo (envuelta por el advice). */
	@GetMapping("/vehiculo/{vehiculoId}")
	public List<Orden> getByVehiculo(@PathVariable String vehiculoId) {
		return ordenesServices.getByVehiculo(vehiculoId);
	}

	/** 200 con las órdenes del técnico (envuelta por el advice). */
	@GetMapping("/tecnico/{tecnicoId}")
	public List<Orden> getByTecnico(@PathVariable String tecnicoId) {
		return ordenesServices.getByTecnico(tecnicoId);
	}
}
