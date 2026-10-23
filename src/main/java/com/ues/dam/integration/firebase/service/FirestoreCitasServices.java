package com.ues.dam.integration.firebase.service;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.ues.dam.integration.firebase.config.FirestoreCollections;
import com.ues.dam.integration.firebase.repository.FirestoreRepository;
import com.ues.dam.pits360.entities.Cita;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.services.CitasServices;

/**
 * Citas sobre Firestore (perfil sin JPA).
 *
 * <p>
 * {@link FirestoreRepository} no es un bean: cada servicio construye el suyo
 * con el {@link Firestore} inyectado, apuntando a su colección. Por eso el
 * constructor de Spring es explícito (no cabe {@code @RequiredArgsConstructor})
 * y hay un segundo constructor que recibe los repositorios ya construidos,
 * usado por los tests.
 */
@Service
@Profile("!jpa")
public class FirestoreCitasServices implements CitasServices {

	private static final String ESTADO_PROGRAMADA = "PROGRAMADA";
	private static final String ESTADO_APROBADA = "APROBADA";
	private static final String ESTADO_RECHAZADA = "RECHAZADA";
	private static final String ESTADO_CANCELADA = "CANCELADA";
	private static final String ESTADO_CONVERTIDA = "CONVERTIDA";
	private static final String ESTADO_ORDEN_RECIBIDA = "RECIBIDA";

	private final FirestoreRepository<Cita> citas;
	private final FirestoreRepository<Orden> ordenes;

	/** Constructor de Spring: arma un repositorio por colección. */
	@Autowired
	public FirestoreCitasServices(Firestore firestore) {
		this(new FirestoreRepository<>(firestore, FirestoreCollections.CITAS, Cita.class, Cita::getId, Cita::setId),
				new FirestoreRepository<>(firestore, FirestoreCollections.ORDENES, Orden.class, Orden::getId,
						Orden::setId));
	}

	/** Constructor para tests: repositorios ya construidos. */
	public FirestoreCitasServices(FirestoreRepository<Cita> citas, FirestoreRepository<Orden> ordenes) {
		this.citas = citas;
		this.ordenes = ordenes;
	}

	@Override
	public List<Cita> getAll(String estado) {
		if (estado == null || estado.isBlank()) {
			return citas.findAll();
		}
		return citas.findBy("estado", estado);
	}

	@Override
	public Cita getById(String id) {
		return findCita(id);
	}

	/** El id lo genera siempre el servidor (ADR-8). */
	@Override
	public Cita create(Cita cita) {
		cita.setId(UUID.randomUUID().toString());
		return citas.save(cita);
	}

	@Override
	public Cita update(String id, Cita cita) {
		Cita existente = findCita(id);
		existente.setFecha(cita.getFecha());
		existente.setHora(cita.getHora());
		existente.setEstado(cita.getEstado());
		existente.setVehiculoId(cita.getVehiculoId());
		existente.setTallerId(cita.getTallerId());
		return citas.save(existente);
	}

	@Override
	public void delete(String id) {
		findCita(id);
		citas.delete(id);
	}

	@Override
	public Cita aprobar(String id) {
		return cambiarEstado(id, ESTADO_APROBADA);
	}

	@Override
	public Cita rechazar(String id) {
		return cambiarEstado(id, ESTADO_RECHAZADA);
	}

	/** Solo una cita PROGRAMADA puede cambiar de estado. */
	private Cita cambiarEstado(String id, String nuevoEstado) {
		Cita cita = findCita(id);
		if (!ESTADO_PROGRAMADA.equalsIgnoreCase(cita.getEstado())) {
			throw new ApiException(HttpStatus.CONFLICT, "La cita no está en estado PROGRAMADA");
		}
		cita.setEstado(nuevoEstado);
		return citas.save(cita);
	}

	@Override
	public Orden convertir(String citaId, Orden datos) {
		Cita cita = findCita(citaId);
		if (ESTADO_CONVERTIDA.equals(cita.getEstado()) || ESTADO_CANCELADA.equals(cita.getEstado())
				|| ESTADO_RECHAZADA.equals(cita.getEstado())) {
			throw new ApiException(HttpStatus.CONFLICT, "La cita ya está " + cita.getEstado());
		}
		if (datos == null) {
			datos = new Orden();
		}
		if (datos.getTotal() != null && datos.getTotal() < 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "total no puede ser negativo");
		}
		if (datos.getAvance() != null && (datos.getAvance() < 0 || datos.getAvance() > 100)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "avance debe estar entre 0 y 100");
		}
		Orden orden = Orden.builder()
				.id(UUID.randomUUID().toString())
				.numero(siguienteNumero())
				.estado(ESTADO_ORDEN_RECIBIDA)
				.vehiculoId(datos.getVehiculoId() != null ? datos.getVehiculoId() : cita.getVehiculoId())
				.tallerId(datos.getTallerId() != null ? datos.getTallerId() : cita.getTallerId())
				.fecha(cita.getFecha())
				.total(datos.getTotal() != null ? datos.getTotal() : 0.0)
				.avance(datos.getAvance() != null ? datos.getAvance() : 0)
				.tecnicoId(datos.getTecnicoId())
				.citaId(citaId)
				.nota(datos.getNota())
				.trabajos(datos.getTrabajos())
				.metodoPago(datos.getMetodoPago())
				.build();
		Orden saved = ordenes.save(orden);
		cita.setEstado(ESTADO_CONVERTIDA);
		citas.save(cita);
		return saved;
	}

	/**
	 * Numero correlativo del año: {@code ORD-<año>-<n>} con {@code n} de tres
	 * digitos. Cuenta las ordenes del año en curso (las de la serie del numero
	 * y las fechadas este año).
	 */
	private String siguienteNumero() {
		int anio = Year.now().getValue();
		String prefijo = "ORD-" + anio + "-";
		long n = ordenes.findAll().stream().filter(o -> esDelAnio(o, anio, prefijo)).count() + 1;
		return String.format("%s%03d", prefijo, n);
	}

	private boolean esDelAnio(Orden orden, int anio, String prefijo) {
		String numero = orden.getNumero();
		if (numero != null && numero.startsWith(prefijo)) {
			return true;
		}
		String fecha = orden.getFecha();
		return fecha != null && fecha.startsWith(String.valueOf(anio));
	}

	/** Busca la cita o lanza 404. */
	private Cita findCita(String id) {
		return citas.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada: " + id));
	}

}
