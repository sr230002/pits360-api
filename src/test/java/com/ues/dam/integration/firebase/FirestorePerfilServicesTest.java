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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ues.dam.integration.firebase.dto.PerfilRequest;
import com.ues.dam.integration.firebase.dto.PerfilResultado;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.integration.firebase.service.FirestorePerfilServices;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.utilities.JwtUtil;

/**
 * Tests de actualización de perfil sobre Firestore con Mockito puro (sin
 * Spring, sin emulador): se doblea el repositorio, el {@link PasswordEncoder}
 * y el {@link JwtUtil}.
 */
@ExtendWith(MockitoExtension.class)
class FirestorePerfilServicesTest {

	private static final String EMAIL = "ana@b.com";

	@Mock
	private FirestoreRepository<Usuario> repository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	private FirestorePerfilServices service;

	@BeforeEach
	void setUp() {
		service = new FirestorePerfilServices(repository, passwordEncoder, jwtUtil);
	}

	@Test
	void actualizarConPasswordActualIncorrectaLanza401() {
		when(repository.findBy("email", EMAIL)).thenReturn(List.of(usuario()));
		when(passwordEncoder.matches("mala", "hash-bcrypt")).thenReturn(false);

		assertThatThrownBy(() -> service.actualizar(EMAIL, request("Ana", "mala", "nueva123")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));

		verify(repository, never()).save(any(Usuario.class));
	}

	@Test
	void actualizarConPasswordNuevaCortaLanza400() {
		when(repository.findBy("email", EMAIL)).thenReturn(List.of(usuario()));
		when(passwordEncoder.matches("actual", "hash-bcrypt")).thenReturn(true);

		assertThatThrownBy(() -> service.actualizar(EMAIL, request(null, "actual", "corta")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

		verify(repository, never()).save(any(Usuario.class));
	}

	@Test
	void actualizarConNombreValidoGuardaYDevuelveNombreNuevo() {
		when(repository.findBy("email", EMAIL)).thenReturn(List.of(usuario()));
		when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		PerfilResultado resultado = service.actualizar(EMAIL, request("  Ana Nueva  ", "actual", null));

		assertThat(resultado.usuario().getNombre()).isEqualTo("Ana Nueva");
		assertThat(resultado.usuario().getEmail()).isEqualTo(EMAIL);
		verify(repository).save(any(Usuario.class));
	}

	@Test
	void actualizarTelefonoYCiudadGuardaLosCampos() {
		when(repository.findBy("email", EMAIL)).thenReturn(List.of(usuario()));
		when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		PerfilRequest req = request(null, null, null);
		req.setTelefono("  7011-2233  ");
		req.setCiudad("  San Salvador  ");

		PerfilResultado resultado = service.actualizar(EMAIL, req);

		assertThat(resultado.usuario().getTelefono()).isEqualTo("7011-2233");
		assertThat(resultado.usuario().getCiudad()).isEqualTo("San Salvador");
		verify(repository).save(any(Usuario.class));
	}

	@Test
	void actualizarConCorreoDuplicadoLanza409() {
		when(repository.findBy("email", EMAIL)).thenReturn(List.of(usuario()));
		when(repository.findBy("email", "otro@b.com")).thenReturn(List.of(usuario("id-2", "otro@b.com")));

		PerfilRequest req = request(null, null, null);
		req.setEmail("Otro@B.com");

		assertThatThrownBy(() -> service.actualizar(EMAIL, req))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
					assertThat(e.getMessage()).isEqualTo("El correo ya está registrado");
				});

		verify(repository, never()).save(any(Usuario.class));
	}

	@Test
	void actualizarConCorreoNuevoDevuelveTokenYGuardaElCorreo() {
		when(repository.findBy("email", EMAIL)).thenReturn(List.of(usuario()));
		when(repository.findBy("email", "nuevo@b.com")).thenReturn(List.of());
		when(jwtUtil.generateToken("nuevo@b.com")).thenReturn("token-nuevo");
		when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		PerfilRequest req = request(null, null, null);
		req.setEmail("  Nuevo@B.com  ");

		PerfilResultado resultado = service.actualizar(EMAIL, req);

		assertThat(resultado.token()).isEqualTo("token-nuevo");
		assertThat(resultado.usuario().getEmail()).isEqualTo("nuevo@b.com");
		verify(repository).save(any(Usuario.class));
	}

	@Test
	void actualizarSinCambiarElCorreoNoDevuelveToken() {
		when(repository.findBy("email", EMAIL)).thenReturn(List.of(usuario()));
		when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		PerfilRequest req = request("Ana", null, null);
		req.setEmail(EMAIL);

		PerfilResultado resultado = service.actualizar(EMAIL, req);

		assertThat(resultado.token()).isNull();
		assertThat(resultado.usuario().getEmail()).isEqualTo(EMAIL);
	}

	@Test
	void actualizarConUsuarioInexistenteLanza404() {
		when(repository.findBy("email", EMAIL)).thenReturn(List.of());

		assertThatThrownBy(() -> service.actualizar(EMAIL, request(null, null, null)))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	private PerfilRequest request(String nombre, String passwordActual, String passwordNueva) {
		PerfilRequest req = new PerfilRequest();
		req.setNombre(nombre);
		req.setPasswordActual(passwordActual);
		req.setPasswordNueva(passwordNueva);
		return req;
	}

	private Usuario usuario() {
		return usuario("id-1", EMAIL);
	}

	private Usuario usuario(String id, String email) {
		return Usuario.builder()
				.id(id)
				.nombre("Ana")
				.email(email)
				.passwordHash("hash-bcrypt")
				.rol("cliente")
				.build();
	}

}
