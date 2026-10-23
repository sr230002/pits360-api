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
import com.ues.dam.integration.firebase.service.FirestoreVehiculosServices;
import com.ues.dam.pits360.entities.Vehiculo;
import com.ues.dam.pits360.exceptions.ApiException;

/**
 * Tests unitarios del CRUD de vehículos sobre Firestore: Mockito puro, sin
 * Spring ni Firestore real.
 */
class FirestoreVehiculosServicesTest {

	private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

	private FirestoreRepository<Vehiculo> repo;
	private FirestoreVehiculosServices service;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		repo = mock(FirestoreRepository.class);
		service = new FirestoreVehiculosServices(repo);
	}

	private Vehiculo vehiculo(String id) {
		return Vehiculo.builder().id(id).placa("ABC-123").marca("Toyota").modelo("Yaris").anio(2020).kilometraje(10000)
				.build();
	}

	@Test
	void createSinIdGeneraUuid() {
		Vehiculo nuevo = vehiculo(null);
		when(repo.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

		Vehiculo saved = service.create(nuevo);

		assertThat(saved.getId()).isNotNull().isNotBlank().matches(UUID_REGEX);
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
	void updateInexistenteLanza404() {
		when(repo.findById("no-existe")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.update("no-existe", vehiculo("no-existe")))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		verify(repo, never()).save(any(Vehiculo.class));
	}

	@Test
	void updateExistenteCopiaCamposYGuarda() {
		Vehiculo existing = vehiculo("id-1");
		existing.setPlaca("OLD-1");
		when(repo.findById("id-1")).thenReturn(Optional.of(existing));
		when(repo.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

		Vehiculo body = vehiculo("otro-id");
		body.setPlaca("NEW-1");

		Vehiculo saved = service.update("id-1", body);

		assertThat(saved.getId()).isEqualTo("id-1");
		assertThat(saved.getPlaca()).isEqualTo("NEW-1");
	}

	@Test
	void deleteInexistenteLanza404() {
		when(repo.findById("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete("ghost"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		verify(repo, never()).delete(anyString());
	}

	@Test
	void getAllDelegaEnFindAll() {
		List<Vehiculo> lista = List.of(vehiculo("id-1"));
		when(repo.findAll()).thenReturn(lista);

		assertThat(service.getAll()).isSameAs(lista);
	}

}
