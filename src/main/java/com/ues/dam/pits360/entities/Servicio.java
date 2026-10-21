package com.ues.dam.pits360.entities;

import com.ues.dam.pits360.validations.OnCreate;
import com.ues.dam.pits360.validations.OnUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Servicio del catálogo, mapea la tabla {@code servicios} de PostgreSQL (Neon).
 * El id es un UUID generado por el servidor al crear (ADR-8), nunca por la app.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "servicios")
public class Servicio {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false)
	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String nombre;

	@Column(nullable = false)
	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String descripcion;

	@Column(nullable = false)
	@NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "El precio es obligatorio")
	@PositiveOrZero(groups = {OnCreate.class, OnUpdate.class})
	private Double precio;

	/** Duración en minutos. */
	@Column(nullable = false)
	@NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "La duración es obligatoria")
	@Min(value = 0, groups = {OnCreate.class, OnUpdate.class})
	private Integer duracion;
}
