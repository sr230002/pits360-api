package com.ues.dam.pits360.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pivote orden↔adjunto, mapea la tabla {@code orden_adjunto}. Columnas String
 * sueltas sin FK DDL ni {@code @ManyToOne} (convención del proyecto, sin
 * proxies LAZY al serializar): el adjunto asociado lo arma el servicio para
 * la lectura via el campo transitorio {@link #adjunto}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orden_adjunto")
public class OrdenAdjunto {

	/**
	 * UUID String(36); el id lo genera el servicio (ADR-8), sin
	 * {@code @GeneratedValue}.
	 */
	@Id
	@Column(length = 36)
	private String ordenAdjuntoId;

	@Column(name = "orden_id", length = 36, nullable = false)
	private String ordenId;

	@Column(name = "adjunto_id", length = 36, nullable = false)
	private String adjuntoId;

	@Column(length = 500)
	private String descripcion;

	@Column(nullable = false)
	private Boolean visible;

	@Column(nullable = false)
	private Boolean esPrincipal;

	/** No mapea a columna: lo rellena el servicio al leer para devolver el adjunto. */
	@Transient
	private Adjunto adjunto;
}
