package com.ues.dam.integration.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.integration.firebase.service.FirestoreTalleresServices;
import com.ues.dam.pits360.entities.Taller;
import com.ues.dam.pits360.exceptions.ApiException;

/**
 * Tests unitarios del CRUD de talleres sobre Firestore: Mockito puro, sin
 * Spring ni Firestore real.
 */
class FirestoreTalleresServicesTest {

	private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

	private FirestoreRepository<Taller> repo;
	private FirestoreTalleresServices service;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		repo = mock(FirestoreRepository.class);
		service = new FirestoreTalleresServices(repo);
	}

	private Taller taller(String id, Double latitud, Double longitud) {
		return Taller.builder().id(id).nombre("Autocentro Pits").direccion("Calle 1").telefono("2222-3333").rating(4.5)
				.latitud(latitud).longitud(longitud).build();
	}

	@Test
	void createGeneraUuidYPersisteCoordenadas() {
		Taller nuevo = taller(null, 13.69, -89.19);
		when(repo.save(any(Taller.class))).thenAnswer(inv -> inv.getArgument(0));

		Taller saved = service.create(nuevo);

		assertThat(saved.getId()).isNotNull().isNotBlank().matches(UUID_REGEX);
		assertThat(saved.getLatitud()).isEqualTo(13.69);
		assertThat(saved.getLongitud()).isEqualTo(-89.19);
		verify(repo).save(saved);
	}

	@Test
	void getByIdInexistenteLanza404() {
		when(repo.findById("no-existe")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getById("no-existe"))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("no-existe")
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void getAllDelegaEnFindAll() {
		List<Taller> lista = List.of(taller("id-1", 13.69, -89.19));
		when(repo.findAll()).thenReturn(lista);

		assertThat(service.getAll()).isSameAs(lista);
	}

	@Test
	void updateInexistenteLanza404() {
		when(repo.findById("no-existe")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.update("no-existe", taller("no-existe", 1.0, 2.0)))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		verify(repo, never()).save(any(Taller.class));
	}

	@Test
	void updateCopiaCoordenadasYGuarda() {
		Taller existing = taller("id-1", null, null);
		when(repo.findById("id-1")).thenReturn(Optional.of(existing));
		when(repo.save(any(Taller.class))).thenAnswer(inv -> inv.getArgument(0));

		Taller saved = service.update("id-1", taller("otro-id", 13.69, -89.19));

		assertThat(saved.getId()).isEqualTo("id-1");
		assertThat(saved.getLatitud()).isEqualTo(13.69);
		assertThat(saved.getLongitud()).isEqualTo(-89.19);
	}

	@Test
	void deleteInexistenteLanza404() {
		when(repo.findById("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete("ghost"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		verify(repo, never()).delete(anyString());
	}

}
