package com.ues.dam.integration.firebase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ues.dam.integration.firebase.repository.DocumentMapper;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.integration.firebase.seed.FirestoreDataSeeder;
import com.ues.dam.pits360.entities.Cita;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.entities.Servicio;
import com.ues.dam.pits360.entities.Taller;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.entities.Vehiculo;

/**
 * Idempotencia del seed de Firestore: con usuarios sembra todas las
 * colecciones, con usuarios existentes no escribe nada. Solo se mockean los
 * repositorios: sin Firestore, sin Spring.
 */
@ExtendWith(MockitoExtension.class)
class FirestoreSeederTest {

	private static final String HASH_DEMO = "$2a$10$hashDemoPits360";

	@Mock
	private FirestoreRepository<Usuario> usuarios;
	@Mock
	private FirestoreRepository<Taller> talleres;
	@Mock
	private FirestoreRepository<Servicio> servicios;
	@Mock
	private FirestoreRepository<Vehiculo> vehiculos;
	@Mock
	private FirestoreRepository<Cita> citas;
	@Mock
	private FirestoreRepository<Orden> ordenes;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private ApplicationArguments args;

	@Test
	void siembraTodasLasColeccionesCuandoNoHayUsuarios() {
		when(usuarios.findAll()).thenReturn(List.of());
		when(passwordEncoder.encode("pits3601")).thenReturn(HASH_DEMO);

		seeder().run(args);

		verify(usuarios, times(3)).save(any(Usuario.class));
		verify(talleres, times(2)).save(any(Taller.class));
		verify(servicios, times(4)).save(any(Servicio.class));
		verify(vehiculos, times(2)).save(any(Vehiculo.class));
		verify(citas, times(2)).save(any(Cita.class));
		verify(ordenes, times(2)).save(any(Orden.class));
	}

	@Test
	void noSiembraNadaCuandoYaHayUsuarios() {
		when(usuarios.findAll())
				.thenReturn(List.of(Usuario.builder().id("u-existente").email("cliente@pits360.com").build()));

		seeder().run(args);

		verify(usuarios, never()).save(any(Usuario.class));
		verify(talleres, never()).save(any(Taller.class));
		verify(servicios, never()).save(any(Servicio.class));
		verify(vehiculos, never()).save(any(Vehiculo.class));
		verify(citas, never()).save(any(Cita.class));
		verify(ordenes, never()).save(any(Orden.class));
		verify(passwordEncoder, never()).encode(anyString());
	}

	@Test
	void siembraUsuariosConHashRolesEIdsFijos() {
		when(usuarios.findAll()).thenReturn(List.of());
		when(passwordEncoder.encode("pits3601")).thenReturn(HASH_DEMO);

		seeder().run(args);

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarios, times(3)).save(captor.capture());
		List<Usuario> guardados = captor.getAllValues();

		assertThat(guardados).extracting(Usuario::getEmail).containsExactlyInAnyOrder("cliente@pits360.com",
				"tecnico@pits360.com", "admin@pits360.com");
		assertThat(guardados).extracting(Usuario::getRol).containsExactlyInAnyOrder("cliente", "tecnico", "admin");
		assertThat(guardados).allSatisfy(u -> assertThat(u.getPasswordHash()).isEqualTo(HASH_DEMO));
		assertThat(guardados).extracting(Usuario::getId).doesNotHaveDuplicates().allSatisfy(id -> assertThat(id)
				.matches("[0-9a-f-]{36}"));
	}

	@Test
	void siembraTalleresConCoordenadasYDocumentoConCampoId() {
		when(usuarios.findAll()).thenReturn(List.of());
		when(passwordEncoder.encode("pits3601")).thenReturn(HASH_DEMO);

		seeder().run(args);

		ArgumentCaptor<Taller> captor = ArgumentCaptor.forClass(Taller.class);
		verify(talleres, times(2)).save(captor.capture());
		List<Taller> guardados = captor.getAllValues();

		assertThat(guardados).allSatisfy(t -> {
			assertThat(t.getLatitud()).isNotNull();
			assertThat(t.getLongitud()).isNotNull();
			Map<String, Object> documento = new DocumentMapper().toMap(t);
			assertThat(documento).containsEntry("id", t.getId());
		});
	}

	private FirestoreDataSeeder seeder() {
		return new FirestoreDataSeeder(usuarios, talleres, servicios, vehiculos, citas, ordenes, passwordEncoder);
	}

}
