package com.ues.dam.pits360.utilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.ues.dam.pits360.exceptions.ApiException;

/**
 * Tests puros de la lista blanca de categorías: sin Spring ni Cloudinary.
 */
class AdjuntoCategoriaTest {

	private static final String ID = "123e4567-e89b-12d3-a456-426614174000";

	@Test
	void categoriasConocidasSeResuelven() {
		assertThat(AdjuntoCategoria.from("APP_LOGOS")).isEqualTo(AdjuntoCategoria.APP_LOGOS);
		// Tolerante: cualquier caja, recortando espacios.
		assertThat(AdjuntoCategoria.from(" orden_evidencia "))
				.isEqualTo(AdjuntoCategoria.ORDEN_EVIDENCIA);
		assertThat(AdjuntoCategoria.from("Cliente_Perfil"))
				.isEqualTo(AdjuntoCategoria.CLIENTE_PERFIL);
	}

	@Test
	void categoriaDesconocidaDa400() {
		assertThatThrownBy(() -> AdjuntoCategoria.from("pits360/otra/carpeta"))
				.isInstanceOf(ApiException.class);
	}

	@Test
	void carpetaAppNoDependeDeIds() {
		assertThat(AdjuntoCategoria.APP_SPLASH.construirRuta(null, null, null))
				.isEqualTo("pits360/app/splash");
	}

	@Test
	void carpetaOrdenConOrdenIdValido() {
		assertThat(AdjuntoCategoria.ORDEN_EVIDENCIA.construirRuta(null, null, ID))
				.isEqualTo("pits360/ordenes/" + ID + "/evidencias");
	}

	@Test
	void carpetaVehiculoConUsuarioYVehiculo() {
		String otroId = "00000000-0000-0000-0000-000000000abc";
		assertThat(AdjuntoCategoria.CLIENTE_VEHICULO.construirRuta(ID, otroId, null))
				.isEqualTo("pits360/clientes/" + ID + "/vehiculos/" + otroId);
	}

	@Test
	void rutaTravesinaSeRechaza() {
		assertThatThrownBy(() -> AdjuntoCategoria.ORDEN_EVIDENCIA
				.construirRuta(null, null, "../app/logos"))
				.isInstanceOf(ApiException.class);
	}

	@Test
	void idFaltanteEnCategoriaQueLoRequiere() {
		assertThatThrownBy(() -> AdjuntoCategoria.ORDEN_DOCUMENTO.construirRuta(null, null, null))
				.isInstanceOf(ApiException.class);
	}

	@Test
	void idSobranteEnCategoriaQueNoLoAdmite() {
		assertThatThrownBy(() -> AdjuntoCategoria.APP_LOGOS.construirRuta(ID, ID, ID))
				.isInstanceOf(ApiException.class);
	}
}
