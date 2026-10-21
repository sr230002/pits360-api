package com.ues.dam.pits360.utilities;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

import com.ues.dam.pits360.exceptions.ApiException;

/**
 * Lista blanca de categorías de adjuntos y la carpeta Cloudinary que compone
 * cada una.
 *
 * <p>
 * El cliente NUNCA manda una ruta: manda una categoría de esta lista y el
 * servidor construye la carpeta sustituyendo los placeholders con los ids
 * validados. Cualquier categoría fuera de la lista → 400, y cualquier id
 * fuera del formato de 36 caracteres → 400, así no se filtrarían rutas como
 * {@code ../otra-carpeta} ni barras arbitrarias hacia Cloudinary.
 */
public enum AdjuntoCategoria {

	APP_LOGOS("pits360/app/logos"),
	APP_SPLASH("pits360/app/splash"),
	APP_ICONOS("pits360/app/iconos"),
	ADMIN_CATALOGO("pits360/admin/catalogo"),
	CLIENTE_PERFIL("pits360/clientes/{usuarioId}/perfil", true, false, false),
	CLIENTE_VEHICULO("pits360/clientes/{usuarioId}/vehiculos/{vehiculoId}", true, true, false),
	TECNICO_PERFIL("pits360/tecnicos/{usuarioId}/perfil", true, false, false),
	ORDEN_EVIDENCIA("pits360/ordenes/{ordenId}/evidencias", false, false, true),
	ORDEN_DOCUMENTO("pits360/ordenes/{ordenId}/documentos", false, false, true);

	/** Formato admitido para los ids que van dentro de la carpeta. */
	private static final java.util.regex.Pattern ID_VALIDO =
			java.util.regex.Pattern.compile("^[0-9a-fA-F-]{36}$");

	private final String plantilla;
	private final boolean requiereUsuarioId;
	private final boolean requiereVehiculoId;
	private final boolean requiereOrdenId;

	AdjuntoCategoria(String plantilla) {
		this(plantilla, false, false, false);
	}

	AdjuntoCategoria(String plantilla, boolean requiereUsuarioId, boolean requiereVehiculoId,
			boolean requiereOrdenId) {
		this.plantilla = plantilla;
		this.requiereUsuarioId = requiereUsuarioId;
		this.requiereVehiculoId = requiereVehiculoId;
		this.requiereOrdenId = requiereOrdenId;
	}

	public boolean requiereUsuarioId() {
		return requiereUsuarioId;
	}

	public boolean requiereVehiculoId() {
		return requiereVehiculoId;
	}

	public boolean requiereOrdenId() {
		return requiereOrdenId;
	}

	/** Analiza el valor del cliente contra la lista blanca; fuera de ella → 400. */
	public static AdjuntoCategoria from(String valor) {
		if (valor != null) {
			try {
				return AdjuntoCategoria.valueOf(valor.trim().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException e) {
				// cae abajo con el mensaje de error
			}
		}
		throw new ApiException(HttpStatus.BAD_REQUEST, "Categoría inválida: "
				+ valor + ". Categorías válidas: " + Arrays.stream(AdjuntoCategoria.values())
						.map(Enum::name).collect(Collectors.joining(", ")));
	}

	/**
	 * Compone la carpeta final sustituyendo los placeholders con los ids
	 * autenticados. Los ids que la categoría no admite → 400 (coherencia), y
	 * los que necesita con formato distinto a 36 caracteres UUID → 400.
	 */
	public String construirRuta(String usuarioId, String vehiculoId, String ordenId) {
		String ruta = plantilla;
		ruta = sustituir(ruta, "{usuarioId}", usuarioId, requiereUsuarioId);
		ruta = sustituir(ruta, "{vehiculoId}", vehiculoId, requiereVehiculoId);
		ruta = sustituir(ruta, "{ordenId}", ordenId, requiereOrdenId);
		return ruta;
	}

	private String sustituir(String ruta, String placeholder, String id, boolean requerido) {
		if (id == null || id.isBlank()) {
			if (requerido) {
				throw new ApiException(HttpStatus.BAD_REQUEST,
						"La categoría " + name() + " requiere " + placeholder.replace("{", "").replace("}", ""));
			}
			return ruta;
		}
		if (!requerido) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"La categoría " + name() + " no admite " + placeholder.replace("{", "").replace("}", ""));
		}
		if (!ID_VALIDO.matcher(id.trim()).matches()) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					placeholder.replace("{", "").replace("}", "") + " no tiene formato de id válido");
		}
		return ruta.replace(placeholder, id.trim());
	}
}
