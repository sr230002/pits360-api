package com.ues.dam.pits360.implementations;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.UsuarioRepository;
import com.ues.dam.pits360.services.UsuariosServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuariosServicesImpl implements UsuariosServices {

	private static final Set<String> ROLES_VALIDOS = Set.of("cliente", "tecnico", "admin");

	private final UsuarioRepository usuarioRepository;

	@Override
	@Transactional(readOnly = true)
	public List<Usuario> getAll(String rol) {
		if (rol == null || rol.isBlank()) {
			return usuarioRepository.findAll();
		}
		String normalizado = rol.trim().toLowerCase();
		if (!ROLES_VALIDOS.contains(normalizado)) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"Rol inválido: usa cliente|tecnico|admin");
		}
		return usuarioRepository.findByRol(normalizado);
	}

	@Override
	@Transactional(readOnly = true)
	public Usuario getById(String id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
						"Usuario no encontrado: " + id));
	}
}
