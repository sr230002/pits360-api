package com.ues.dam.pits360.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.ues.dam.pits360.dtos.ApiResponse;
import com.ues.dam.pits360.entities.Adjunto;
import com.ues.dam.pits360.entities.OrdenAdjunto;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.AdjuntoServices;

@ExtendWith(MockitoExtension.class)
class AdjuntosControllersTest {

	@Mock
	private AdjuntoServices adjuntoServices;

	@InjectMocks
	private AdjuntosControllers adjuntosControllers;

	private Adjunto adjunto(String id) {
		return Adjunto.builder().adjuntoId(id).url("https://x").publicId("p").tipoArchivo("imagen")
				.formato("jpg").tamano(10L).nombreOriginal("a.jpg").esPrincipal(false).build();
	}

	@Test
	void listarPorVehiculoDevuelveLista() {
		when(adjuntoServices.listarPorVehiculo("v1")).thenReturn(List.of(adjunto("a1")));
		List<Adjunto> res = adjuntosControllers.listarPorVehiculo("v1");
		assertThat(res).hasSize(1);
		assertThat(res.get(0).getAdjuntoId()).isEqualTo("a1");
	}

	@Test
	void listarPorOrdenDevuelveLista() {
		when(adjuntoServices.listarPorOrden("o1")).thenReturn(List.of(new OrdenAdjunto()));
		List<OrdenAdjunto> res = adjuntosControllers.listarPorOrden("o1");
		assertThat(res).hasSize(1);
	}

	@Test
	void subirOkDevuelveAdjunto() {
		when(adjuntoServices.subir(any(), anyString(), any(), any(), any(), any())).thenReturn(adjunto("a1"));
		MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "x".getBytes());
		ResponseEntity<ApiResponse<Adjunto>> resp = adjuntosControllers.subir(file, "APP_LOGOS", null, null, null, null);
		assertThat(resp.getStatusCode().value()).isEqualTo(200);
		assertThat(resp.getBody()).isNotNull();
		assertThat(resp.getBody().getData().getAdjuntoId()).isEqualTo("a1");
		assertThat(resp.getBody().getMessage()).isEqualTo("Adjunto subido exitosamente");
	}

	@Test
	void subirSinFileLanza400() {
		when(adjuntoServices.subir(isNull(), anyString(), any(), any(), any(), any()))
				.thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "file requerido"));
		assertThatThrownBy(() -> adjuntosControllers.subir(null, "APP_LOGOS", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirSinCategoriaLanza400() {
		when(adjuntoServices.subir(any(), isNull(), any(), any(), any(), any()))
				.thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "categoria requerida"));
		MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "x".getBytes());
		assertThatThrownBy(() -> adjuntosControllers.subir(file, null, null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirCategoriaInvalidaLanza400() {
		when(adjuntoServices.subir(any(), eq("INVALIDA"), any(), any(), any(), any()))
				.thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "Categoria invalida"));
		MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "x".getBytes());
		assertThatThrownBy(() -> adjuntosControllers.subir(file, "INVALIDA", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirTipoNoPermitidoLanza400() {
		when(adjuntoServices.subir(any(), anyString(), any(), any(), any(), any()))
				.thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "Tipo de archivo no permitido"));
		MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());
		assertThatThrownBy(() -> adjuntosControllers.subir(file, "APP_LOGOS", null, null, null, null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirDescripcionMayor500Lanza400() {
		String larga = "a".repeat(501);
		when(adjuntoServices.subir(any(), anyString(), any(), any(), any(), eq(larga)))
				.thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "descripcion demasiado larga"));
		MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "x".getBytes());
		assertThatThrownBy(() -> adjuntosControllers.subir(file, "APP_LOGOS", null, null, null, larga))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirAmbosDuenosLanza400() {
		when(adjuntoServices.subir(any(), anyString(), any(), eq("v1"), eq("o1"), any()))
				.thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "no admite ambos"));
		MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "x".getBytes());
		assertThatThrownBy(() -> adjuntosControllers.subir(file, "APP_LOGOS", null, "v1", "o1", null))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void subirArchivoMayorLimiteLanza413() {
		when(adjuntoServices.subir(any(), anyString(), any(), any(), any(), any()))
				.thenThrow(new MaxUploadSizeExceededException(10L));
		MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "x".getBytes());
		assertThatThrownBy(() -> adjuntosControllers.subir(file, "APP_LOGOS", null, null, null, null))
				.isInstanceOf(MaxUploadSizeExceededException.class);
	}

	@Test
	void eliminarInexistenteLanza404() {
		when(adjuntoServices.eliminar("ghost")).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Adjunto no encontrado"));
		assertThatThrownBy(() -> adjuntosControllers.eliminar("ghost"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void eliminarOkDevuelveMapa() {
		when(adjuntoServices.eliminar("a1")).thenReturn(java.util.Map.of("adjuntoId", "a1", "eliminado", true));
		ResponseEntity<ApiResponse<java.util.Map<String, Object>>> resp = adjuntosControllers.eliminar("a1");
		assertThat(resp.getStatusCode().value()).isEqualTo(200);
		assertThat(resp.getBody()).isNotNull();
		assertThat(resp.getBody().getData().get("eliminado")).isEqualTo(true);
	}
}
