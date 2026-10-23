package com.ues.dam.pits360.implementations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ues.dam.pits360.entities.Adjunto;
import com.ues.dam.pits360.entities.OrdenAdjunto;
import com.ues.dam.pits360.entities.Usuario;
import com.ues.dam.pits360.exceptions.ApiException;
import com.ues.dam.pits360.repositories.AdjuntoRepository;
import com.ues.dam.pits360.repositories.OrdenAdjuntoRepository;
import com.ues.dam.pits360.repositories.OrdenRepository;
import com.ues.dam.pits360.repositories.UsuarioRepository;
import com.ues.dam.pits360.repositories.VehiculoRepository;
import com.ues.dam.pits360.services.AdjuntoServices;
import com.ues.dam.pits360.utilities.AdjuntoCategoria;
import com.ues.dam.pits360.utilities.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de adjuntos: subida, listado y borrado contra Cloudinary y
 * la tabla central {@code adjunto} (con pivote {@code orden_adjunto}).
 *
 * <p>
 * Reglas de blindaje del free-tier: 5 MB máximo por archivo, tipos
 * {@code image/jpeg|png|webp} y {@code application/pdf} únicamente, máximo 20
 * adjuntos por orden y una sola foto por vehículo (el reemplazo sube el nuevo
 * primero y destruye el viejo después, para no perder la foto si algo falla
 * a mitad de camino).
 */
@Slf4j
@Service
@Profile("jpa")
@RequiredArgsConstructor
public class AdjuntoServicesImpl implements AdjuntoServices {

	private static final long MAX_TAMANO_BYTES = 5L * 1024 * 1024;
	private static final int MAX_ADJUNTOS_POR_ORDEN = 20;
	private static final Set<String> CONTENT_TYPES_PERMITIDOS =
			Set.of("image/jpeg", "image/png", "image/webp", "application/pdf");

	private final AdjuntoRepository adjuntoRepository;
	private final OrdenAdjuntoRepository ordenAdjuntoRepository;
	private final CloudinaryService cloudinaryService;
	private final UsuarioRepository usuarioRepository;
	private final VehiculoRepository vehiculoRepository;
	private final OrdenRepository ordenRepository;

