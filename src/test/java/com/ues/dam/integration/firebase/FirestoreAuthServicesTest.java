package com.ues.dam.integration.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.integration.firebase.service.FirestoreAuthServices;
import com.ues.dam.pits360.dtos.AuthResponse;
import com.ues.dam.pits360.dtos.LoginRequest;
import com.ues.dam.pits360.dtos.RegisterDTO;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.utilities.JwtUtil;

/**
 * Tests de autenticación sobre Firestore con Mockito puro (sin Spring, sin
 * emulador): se doblea el repositorio, el {@link AuthenticationManager}, el
 * {@link JwtUtil} y el {@link PasswordEncoder}.
 */
@ExtendWith(MockitoExtension.class)
class FirestoreAuthServicesTest {

	private static final String TOKEN = "token-jwt-test";

	@Mock
	private FirestoreRepository<Usuario> repository;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private PasswordEncoder passwordEncoder;

	private FirestoreAuthServices service;

	@BeforeEach
	void setUp() {
		service = new FirestoreAuthServices(repository, authenticationManager, jwtUtil, passwordEncoder);
	}

	@Test
	void registerConEmailExistenteLanza409() {
		when(repository.findBy("email", "dup@b.com")).thenReturn(List.of(usuario("dup@b.com")));

		assertThatThrownBy(() -> service.register(register("Ana", "dup@b.com", "pass123")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException api = (ApiException) e;
					assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
					assertThat(api.getMessage()).isEqualTo("El correo ya está registrado");
				});
		verify(repository, never()).save(any(Usuario.class));
	}

	@Test
	void loginConPasswordIncorrectaLanza401() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("mal"));

		assertThatThrownBy(() -> service.login(login("a@b.com", "mala")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException api = (ApiException) e;
					assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
					assertThat(api.getMessage()).isEqualTo("Credenciales inválidas");
				});
	}

	@Test
	void meDeEmailInexistenteLanza404() {
		when(repository.findBy("email", "nope@b.com")).thenReturn(List.of());

		assertThatThrownBy(() -> service.me("nope@b.com"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException api = (ApiException) e;
					assertThat(api.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
					assertThat(api.getMessage()).isEqualTo("Usuario no encontrado: nope@b.com");
				});
	}

	@Test
	void registerOkDevuelveTokenYRolCliente() {
		when(repository.findBy("email", "ana@b.com")).thenReturn(List.of());
		when(passwordEncoder.encode("pass123")).thenReturn("hash-bcrypt");
		when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
		when(jwtUtil.generateToken("ana@b.com")).thenReturn(TOKEN);

		AuthResponse resp = service.register(register("Ana", "ANA@b.com", "pass123"));

		assertThat(resp.getToken()).isNotBlank();
		assertThat(resp.getEmail()).isEqualTo("ana@b.com");
		assertThat(resp.getRol()).isEqualTo("cliente");
		assertThat(resp.getId()).isNotBlank();
		assertThat(resp.getNombre()).isEqualTo("Ana");
	}

	@Test
	void loginOkDevuelveTokenYDatosDelUsuario() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
		when(repository.findBy("email", "login@b.com")).thenReturn(List.of(usuario("login@b.com")));
		when(jwtUtil.generateToken("login@b.com")).thenReturn(TOKEN);

		AuthResponse resp = service.login(login("LOGIN@b.com", "buena"));

		assertThat(resp.getToken()).isEqualTo(TOKEN);
		assertThat(resp.getId()).isEqualTo("id-1");
		assertThat(resp.getRol()).isEqualTo("cliente");

		ArgumentCaptor<UsernamePasswordAuthenticationToken> enviado = ArgumentCaptor
				.forClass(UsernamePasswordAuthenticationToken.class);
		verify(authenticationManager).authenticate(enviado.capture());
		assertThat(enviado.getValue().getPrincipal()).isEqualTo("login@b.com");
	}

	private RegisterDTO register(String nombre, String email, String password) {
		RegisterDTO dto = new RegisterDTO();
		dto.setNombre(nombre);
		dto.setEmail(email);
		dto.setPassword(password);
		return dto;
	}

	private LoginRequest login(String email, String password) {
		LoginRequest req = new LoginRequest();
		req.setEmail(email);
		req.setPassword(password);
		return req;
	}

	private Usuario usuario(String email) {
		return Usuario.builder()
				.id("id-1")
				.nombre("Ana")
				.email(email)
				.passwordHash("hash-bcrypt")
				.rol("cliente")
				.build();
	}

}
