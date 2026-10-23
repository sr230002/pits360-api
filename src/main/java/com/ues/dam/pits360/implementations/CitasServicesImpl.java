package com.ues.dam.pits360.implementations;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import com.ues.dam.pits360.entities.Cita;
import com.ues.dam.pits360.entities.Orden;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.CitaRepository;
import com.ues.dam.pits360.repositories.OrdenRepository;
import com.ues.dam.pits360.services.CitasServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del CRUD de citas.
 */
@Slf4j
@Service
@Profile("jpa")
@RequiredArgsConstructor
public class CitasServicesImpl implements CitasServices {

	private final CitaRepository citaRepository;
	private final OrdenRepository ordenRepository;

	private static final java.util.Set<String> ESTADOS_CITA =
			java.util.Set.of("PROGRAMADA", "CONFIRMADA", "CANCELADA", "CONVERTIDA");

	@Override
	@Transactional(readOnly = true)
	public List<Cita> getAll() {
		return getAll(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Cita> getAll(String estado) {
		if (estado == null || estado.isBlank()) {
			return citaRepository.findAll();
		}
		if (!ESTADOS_CITA.contains(estado)) {
			throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST,
					"Estado inválido: " + estado);
		}
		return citaRepository.findByEstado(estado);
	}

	@Override
	@Transactional(readOnly = true)
	public Cita getById(String id) {
		return findCita(id);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * El id lo genera siempre el servidor (ADR-8): el que venga en el body se
	 * sobreescribe con un UUID.
	 */
	@Override
	@Transactional
	public Cita create(Cita cita) {
		cita.setId(UUID.randomUUID().toString());
		return citaRepository.save(cita);
	}

	/** {@inheritDoc} */
	@Override
	@Transactional
	public Cita update(String id, Cita cita) {
		Cita existente = findCita(id);
		existente.setFecha(cita.getFecha());
		existente.setHora(cita.getHora());
		existente.setEstado(cita.getEstado());
		existente.setVehiculoId(cita.getVehiculoId());
		existente.setTallerId(cita.getTallerId());
		return citaRepository.save(existente);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Se carga la entidad gestionada y se borra con {@code delete}: dentro de la
	 * transacción es un único {@code DELETE} (frente a {@code existsById} +
	 * {@code deleteById}, que serían dos consultas).
	 */
	@Override
	@Transactional
	public void delete(String id) {
		Cita cita = findCita(id);
		citaRepository.delete(cita);
		log.debug("Cita eliminada: id={}", id);
	}

	@Override
	@Transactional
	public Cita aprobar(String id) {
		Cita cita = findCita(id);
		if (!"PROGRAMADA".equals(cita.getEstado())) {
			throw new ApiException(HttpStatus.CONFLICT, "La cita no está en estado PROGRAMADA");
		}
		cita.setEstado("CONFIRMADA");
		return citaRepository.save(cita);
	}

	@Override
	@Transactional
	public Cita rechazar(String id) {
		Cita cita = findCita(id);
		if (!"PROGRAMADA".equals(cita.getEstado())) {
			throw new ApiException(HttpStatus.CONFLICT, "La cita no está en estado PROGRAMADA");
		}
		cita.setEstado("CANCELADA");
		return citaRepository.save(cita);
	}

	@Override
	@Transactional
	public Orden convertir(String citaId, Orden datos) {
		Cita cita = findCita(citaId);
		if ("CONVERTIDA".equals(cita.getEstado()) || "CANCELADA".equals(cita.getEstado())) {
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
		Orden orden = new Orden();
		orden.setId(UUID.randomUUID().toString());
		String numero = datos.getNumero();
		if (numero == null || numero.isBlank()) {
			long n = ordenRepository.count() + 1;
			numero = String.format("ORD-%d-%04d", LocalDate.now().getYear(), n);
		}
		orden.setNumero(numero);
		orden.setEstado("RECIBIDO");
		orden.setVehiculoId(cita.getVehiculoId());
		orden.setTallerId(cita.getTallerId());
		orden.setFecha(cita.getFecha());
		Double total = datos.getTotal();
		orden.setTotal(total != null ? total : 0.0);
		orden.setTecnicoId(datos.getTecnicoId());
		orden.setCitaId(citaId);
		orden.setNota(datos.getNota());
		orden.setTrabajos(datos.getTrabajos());
		orden.setMetodoPago(datos.getMetodoPago());
		Integer avance = datos.getAvance();
		orden.setAvance(avance != null ? avance : 0);
		Orden saved = ordenRepository.save(orden);
		cita.setEstado("CONVERTIDA");
		citaRepository.save(cita);
		return saved;
	}

	/** Busca la entidad o lanza 404. Usa la caché de la transacción en curso. */
	private Cita findCita(String id) {
		return citaRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
						"Cita no encontrada: " + id));
	}
}