	@Override
	@Transactional
	public Adjunto subir(MultipartFile file, String categoria, String usuarioId,
			String vehiculoId, String ordenId, String descripcion) {
		AdjuntoCategoria cat = AdjuntoCategoria.from(categoria);
		String tipoArchivo = validarArchivo(file);

		// Solo las categorías con {usuarioId} en la carpeta resuelven usuario;
		// el id siempre sale del usuario autenticado (cierra el IDOR).
		if (cat.requiereUsuarioId()) {
			usuarioId = resolverUsuarioId(usuarioId);
		}
		// Referencias existentes antes de subir: no se crean adjuntos colgados
		// de un vehiculoId o un ordenId que no existan.
		if (cat.requiereVehiculoId()) {
			vehiculoRepository.findById(vehiculoId.trim())
					.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
							"Vehículo no encontrado: " + vehiculoId));
		}
		if (cat.requiereOrdenId()) {
			ordenRepository.findById(ordenId.trim())
					.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
							"Orden no encontrada: " + ordenId));
		}

		// El límite de 20 por orden se comprueba antes de subir: fuera del
		// free-tier del plan, nada de destruir un recién subido por una regla
		// de negocio que se podía validar sin costo.
		if (cat.requiereOrdenId()
				&& ordenAdjuntoRepository.countByOrdenId(ordenId) >= MAX_ADJUNTOS_POR_ORDEN) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"La orden ya tiene el máximo de " + MAX_ADJUNTOS_POR_ORDEN + " adjuntos.");
		}

		String carpeta = cat.construirRuta(usuarioId, vehiculoId, ordenId);
		Map<String, Object> resultado = cloudinaryService.upload(file, carpeta);

		Adjunto adjunto = Adjunto.builder()
				.adjuntoId(UUID.randomUUID().toString())
				.url(valor(resultado.get("secure_url"), "La URL de Cloudinary no llegó"))
				.publicId(valor(resultado.get("public_id"), "El public_id de Cloudinary no llegó"))
				.tipoArchivo(tipoArchivo)
				.formato(valor(resultado.get("format"), "otro"))
				.tamano(tamanoComoLong(resultado.get("bytes")))
				.nombreOriginal(nombreOriginal(resultado, file))
				.vehiculoId(cat.requiereVehiculoId() ? vehiculoId.trim() : null)
				.ordenId(cat.requiereOrdenId() ? ordenId.trim() : null)
				.esPrincipal(false)
				.descripcion(descripcion)
				.build();

		// Si la asignación falla después de subir, se destruye el recién
		// subido para no dejar recursos huérfanos (sin cron: se limpia ahora).
		try {
			Adjunto guardado = adjuntoRepository.save(adjunto);
			if (cat.requiereVehiculoId()) {
				reemplazarFotoVehiculo(guardado);
			}
			if (cat.requiereOrdenId()) {
				crearFilaPivote(guardado, descripcion);
			}
			log.debug("Adjunto subido: id={} carpeta={} public_id={}", guardado.getAdjuntoId(),
					carpeta, guardado.getPublicId());
			return guardado;
		} catch (RuntimeException e) {
			log.error("Fallo al asignar el adjunto {} tras subirlo; se destruye el recién subido.",
					adjunto.getAdjuntoId());
			try {
				cloudinaryService.delete(adjunto.getPublicId(), tipoArchivo);
			} catch (RuntimeException ex) {
				log.error("Además falló el destroy de compensación: {}", ex.getMessage());
			}
			throw e;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Adjunto> listarPorVehiculo(String vehiculoId) {
		return adjuntoRepository.findByVehiculoId(vehiculoId);
	}

	/**
	 * Filas de la pivote con su adjunto cargado en memoria (una sola consulta
	 * con {@code findAllById}, sin N+1): sin {@code @ManyToOne} en la entidad,
	 * este es el "join a mano" de la lectura.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<OrdenAdjunto> listarPorOrden(String ordenId) {
		List<OrdenAdjunto> filas = ordenAdjuntoRepository.findByOrdenId(ordenId);
		if (filas.isEmpty()) {
			return filas;
		}
		Map<String, Adjunto> porId = adjuntoRepository
				.findAllById(filas.stream().map(OrdenAdjunto::getAdjuntoId).collect(Collectors.toSet()))
				.stream().collect(Collectors.toMap(Adjunto::getAdjuntoId, Function.identity()));
		filas.forEach(fila -> fila.setAdjunto(porId.get(fila.getAdjuntoId())));
		return filas;
	}

	@Override
	@Transactional
	public Map<String, Object> eliminar(String adjuntoId) {
		Adjunto adjunto = adjuntoRepository.findById(adjuntoId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Adjunto no encontrado: " + adjuntoId));
		limpiarPivote(adjuntoId);
		adjuntoRepository.delete(adjunto);
		// Cloudinary va al final: si la BD falla el row queda y se reintenta;
		// si Cloudinary falla el row ya no existe y el recurso muere solo en
		// el free-tier de 25k imágenes mensuales.
		cloudinaryService.delete(adjunto.getPublicId(), adjunto.getTipoArchivo());
		return Map.of("adjuntoId", adjuntoId, "eliminado", true);
	}

	/**
	 * Resuelve el id del usuario autenticado: el principal que dejó el
	 * {@code JwtFilter} es el email, y se traduce con {@code UsuarioRepository}
	 * al id real. Si el cliente manda {@code usuarioId}, debe ser el suyo
	 * (403 si no); si no lo manda, se resuelve server-side. Sin autenticación
	 * no hay carpeta de usuario: 401, nunca 500.
	 */
	private String resolverUsuarioId(String usuarioId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof String email)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED,
					"No autenticado: envía un token Bearer válido");
		}
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
						"No autenticado: envía un token Bearer válido"));
		if (usuarioId != null && !usuarioId.isBlank() && !usuario.getId().equals(usuarioId.trim())) {
			throw new ApiException(HttpStatus.FORBIDDEN,
					"No puedes subir archivos a la carpeta de otro usuario.");
		}
		return usuario.getId();
	}

	/** Tamaño ≤ 5 MB y tipos permitidos; {@code tipoArchivo} sale del contentType. */
	private String validarArchivo(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "El archivo es obligatorio.");
		}
		if (file.getSize() > MAX_TAMANO_BYTES) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"El archivo supera el máximo de 5 MB.");
		}
		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
		if (!CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"Tipo de archivo no permitido. Solo image/jpeg, image/png, image/webp y application/pdf.");
		}
		return contentType.startsWith("image/") ? "imagen" : "pdf";
	}

	/**
	 * 1 foto por vehículo: tras guardar el nuevo, borra los previos de ese
	 * mismo vehículo (fila + pivote) y destruye su binario en Cloudinary
	 * DESPUÉS de guardar el nuevo, para no dejar al vehículo sin foto.
	 */
	private void reemplazarFotoVehiculo(Adjunto nuevo) {
		adjuntoRepository.findByVehiculoId(nuevo.getVehiculoId()).stream()
				.filter(previo -> !previo.getAdjuntoId().equals(nuevo.getAdjuntoId()))
				.forEach(previo -> {
					limpiarPivote(previo.getAdjuntoId());
					adjuntoRepository.delete(previo);
					cloudinaryService.delete(previo.getPublicId(), previo.getTipoArchivo());
				});
	}

	private void crearFilaPivote(Adjunto adjunto, String descripcion) {
		OrdenAdjunto pivote = OrdenAdjunto.builder()
				.ordenAdjuntoId(UUID.randomUUID().toString())
				.ordenId(adjunto.getOrdenId())
				.adjuntoId(adjunto.getAdjuntoId())
				.descripcion(descripcion)
				.visible(true)
				.esPrincipal(false)
				.build();
		ordenAdjuntoRepository.save(pivote);
	}

	/** Borra las filas de pivote de un adjunto antes de borrar al adjunto. */
	private void limpiarPivote(String adjuntoId) {
		ordenAdjuntoRepository.deleteByAdjuntoId(adjuntoId);
	}

	private static String valor(Object valor, String mensajeSiNull) {
		return valor != null ? valor.toString() : mensajeSiNull;
	}

	private static Long tamanoComoLong(Object bytes) {
		return bytes instanceof Number numero ? numero.longValue() : 0L;
	}

	private static String nombreOriginal(Map<String, Object> resultado, MultipartFile file) {
		String nombre = (String) resultado.get("original_filename");
		return nombre != null ? nombre : file.getOriginalFilename();
	}
}
