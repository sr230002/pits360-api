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
import com.ues.dam.pits360.entities.Vehiculo;
import com.ues.dam.pits360.services.VehiculosServices;
import com.ues.dam.pits360.utilities.HandlerResponse;
import com.ues.dam.pits360.validations.OnCreate;
import com.ues.dam.pits360.validations.OnUpdate;

import lombok.RequiredArgsConstructor;

/**
 * Recurso {@code /vehiculos}: CRUD del catálogo de vehículos.
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
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
public class VehiculosControllers {

	private final VehiculosServices vehiculosServices;

	/** 200 con la lista (envuelta por el advice); si está vacía devuelve {@code data: []}. */
	@GetMapping
	public List<Vehiculo> getAll() {
		return vehiculosServices.getAll();
	}

	/** 200 con el vehículo o 404 si no existe. */
	@GetMapping("/{id}")
	public Vehiculo getById(@PathVariable String id) {
		return vehiculosServices.getById(id);
	}

	/** 201 con el vehículo creado o 400 si el body no valida. */
	@PostMapping
	public ResponseEntity<ApiResponse<Vehiculo>> create(
			@Validated(OnCreate.class) @RequestBody Vehiculo vehiculo) {
		return HandlerResponse.created(vehiculosServices.create(vehiculo),
				"Vehiculo creado exitosamente");
	}

	/** 200 con el vehículo actualizado, 404 si no existe o 400 si el body no valida. */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Vehiculo>> update(@PathVariable String id,
			@Validated(OnUpdate.class) @RequestBody Vehiculo vehiculo) {
		return HandlerResponse.ok(vehiculosServices.update(id, vehiculo),
				"Vehiculo actualizado exitosamente");
	}

	/** 200 con cuerpo o 404 si no existe. */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable String id) {
		vehiculosServices.delete(id);
		return HandlerResponse.ok(true, "Vehiculo eliminado exitosamente");
	}
}
