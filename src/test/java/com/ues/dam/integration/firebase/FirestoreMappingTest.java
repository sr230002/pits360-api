package com.ues.dam.integration.firebase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ues.dam.integration.firebase.repository.DocumentMapper;
import com.ues.dam.pits360.entities.Adjunto;
import com.ues.dam.pits360.entities.OrdenAdjunto;
import com.ues.dam.pits360.entities.Usuario;

/**
 * Mapeo entidad <-> documento de Firestore. Sin Firestore, sin Spring.
 */
class FirestoreMappingTest {

	private final DocumentMapper mapper = new DocumentMapper();

	@Test
	void localDateTimeSeGuardaComoTextoIso() {
		Map<String, Object> document = mapper.toMap(adjunto());

		assertInstanceOf(String.class, document.get("fechaCreacion"));
		assertEquals("2026-10-21T10:30", document.get("fechaCreacion"));
	}

	@Test
	void losCamposNulosNoAparecen() {
		Map<String, Object> document = mapper.toMap(adjunto());

		assertFalse(document.containsKey("descripcion"));
		assertTrue(document.containsKey("url"));
	}

	@Test
	void losCamposTransientNoAparecen() {
		OrdenAdjunto ordenAdjunto = OrdenAdjunto.builder()
				.ordenAdjuntoId("oa-1")
				.ordenId("orden-1")
				.adjuntoId("adjunto-1")
				.visible(true)
				.esPrincipal(false)
				.adjunto(adjunto())
				.build();

		Map<String, Object> document = mapper.toMap(ordenAdjunto);

		assertFalse(document.containsKey("adjunto"));
		assertEquals("orden-1", document.get("ordenId"));
	}

	@Test
	void elIdempotenteMapeoConservaElLocalDateTime() {
		Adjunto original = adjunto();

		Adjunto leido = mapper.fromMap(mapper.toMap(original), Adjunto.class);

		assertEquals(original.getFechaCreacion(), leido.getFechaCreacion());
		assertEquals(original.getAdjuntoId(), leido.getAdjuntoId());
	}

	@Test
	void elPasswordHashSePersisteAunqueJsonIgnoreLoOculteEnHttp() {
		Usuario usuario = Usuario.builder()
				.id("u-1")
				.nombre("Ana Cliente")
				.email("ana@pits360.com")
				.passwordHash("$2a$10$hash")
				.rol("cliente")
				.build();

		Map<String, Object> document = mapper.toMap(usuario);

		assertEquals("$2a$10$hash", document.get("passwordHash"));
	}

	private Adjunto adjunto() {
		return Adjunto.builder()
				.adjuntoId("adjunto-1")
				.fechaCreacion(LocalDateTime.of(2026, 10, 21, 10, 30))
				.url("https://res.cloudinary.com/demo/image/upload/foto.jpg")
				.publicId("demo/foto")
				.tipoArchivo("imagen")
				.formato("jpg")
				.tamano(1024L)
				.nombreOriginal("foto.jpg")
				.esPrincipal(false)
				.build();
	}
}
