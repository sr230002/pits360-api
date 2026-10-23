package com.ues.dam.integration.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.integration.firebase.service.FirestoreServiciosServices;
import com.ues.dam.pits360.entities.Servicio;
import com.ues.dam.pits360.exceptions.ApiException;

/**
 * CRUD de servicios sobre Firestore. Solo se mockea el repositorio:
 * sin Firestore, sin Spring.
 */
@ExtendWith(MockitoExtension.class)
class FirestoreServiciosServicesTest {

	@Mock
	private FirestoreRepository<Servicio> repository;

	@InjectMocks
	private FirestoreServiciosServices service;

	@Test
	void getByIdDeIdInexistenteLanza404() {
		when(repository.findById("no-existe")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getById("no-existe"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> {
					ApiException api = (ApiException) e;
					assertThat(api.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
					assertThat(api.getMessage()).isEqualTo("Servicio no encontrado: no-existe");
				});
	}

	@Test
	void createIgnoraElIdDelBodyYDevuelveElServicioGuardado() {
		when(repository.save(any(Servicio.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Servicio creado = service.create(Servicio.builder()
				.id("id-del-body")
				.nombre("Cambio de aceite")
				.descripcion("Incluye filtro")
				.precio(25.0)
				.duracion(60)
				.build());

		assertThat(creado.getId()).isNotBlank().isNotEqualTo("id-del-body");
		assertThat(creado.getNombre()).isEqualTo("Cambio de aceite");
	}

	@Test
	void getAllDevuelveLaListaDelRepositorio() {
		List<Servicio> servicios = List.of(servicio("s1"), servicio("s2"));
		when(repository.findAll()).thenReturn(servicios);

		assertThat(service.getAll()).isEqualTo(servicios);
	}

	@Test
	void updateDeIdInexistenteLanza404() {
		when(repository.findById("no-existe")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.update("no-existe", servicio("s1")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void deleteDeIdInexistenteLanza404() {
		when(repository.findById("no-existe")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete("no-existe"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	private Servicio servicio(String id) {
		return Servicio.builder()
				.id(id)
				.nombre("Cambio de aceite")
				.descripcion("Incluye filtro")
				.precio(25.0)
				.duracion(60)
				.build();
	}
}
