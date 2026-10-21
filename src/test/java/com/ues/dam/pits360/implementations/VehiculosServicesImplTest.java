package com.ues.dam.pits360.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.ues.dam.pits360.entities.Vehiculo;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.VehiculoRepository;

/**
 * Tests unitarios del CRUD de vehículos con Mockito puro (sin Spring ni BD).
 */
@ExtendWith(MockitoExtension.class)
class VehiculosServicesImplTest {

	@Mock
	private VehiculoRepository vehiculoRepository;

	@InjectMocks
	private VehiculosServicesImpl service;

	private Vehiculo vehiculo(String id) {
		return Vehiculo.builder().id(id).placa("ABC-123").marca("Toyota").modelo("Yaris").anio(2020).kilometraje(10).build();
	}

	@Test
	void getByIdInexistenteLanza404() {
		when(vehiculoRepository.findById("no-existe")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getById("no-existe"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void getByIdExistenteDevuelveLaEntidad() {
		Vehiculo guardado = vehiculo("id-1");
		when(vehiculoRepository.findById("id-1")).thenReturn(Optional.of(guardado));

		assertThat(service.getById("id-1")).isSameAs(guardado);
	}

	@Test
	void createAsignaUuidDistintoDelEnviado() {
		Vehiculo nuevo = vehiculo("id-enviado-por-cliente");
		when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

		Vehiculo saved = service.create(nuevo);

		assertThat(saved.getId()).isNotEqualTo("id-enviado-por-cliente");
		assertThat(saved.getId()).isNotBlank();
	}

	@Test
	void deleteInexistenteLanza404() {
		when(vehiculoRepository.findById("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete("ghost"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		verify(vehiculoRepository).findById("ghost");
	}
}
