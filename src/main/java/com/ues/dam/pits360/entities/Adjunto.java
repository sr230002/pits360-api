package com.ues.dam.pits360.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Adjunto subido a Cloudinary, mapea la tabla central {@code adjunto}.
 * Tabla central única: todo el binario (app, perfil, foto de vehículo,
 * evidencia de orden, documento) vive aquí con su {@code public_id} y su
 * {@code url} segura; las vinculaciones con cada entidad van por columnas
 * sueltas de id ({@code vehiculoId}, {@code ordenId}) o por la pivote
 * {@code orden_adjunto}, sin FK DDL ni {@code @ManyToOne} (convención del
 * proyecto, entidades/Orden.java:20-23).
 *
 * <p>
 * Longitudes más anchas que un String(255) a secas a propósito: la
 * {@code secure_url} real de Cloudinary (cloud + carpeta + hash + version)
 * puede superar 255 caracteres y el {@code public_id} con carpetas puede
 * superar 100; si la columna fuera corta, Postgres respondería
 * "value too long for varying" y el upload fallaría con un 500 tras haberse
 * subido el binario. Igual con {@code nombreOriginal} (255) por nombres con
 * ruta corta incluida.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "adjunto")
public class Adjunto {

	/**
	 * UUID String(36); el id lo genera el servicio con {@code UUID.randomUUID()}
	 * (ADR-8), sin {@code @GeneratedValue}.
	 */
	@Id
	@Column(length = 36)
	private String adjuntoId;

	/** Fecha de creación; la estampa la BD/Hibernate al insertar. */
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	/**
	 * URL segura de Cloudinary. 500 porque la URL real con carpeta + hash +
	 * versión puede superar 255 caracteres.
	 */
	@Column(length = 500, nullable = false)
	private String url;

	/**
	 * public_id de Cloudinary (incluye la carpeta). 255 porque con carpetas
	 * profundas puede pasar del viejo String(100).
	 */
	@Column(length = 255, nullable = false)
	private String publicId;

	/** {@code imagen}, {@code pdf} o {@code otro}; decide el resource_type al borrar. */
	@Column(length = 20, nullable = false)
	private String tipoArchivo;

	@Column(length = 50, nullable = false)
	private String formato;

	@Column(nullable = false)
	private Long tamano;

	/** Nombre original del archivo; 250 por nombres con extensión larga. */
	@Column(nullable = false)
	private String nombreOriginal;

	/** Id suelto sin FK DDL; solo se llena para fotos de vehículo. */
	@Column(name = "vehiculo_id", length = 36)
	private String vehiculoId;

	/** Id suelto sin FK DDL; solo se llena cuando el adjunto vive en una orden. */
	@Column(name = "orden_id", length = 36)
	private String ordenId;

	/** Bandera de foto principal; el pivote {@code orden_adjunto} mantiene la suya. */
	@Column(nullable = false)
	private Boolean esPrincipal;

	@Column(length = 500)
	private String descripcion;
}
