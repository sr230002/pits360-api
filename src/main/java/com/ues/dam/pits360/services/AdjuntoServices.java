package com.ues.dam.pits360.services;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.ues.dam.pits360.entities.Adjunto;
import com.ues.dam.pits360.entities.OrdenAdjunto;

/**
 * Contrato del recurso {@code /adjuntos}: tabla central de adjuntos en
 * Cloudinary con taxonomía de carpetas {@code pits360/...}.
 *
 * <p>
 * ADR-10: sin capa DTO; las entidades {@link Adjunto} y {@link OrdenAdjunto}
 * entran y salen tal cual. Los fallos se señalan con
 * {@link com.ues.dam.pits360.exceptions.ApiException}.
 */
public interface AdjuntoServices {

	/**
	 * Sube un archivo a la categoría indicada, persiste el {@link Adjunto} y
	 * devuelto guardado. La carpeta la compone el servidor con
	 * {@link com.ues.dam.pits360.utilities.AdjuntoCategoria#construirRuta};
	 * el cliente nunca manda una ruta.
	 *
	 * @throws com.ues.dam.pits360.exceptions.ApiException 400 si el archivo no
	 *         cumple los límites de negocio (5 MB, tipos permitidos, ids que no
	 *         cuadran con la categoría) o si la orden ya tiene 20 adjuntos
	 * @throws com.ues.dam.pits360.exceptions.ApiException 502 si Cloudinary falla
	 */
	Adjunto subir(MultipartFile file, String categoria, String usuarioId,
			String vehiculoId, String ordenId, String descripcion);

	/** Adjuntos de un vehículo (máximo 1 en reemplazo). */
	List<Adjunto> listarPorVehiculo(String vehiculoId);

	/** Filas de la pivote de una orden con su adjunto cargado en memoria. */
	List<OrdenAdjunto> listarPorOrden(String ordenId);

	/**
	 * Borra el adjunto: fila + pivote + recurso en Cloudinary, @Transactional.
	 *
	 * @return un mapa con {@code adjuntoId} y {@code eliminado = true}
	 * @throws com.ues.dam.pits360.exceptions.ApiException 404 si no existe
	 */
	Map<String, Object> eliminar(String adjuntoId);
}
