package com.ues.dam.integration.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.integration.firebase.service.FirestoreOrdenesServices;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.exceptions.ApiException;

/**
 * Órdenes sobre Firestore con el repositorio simulado: sin Firestore, sin
 * Spring.
 */
@ExtendWith(MockitoExtension.class)
class FirestoreOrdenesServicesTest {

	@Mock
	private FirestoreRepository<Orden> repository;

	private FirestoreOrdenesServices service;

	@BeforeEach
	void setUp() {
		service = new FirestoreOrdenesServices(repository);
	}

	@Test
	void getByTecnicoFiltraPorTecnico() {
		Orden orden = Orden.builder().id("o1").tecnicoId("t1").build();
		when(repository.findBy("tecnicoId", "t1")).thenReturn(List.of(orden));

		List<Orden> resultado = service.getByTecnico("t1");

		assertThat(resultado).containsExactly(orden);
		verify(repository).findBy("tecnicoId", "t1");
	}

	@Test
	void getByVehiculoFiltraPorVehiculo() {
		Orden orden = Orden.builder().id("o1").vehiculoId("v1").build();
		when(repository.findBy("vehiculoId", "v1")).thenReturn(List.of(orden));

		List<Orden> resultado = service.getByVehiculo("v1");

		assertThat(resultado).containsExactly(orden);
		verify(repository).findBy("vehiculoId", "v1");
	}

	@Test
	void deleteDeOrdenInexistenteLanza404() {
		when(repository.findById("nope")).thenReturn(Optional.empty());

		Throwable error = catchThrowable(() -> service.delete("nope"));

		assertThat(error).isInstanceOf(ApiException.class).hasMessage("Orden no encontrada: nope");
		assertThat(((ApiException) error).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void createDevuelveLaOrdenGuardada() {
		Orden orden = Orden.builder().numero("ORD-1").build();
		when(repository.save(any(Orden.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		Orden resultado = service.create(orden);

		assertThat(resultado).isSameAs(orden);
		assertThat(resultado.getId()).isNotBlank();
		verify(repository).save(orden);
	}

}
