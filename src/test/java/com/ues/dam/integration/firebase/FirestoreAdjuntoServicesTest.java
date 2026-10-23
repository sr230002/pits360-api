package com.ues.dam.integration.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.integration.firebase.service.FirestoreAdjuntoServices;
import com.ues.dam.pits360.entities.Adjunto;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.entities.OrdenAdjunto;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.entities.Vehiculo;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.utilities.CloudinaryService;

/**
 * Tests unitarios de los adjuntos sobre Firestore: Mockito puro, sin Spring
 * ni Firestore ni Cloudinary reales.
 */
class FirestoreAdjuntoServicesTest {

	private static final String USUARIO_ID = "11111111-1111-1111-1111-111111111111";
	private static final String VEHICULO_ID = "cccccccc-cccc-cccc-cccc-ccccccccccc1";
	private static final String ORDEN_ID = "dddddddd-dddd-dddd-dddd-dddddddddddd01";

	private FirestoreRepository<Adjunto> adjuntos;
	private FirestoreRepository<OrdenAdjunto> ordenAdjuntos;
	private FirestoreRepository<Usuario> usuarios;
	private FirestoreRepository<Vehiculo> vehiculos;
	private FirestoreRepository<Orden> ordenes;
	private CloudinaryService cloudinary;
	private FirestoreAdjuntoServices service;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		adjuntos = mock(FirestoreRepository.class);
		ordenAdjuntos = mock(FirestoreRepository.class);
		usuarios = mock(FirestoreRepository.class);
		vehiculos = mock(FirestoreRepository.class);
		ordenes = mock(FirestoreRepository.class);
		cloudinary = mock(CloudinaryService.class);
		service = new FirestoreAdjuntoServices(adjuntos, ordenAdjuntos, usuarios, vehiculos, ordenes, cloudinary);
	}

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	private MockMultipartFile archivo(String contentType, int size) {
		return new MockMultipartFile("file", "a.jpg", contentType, new byte[size]);
	}

	private Map<String, Object> resultadoCloudinary(String publicId) {
		return Map.of(
				"secure_url", "https://res.cloudinary.com/x/image/upload/v1/" + publicId,
				"public_id", publicId,
				"format", "jpg",
				"bytes", 10L,
				"original_filename", "a.jpg");
	}

	@Test
	void subirGuardaElAdjuntoYDevuelveLaUrl() {
		when(cloudinary.upload(any(), anyString())).thenReturn(resultadoCloudinary("pits360/app/logos/a"));
		when(adjuntos.save(any(Adjunto.class))).thenAnswer(inv -> inv.getArgument(0));

		Adjunto guardado = service.subir(archivo("image/jpeg", 10), "APP_LOGOS", null, null, null, null);

		assertThat(guardado.getUrl()).isEqualTo("https://res.cloudinary.com/x/image/upload/v1/pits360/app/logos/a");
		assertThat(guardado.getAdjuntoId()).isNotBlank();
		assertThat(guardado.getFechaCreacion()).isNotNull();
		assertThat(guardado.getTipoArchivo()).isEqualTo("imagen");
		verify(adjuntos).save(any(Adjunto.class));
	}

	@Test
	void eliminarAdjuntoInexistenteLanza404() {
		when(adjuntos.findById("no-existe")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.eliminar("no-existe"))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("no-existe")
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		verify(cloudinary, never()).delete(anyString(), anyString());
	}

	@Test
	void eliminarAdjuntoExistenteBorraEnFirestoreYEnCloudinary() {
		Adjunto adjunto = Adjunto.builder()
				.adjuntoId("adjunto-1")
				.publicId("pits360/app/logos/a")
				.tipoArchivo("imagen")
				.build();
		when(adjuntos.findById("adjunto-1")).thenReturn(Optional.of(adjunto));

		Map<String, Object> resultado = service.eliminar("adjunto-1");

		assertThat(resultado).containsEntry("adjuntoId", "adjunto-1").containsEntry("eliminado", true);
		verify(adjuntos).delete("adjunto-1");
		verify(cloudinary).delete("pits360/app/logos/a", "imagen");
	}

	@Test
	void subirConTipoNoPermitidoLanza400() {
		assertThatThrownBy(() -> service.subir(archivo("text/plain", 10), "APP_LOGOS", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(cloudinary, never()).upload(any(), anyString());
	}

	@Test
	void subirEnOrdenConVeinteAdjuntosLanza400() {
		when(ordenes.findById(ORDEN_ID)).thenReturn(Optional.of(new Orden()));
		when(ordenAdjuntos.findBy("ordenId", ORDEN_ID))
				.thenReturn(IntStream.range(0, 20).mapToObj(i -> pivote("oa-" + i)).toList());

		assertThatThrownBy(() -> service.subir(archivo("image/jpeg", 10), "ORDEN_EVIDENCIA", null, null, ORDEN_ID, null))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("20")
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		verify(cloudinary, never()).upload(any(), anyString());
	}

	@Test
	void subirFotoDeVehiculoReemplazaLaFotoAnterior() {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("u@test.com", null, List.of()));
		when(usuarios.findBy("email", "u@test.com"))
				.thenReturn(List.of(Usuario.builder().id(USUARIO_ID).email("u@test.com").build()));
		when(vehiculos.findById(VEHICULO_ID)).thenReturn(Optional.of(new Vehiculo()));
		when(cloudinary.upload(any(), anyString())).thenReturn(resultadoCloudinary("pits360/nuevo"));

		List<Adjunto> guardados = new ArrayList<>();
		when(adjuntos.save(any(Adjunto.class))).thenAnswer(inv -> {
			guardados.add(inv.getArgument(0));
			return inv.getArgument(0);
		});
		Adjunto anterior = Adjunto.builder()
				.adjuntoId("anterior")
				.vehiculoId(VEHICULO_ID)
				.publicId("pits360/anterior")
				.tipoArchivo("imagen")
				.build();
		when(adjuntos.findBy(eq("vehiculoId"), any())).thenAnswer(inv -> List.of(guardados.get(0), anterior));

		service.subir(archivo("image/jpeg", 10), "CLIENTE_VEHICULO", USUARIO_ID, VEHICULO_ID, null, null);

		verify(adjuntos).delete("anterior");
		verify(cloudinary).delete("pits360/anterior", "imagen");
	}

	private OrdenAdjunto pivote(String id) {
		return OrdenAdjunto.builder().ordenAdjuntoId(id).ordenId(ORDEN_ID).adjuntoId(id).visible(true).esPrincipal(false)
				.build();
	}

}
