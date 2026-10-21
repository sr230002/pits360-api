package com.ues.dam.pits360.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ues.dam.pits360.entities.Adjunto;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.AdjuntoRepository;
import com.ues.dam.pits360.repositories.OrdenAdjuntoRepository;
import com.ues.dam.pits360.repositories.OrdenRepository;
import com.ues.dam.pits360.repositories.UsuarioRepository;
import com.ues.dam.pits360.repositories.VehiculoRepository;
import com.ues.dam.pits360.utilities.CloudinaryService;

@ExtendWith(MockitoExtension.class)
class AdjuntoServicesImplTest {

	private static final String ID = "123e4567-e89b-12d3-a456-426614174000";
	private static final String OTRO_ID = "00000000-0000-0000-0000-000000000abc";

	@Mock
	private AdjuntoRepository adjuntoRepository;
	@Mock
	private OrdenAdjuntoRepository ordenAdjuntoRepository;
	@Mock
	private CloudinaryService cloudinaryService;
	@Mock
	private UsuarioRepository usuarioRepository;
	@Mock
	private VehiculoRepository vehiculoRepository;
	@Mock
	private OrdenRepository ordenRepository;

	@InjectMocks
	private AdjuntoServicesImpl service;

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	private MockMultipartFile archivo(String contentType, int size) {
		byte[] data = new byte[size];
		return new MockMultipartFile("file", "test.bin", contentType, data);
	}

	private void autenticarComo(String email, String userId) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(email, null, List.of()));
		when(usuarioRepository.findByEmail(email))
				.thenReturn(Optional.of(Usuario.builder().id(userId).email(email).nombre("U").rol("cliente").passwordHash("h").build()));
	}

	@Test
	void subirArchivoNullLanza400() {
		assertThatThrownBy(() -> service.subir(null, "APP_LOGOS", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirArchivoVacioLanza400() {
		MockMultipartFile vacio = new MockMultipartFile("file", "vacio.txt", "image/jpeg", new byte[0]);
		assertThatThrownBy(() -> service.subir(vacio, "APP_LOGOS", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirArchivoMayorA5MBLanza400() {
		MockMultipartFile grande = archivo("image/jpeg", (int) (5L * 1024 * 1024) + 1);
		assertThatThrownBy(() -> service.subir(grande, "APP_LOGOS", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirTipoNoPermitidoLanza400() {
		MockMultipartFile txt = archivo("text/plain", 10);
		assertThatThrownBy(() -> service.subir(txt, "APP_LOGOS", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void categoriaInvalidaLanza400() {
		MockMultipartFile file = archivo("image/jpeg", 10);
		assertThatThrownBy(() -> service.subir(file, "NO_EXISTE", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void ambosDuenosALaVezLanza400() {
		MockMultipartFile file = archivo("image/jpeg", 10);
		assertThatThrownBy(() -> service.subir(file, "APP_LOGOS", null, ID, OTRO_ID, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void sinAutenticacionEnCategoriaConUsuarioLanza401() {
		MockMultipartFile file = archivo("image/jpeg", 10);
		assertThatThrownBy(() -> service.subir(file, "CLIENTE_PERFIL", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
	}

	@Test
	void usuarioIdDeOtroLanza403() {
		autenticarComo("u@test.com", "u1");
		MockMultipartFile file = archivo("image/jpeg", 10);
		assertThatThrownBy(() -> service.subir(file, "CLIENTE_PERFIL", "u2", null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
	}

	@Test
	void limite20PorOrdenLanza400() {
		when(ordenRepository.findById(ID)).thenReturn(Optional.of(new Orden()));
		when(ordenAdjuntoRepository.countByOrdenId(ID)).thenReturn(20L);
		MockMultipartFile file = archivo("image/jpeg", 10);
		assertThatThrownBy(() -> service.subir(file, "ORDEN_EVIDENCIA", null, null, ID, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void vehiculoInexistenteLanza404() {
		autenticarComo("u@test.com", "u1");
		when(vehiculoRepository.findById(ID)).thenReturn(Optional.empty());
		MockMultipartFile file = archivo("image/jpeg", 10);
		assertThatThrownBy(() -> service.subir(file, "CLIENTE_VEHICULO", "u1", ID, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void ordenInexistenteLanza404() {
		when(ordenRepository.findById(ID)).thenReturn(Optional.empty());
		MockMultipartFile file = archivo("image/jpeg", 10);
		assertThatThrownBy(() -> service.subir(file, "ORDEN_EVIDENCIA", null, null, ID, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void cloudinaryFallaLanza502() {
		when(cloudinaryService.upload(any(), anyString()))
				.thenThrow(new ApiException(HttpStatus.BAD_GATEWAY, "fallo cloud"));
		MockMultipartFile file = archivo("image/jpeg", 10);
		assertThatThrownBy(() -> service.subir(file, "APP_LOGOS", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY));
	}

	@Test
	void subirOkDevuelveAdjunto() {
		when(cloudinaryService.upload(any(), anyString())).thenReturn(Map.of(
				"secure_url", "https://res.cloudinary.com/x/image/upload/v1/a.jpg",
				"public_id", "pits360/app/logos/a",
				"format", "jpg",
				"bytes", 10L,
				"original_filename", "a.jpg"));
		when(adjuntoRepository.save(any(Adjunto.class))).thenAnswer(inv -> inv.getArgument(0));
		MockMultipartFile file = archivo("image/jpeg", 10);
		Adjunto guardado = service.subir(file, "APP_LOGOS", null, null, null, null);
		assertThat(guardado).isNotNull();
		assertThat(guardado.getUrl()).contains("cloudinary");
		assertThat(guardado.getTipoArchivo()).isEqualTo("imagen");
	}
}