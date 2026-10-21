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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vehículo del cliente, mapea la tabla {@code vehiculos} de PostgreSQL (Neon).
 * El id es un UUID generado por el servidor al crear (ADR-8), nunca por la app.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehiculos")
public class Vehiculo {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false)
	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String placa;

	@Column(nullable = false)
	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String marca;

	@Column(nullable = false)
	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String modelo;

	@Column(nullable = false)
	@NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "El año es obligatorio")
	@Min(value = 0, groups = {OnCreate.class, OnUpdate.class})
	private Integer anio;

	@Column(nullable = false)
	@NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "El kilometraje es obligatorio")
	@Min(value = 0, groups = {OnCreate.class, OnUpdate.class})
	private Integer kilometraje;
}
