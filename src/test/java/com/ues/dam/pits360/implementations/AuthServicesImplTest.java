package com.ues.dam.pits360.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ues.dam.pits360.dtos.AuthResponse;
import com.ues.dam.pits360.dtos.LoginRequest;
import com.ues.dam.pits360.dtos.RegisterDTO;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.UsuarioRepository;
import com.ues.dam.pits360.services.CustomUserDetailServices;
import com.ues.dam.pits360.utilities.JwtUtil;

/**
 * Tests unitarios de AuthServicesImpl con Mockito puro (sin Spring).
 */
@ExtendWith(MockitoExtension.class)
class AuthServicesImplTest {

	private static final String TOKEN = "token-jwt-test";

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private CustomUserDetailServices customUserDetailServices;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthServicesImpl service;

	@Test
	void registerOkDevuelveAuthResponseConToken() {
		when(usuarioRepository.existsByEmail("a@b.com")).thenReturn(false);
		when(passwordEncoder.encode("pass123")).thenReturn("hash");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
		when(jwtUtil.generateToken("a@b.com")).thenReturn(TOKEN);

		RegisterDTO dto = new RegisterDTO();
		dto.setNombre("Ana");
		dto.setEmail("a@b.com");
		dto.setPassword("pass123");

		AuthResponse resp = service.register(dto);

		assertThat(resp.getToken()).isEqualTo(TOKEN);
		assertThat(resp.getEmail()).isEqualTo("a@b.com");
		assertThat(resp.getRol()).isEqualTo("cliente");
	}

	@Test
	void registerConEmailExistenteLanza409() {
		when(usuarioRepository.existsByEmail("dup@b.com")).thenReturn(true);

		RegisterDTO dto = new RegisterDTO();
		dto.setNombre("Ana");
		dto.setEmail("dup@b.com");
		dto.setPassword("pass123");

		assertThatThrownBy(() -> service.register(dto))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
	}

	@Test
	void loginConBadCredentialsLanza401() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("mal"));

		LoginRequest req = new LoginRequest();
		req.setEmail("a@b.com");
		req.setPassword("mala");

		assertThatThrownBy(() -> service.login(req))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
	}

	@Test
	void loginOkDevuelveTokenYDatosDelUsuario() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(null);
		Usuario usuario = Usuario.builder()
				.id("id-1")
				.nombre("Ana")
				.email("login@b.com")
				.passwordHash("hash")
				.rol("cliente")
				.build();
		when(customUserDetailServices.findByEmail("login@b.com")).thenReturn(usuario);
		when(jwtUtil.generateToken("login@b.com")).thenReturn(TOKEN);

		LoginRequest req = new LoginRequest();
		req.setEmail("login@b.com");
		req.setPassword("buena");

		AuthResponse resp = service.login(req);

		assertThat(resp.getToken()).isEqualTo(TOKEN);
		assertThat(resp.getId()).isEqualTo("id-1");
	}

	@Test
	void logoutConTokenInvalidoLanza401() {
		when(jwtUtil.isValid(anyString())).thenReturn(false);

		assertThatThrownBy(() -> service.logout("token-malo"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
	}
}
