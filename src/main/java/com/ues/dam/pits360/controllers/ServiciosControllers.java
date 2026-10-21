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
import com.ues.dam.pits360.entities.Servicio;
import com.ues.dam.pits360.services.ServiciosServices;
import com.ues.dam.pits360.utilities.HandlerResponse;
import com.ues.dam.pits360.validations.OnCreate;
import com.ues.dam.pits360.validations.OnUpdate;

import lombok.RequiredArgsConstructor;

/**
 * Recurso {@code /servicios}: CRUD del catálogo de servicios.
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
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServiciosControllers {

	private final ServiciosServices serviciosServices;

	/** 200 con la lista (envuelta por el advice); si está vacía devuelve {@code data: []}. */
	@GetMapping
	public List<Servicio> getAll() {
		return serviciosServices.getAll();
	}

	/** 200 con el servicio o 404 si no existe. */
	@GetMapping("/{id}")
	public Servicio getById(@PathVariable String id) {
		return serviciosServices.getById(id);
	}

	/** 201 con el servicio creado o 400 si el body no valida. */
	@PostMapping
	public ResponseEntity<ApiResponse<Servicio>> create(
			@Validated(OnCreate.class) @RequestBody Servicio servicio) {
		return HandlerResponse.created(serviciosServices.create(servicio),
				"Servicio creado exitosamente");
	}

	/** 200 con el servicio actualizado, 404 si no existe o 400 si el body no valida. */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Servicio>> update(@PathVariable String id,
			@Validated(OnUpdate.class) @RequestBody Servicio servicio) {
		return HandlerResponse.ok(serviciosServices.update(id, servicio),
				"Servicio actualizado exitosamente");
	}

	/** 200 con cuerpo o 404 si no existe. */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable String id) {
		serviciosServices.delete(id);
		return HandlerResponse.ok(true, "Servicio eliminado exitosamente");
	}
}
