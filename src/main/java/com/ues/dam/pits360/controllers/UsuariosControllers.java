package com.ues.dam.pits360.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.services.UsuariosServices;
import com.ues.dam.pits360.utilities.HandlerResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuariosControllers {

	private final UsuariosServices usuariosServices;

	@GetMapping
	public ResponseEntity<ApiResponse<List<Usuario>>> getAll(
			@RequestParam(required = false) String rol) {
		return HandlerResponse.ok(usuariosServices.getAll(rol), "OK");
	}

	@GetMapping("/{id}")
	public Usuario getById(@PathVariable String id) {
		return usuariosServices.getById(id);
	}
}