package com.ues.dam.pits360.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import com.ues.dam.pits360.validations.OnCreate;
import com.ues.dam.pits360.validations.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Orden de trabajo, mapea la tabla {@code ordenes}. Sin FK DDL:
 * {@code vehiculoId} y {@code tallerId} son ids sueltos (ADR/spec §5) y las
 * referencias las arma la app. {@code fecha} es {@code yyyy-MM-dd} en
 * {@code varchar}; el id lo genera el servidor en el POST (ADR-8).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ordenes")
public class Orden {

	@Id
	@Column(length = 36)
	private String id;

	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String numero;

	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String estado;

	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String vehiculoId;

	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String tallerId;

	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", groups = {OnCreate.class, OnUpdate.class},
			message = "fecha debe tener formato yyyy-MM-dd")
	private String fecha;

	@NotNull(groups = {OnCreate.class, OnUpdate.class}, message = "El total es obligatorio")
	@PositiveOrZero(groups = {OnCreate.class, OnUpdate.class})
	private Double total;

	@Column(length = 36)
	private String tecnicoId;

	@Column(length = 36)
	private String citaId;

	@Column(length = 2000)
	private String nota;

	@Column(length = 2000)
	private String trabajos;

	@Column(length = 20)
	private String metodoPago;

	@Min(value = 0, groups = {OnUpdate.class})
	@Max(value = 100, groups = {OnUpdate.class})
	private Integer avance;
}
