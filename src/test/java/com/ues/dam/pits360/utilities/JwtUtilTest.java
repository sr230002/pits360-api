package com.ues.dam.pits360.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests puros de JwtUtil: se instancia directo con el secret, sin Spring.
 */
class JwtUtilTest {

	private static final String SECRET = "test-secret-de-32-bytes-minimo-para-hmac!!";

	@Test
	void tokenGeneradoEsValido() {
		JwtUtil util = new JwtUtil(SECRET, 60_000);
		String token = util.generateToken("a@b.com");

		assertThat(token).isNotBlank();
		assertThat(util.isValid(token)).isTrue();
	}

	@Test
	void extractEmailDevuelveElSubjectDelToken() {
		JwtUtil util = new JwtUtil(SECRET, 60_000);
		String token = util.generateToken("user@test.com");

		assertThat(util.extractEmail(token)).isEqualTo("user@test.com");
	}

	@Test
	void tokenConFirmaCorruptaNoEsValido() {
		JwtUtil util = new JwtUtil(SECRET, 60_000);
		String token = util.generateToken("a@b.com");
		String corrupto = token.substring(0, token.length() - 4) + "XXXX";

		assertThat(util.isValid(corrupto)).isFalse();
	}

	@Test
	void tokenFirmadoConOtroSecretNoEsValido() {
		String token = new JwtUtil(SECRET, 60_000).generateToken("a@b.com");
		JwtUtil otroUtil = new JwtUtil("otro-secret-de-32-bytes-minimo-para-hmac!!", 60_000);

		assertThat(otroUtil.isValid(token)).isFalse();
	}
}
