package com.ues.dam.pits360.utilities;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ues.dam.pits360.exceptions.ApiException;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

/**
 * Pasarela única hacia el SDK de Cloudinary. Las credenciales llegan por
 * variables de entorno vía {@code application.yml}; nunca se registran en
 * logs ni se devuelven en las respuestas.
 *
 * <p>
 * <p>
 * {@code @PostConstruct}: el SDK no expone setters, así que se instancia el
 * {@link Cloudinary} con un mapa de configuración
 * ({@code ObjectUtils.asMap}).
 */
@Slf4j
@Service
public class CloudinaryService {

	@Value("${cloudinary.cloud-name}")
	private String cloudName;

	@Value("${cloudinary.api-key}")
	private String apiKey;

	@Value("${cloudinary.api-secret}")
	private String apiSecret;

	private Cloudinary cloudinary;

	@PostConstruct
	void init() {
		cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", cloudName,
				"api_key", apiKey,
				"api_secret", apiSecret,
				"secure", true));
	}

	/**
	 * Sube el archivo a la carpeta indicada y devuelve el resultado crudo del
	 * SDK: {@code secure_url}, {@code public_id}, {@code format}, {@code bytes} y
	 * {@code original_filename}. Al mandar {@code folder} la firma lo cubre el
	 * SDK. {@code resource_type=image} explícito: Cloudinary trata los PDF como
	 * imágenes (los transforma), así que subida y borrado deben ir SIEMPRE
	 * por el mismo resource_type o el delete no encontraría el asset.
	 *
	 * @throws ApiException 400 si el archivo es inválido (contenido corrupto
	 *         o formato que Cloudinary rechaza) y 502 si el almacén falla
	 */
	public Map<String, Object> upload(MultipartFile file, String carpeta) {
		try {
			return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
					"folder", carpeta, "resource_type", "image"));
		} catch (IOException e) {
			log.error("Fallo subiendo archivo a Cloudinary (carpeta {}): {}", carpeta, e.getMessage());
			throw new ApiException(HttpStatus.BAD_GATEWAY,
					"No se pudo subir el archivo al almacenamiento en la nube.");
		} catch (RuntimeException e) {
			// El SDK lanza RuntimeException por archivos corruptos
			// (p. ej. "Invalid PDF file"): eso es culpa del archivo, no del
			// proveedor; 400 claro en vez de 500.
			if (e.getMessage() != null && e.getMessage().toLowerCase().contains("invalid")) {
				throw new ApiException(HttpStatus.BAD_REQUEST,
						"El archivo no se pudo procesar: formato de imagen o PDF no válido.");
			}
			log.error("Fallo subiendo archivo a Cloudinary (carpeta {}): {}", carpeta, e.getMessage());
			throw new ApiException(HttpStatus.BAD_GATEWAY,
					"No se pudo subir el archivo al almacenamiento en la nube.");
		}
	}

	/**
	 * Destruye el recurso por {@code public_id}, SIEMPRE con
	 * {@code resource_type=image}: es el mismo tipo con el que {@link #upload}
	 * sube todo (los PDF los transforma Cloudinary como imágenes); borrar en
	 * {@code raw} sería un no-op silencioso y acumularía basura del free-tier.
	 * Borrar dos veces no rompe: si el asset no existe, se loguea y sigue.
	 */
	public void delete(String publicId, String tipoArchivo) {
		Map<String, Object> resultado;
		try {
			resultado = cloudinary.uploader().destroy(publicId,
					ObjectUtils.asMap("resource_type", "image", "invalidate", true));
		} catch (IOException e) {
			log.error("Fallo borrando {} de Cloudinary: {}", publicId, e.getMessage());
			throw new ApiException(HttpStatus.BAD_GATEWAY,
					"No se pudo borrar el archivo del almacenamiento en la nube.");
		}
		if ("not found".equals(resultado.get("result"))) {
			log.warn("Nada que borrar: el asset {} no existía en el resource_type image.", publicId);
		}
	}
}
