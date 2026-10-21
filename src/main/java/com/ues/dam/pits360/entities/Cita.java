package com.ues.dam.pits360.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.ues.dam.pits360.validations.OnCreate;
import com.ues.dam.pits360.validations.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cita de servicio, mapea la tabla {@code citas}. Sin FK DDL:
 * {@code vehiculoId} y {@code tallerId} son ids sueltos (ADR/spec §5) y las
 * referencias las arma la app. {@code fecha} es {@code yyyy-MM-dd} y
 * {@code hora} es {@code HH:mm}, ambos {@code varchar} (ADR-9); el id lo
 * genera el servidor en el POST (ADR-8).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "citas")
public class Cita {

	@Id
	@Column(length = 36)
	private String id;

	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", groups = {OnCreate.class, OnUpdate.class},
			message = "fecha debe tener formato yyyy-MM-dd")
	private String fecha;

	@Pattern(regexp = "^\\d{2}:\\d{2}$", groups = {OnCreate.class, OnUpdate.class},
			message = "hora debe tener formato HH:mm")
	private String hora;

	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String estado;

	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String vehiculoId;

	@NotBlank(groups = {OnCreate.class, OnUpdate.class})
	private String tallerId;
}
