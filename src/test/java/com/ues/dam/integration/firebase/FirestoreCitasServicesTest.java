package com.ues.dam.integration.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.integration.firebase.service.FirestoreCitasServices;
import com.ues.dam.pits360.entities.Cita;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.exceptions.ApiException;

/**
 * Tests del servicio de citas sobre Firestore con Mockito puro (sin emulador).
 */
@ExtendWith(MockitoExtension.class)
class FirestoreCitasServicesTest {

	@Mock
	private FirestoreRepository<Cita> citas;

	@Mock
	private FirestoreRepository<Orden> ordenes;

	private FirestoreCitasServices service;

	@BeforeEach
	void setUp() {
		service = new FirestoreCitasServices(citas, ordenes);
	}

	@Test
	void aprobarSobreCitaNoProgramadaLanza409() {
		when(citas.findById("cita-1")).thenReturn(Optional.of(cita("PROGRAMADA").estado("FINALIZADA").build()));

		assertThatThrownBy(() -> service.aprobar("cita-1"))
				.isInstanceOf(ApiException.class)
				.satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
		verify(citas, never()).save(any(Cita.class));
	}

	@Test
	void aprobarSobreCitaProgramadaGuardaAprobada() {
		Cita cita = cita("PROGRAMADA").build();
		when(citas.findById("cita-1")).thenReturn(Optional.of(cita));
		when(citas.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

		Cita guardada = service.aprobar("cita-1");

		assertThat(guardada.getEstado()).isEqualTo("APROBADA");
	}

	@Test
	void convertirCreaOrdenRecibidaYDejaLaCitaConvertida() {
		Cita cita = cita("PROGRAMADA").build();
		when(citas.findById("cita-1")).thenReturn(Optional.of(cita));
		when(ordenes.findAll()).thenReturn(List.of());
		when(ordenes.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));
		when(citas.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

		Orden orden = service.convertir("cita-1", Orden.builder().total(120.0).nota("Cambio de aceite").build());

		assertThat(orden.getEstado()).isEqualTo("RECIBIDA");
		assertThat(orden.getNumero()).startsWith("ORD-");
		assertThat(orden.getCitaId()).isEqualTo("cita-1");
		assertThat(orden.getVehiculoId()).isEqualTo("veh-1");
		assertThat(orden.getTotal()).isEqualTo(120.0);

		ArgumentCaptor<Cita> guardada = ArgumentCaptor.forClass(Cita.class);
		verify(citas).save(guardada.capture());
		assertThat(guardada.getValue().getEstado()).isEqualTo("CONVERTIDA");
	}

	@Test
	void getAllPorEstadoFiltraConFindBy() {
		Cita cita = cita("PENDIENTE").build();
		when(citas.findBy("estado", "PENDIENTE")).thenReturn(List.of(cita));

		assertThat(service.getAll("PENDIENTE")).containsExactly(cita);
		verify(citas).findBy("estado", "PENDIENTE");
		verify(citas, never()).findAll();
	}

	@Test
	void getAllSinEstadoTraeTodas() {
		when(citas.findAll()).thenReturn(List.of(cita("PROGRAMADA").build()));

		assertThat(service.getAll(null)).hasSize(1);
		verify(citas).findAll();
	}

	private Cita.CitaBuilder cita(String estado) {
		return Cita.builder()
				.id("cita-1")
				.fecha("2026-10-21")
				.hora("10:30")
				.estado(estado)
				.vehiculoId("veh-1")
				.tallerId("taller-1");
	}

}
