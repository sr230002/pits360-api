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
import com.ues.dam.pits360.entities.Taller;
import com.ues.dam.pits360.services.TalleresServices;
import com.ues.dam.pits360.utilities.HandlerResponse;
import com.ues.dam.pits360.validations.OnCreate;
import com.ues.dam.pits360.validations.OnUpdate;

import lombok.RequiredArgsConstructor;

/**
 * Recurso {@code /talleres}: CRUD del catálogo de talleres.
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
@RequestMapping("/talleres")
@RequiredArgsConstructor
public class TalleresControllers {

	private final TalleresServices talleresServices;

	/** 200 con la lista (envuelta por el advice); si está vacía devuelve {@code data: []}. */
	@GetMapping
	public List<Taller> getAll() {
		return talleresServices.getAll();
	}

	/** 200 con el taller o 404 si no existe. */
	@GetMapping("/{id}")
	public Taller getById(@PathVariable String id) {
		return talleresServices.getById(id);
	}

	/** 201 con el taller creado o 400 si el body no valida. */
	@PostMapping
	public ResponseEntity<ApiResponse<Taller>> create(
			@Validated(OnCreate.class) @RequestBody Taller taller) {
		return HandlerResponse.created(talleresServices.create(taller),
				"Taller creado exitosamente");
	}

	/** 200 con el taller actualizado, 404 si no existe o 400 si el body no valida. */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Taller>> update(@PathVariable String id,
			@Validated(OnUpdate.class) @RequestBody Taller taller) {
		return HandlerResponse.ok(talleresServices.update(id, taller),
				"Taller actualizado exitosamente");
	}

	/** 200 con cuerpo o 404 si no existe. */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable String id) {
		talleresServices.delete(id);
		return HandlerResponse.ok(true, "Taller eliminado exitosamente");
	}
}
