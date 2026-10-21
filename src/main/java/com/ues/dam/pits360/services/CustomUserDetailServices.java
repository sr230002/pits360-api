package com.ues.dam.pits360.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Carga de usuarios para Spring Security: el email es el username.
 * El bean es singleton, así que no guarda estado entre peticiones.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailServices implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = findByEmail(username);
		return org.springframework.security.core.userdetails.User
				.withUsername(usuario.getEmail())
				.password(usuario.getPasswordHash())
				.roles("USUARIO")
				.build();
	}

	/** Devuelve la entidad Usuario; lo usa AuthServicesImpl para armar el AuthResponse. */
	public Usuario findByEmail(String email) {
		return usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
	}
}
