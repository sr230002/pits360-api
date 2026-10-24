package com.ues.dam.integration.firebase.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Arranque del SDK de Firebase Admin y exposición del cliente de Firestore.
 *
 * <p>
 * Las credenciales llegan por la variable de entorno
 * {@code FIREBASE_SERVICE_ACCOUNT_PATH}, que apunta al JSON de service account
 * descargado de la consola de Firebase. El archivo nunca se registra en logs
 * ni se devuelve en respuestas.
 *
 * <p>
 * El project ID no se fija de forma explícita: {@link FirebaseApp} lo infiere
 * del {@code project_id} del propio service account.
 */
@Slf4j
@Configuration
@Profile("!jpa")
public class FirebaseConfig {

	private final FirebaseApp firebaseApp;

	public FirebaseConfig(@Value("${FIREBASE_SERVICE_ACCOUNT_PATH:}") String serviceAccountPath) {
		this.firebaseApp = initFirebaseApp(serviceAccountPath);
	}

	private static FirebaseApp initFirebaseApp(String serviceAccountPath) {
		if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
			throw new IllegalStateException(
					"Falta la variable de entorno FIREBASE_SERVICE_ACCOUNT_PATH. "
							+ "Defínela con la ruta absoluta al JSON de credenciales de Firebase "
							+ "(por ejemplo: C:/ruta/a/service-account.json) y reinicia la aplicación.");
		}

		Path path = Path.of(serviceAccountPath);
		if (!Files.isRegularFile(path)) {
			throw new IllegalStateException(
					"FIREBASE_SERVICE_ACCOUNT_PATH apunta a un archivo que no existe: '" + path.toAbsolutePath() + "'. "
							+ "Corrige la variable para que apunte al JSON de credenciales de Firebase "
							+ "(ruta absoluta esperada).");
		}

		try (FileInputStream in = new FileInputStream(path.toFile())) {
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(in))
					.build();
			FirebaseApp app = FirebaseApp.getApps().isEmpty()
					? FirebaseApp.initializeApp(options)
					: FirebaseApp.getApps().get(0);
			log.info("Firebase inicializado con las credenciales de {}", path.toAbsolutePath());
			return app;
		} catch (IOException e) {
			throw new IllegalStateException(
					"No se pudo leer el archivo de credenciales de Firebase en '" + path.toAbsolutePath() + "': "
							+ e.getMessage(), e);
		}
	}

	// destroyMethod="" : Spring NO cierra Firestore al destruir el contexto.
	// Con devtools restart el contexto viejo cerraba la instancia compartida y
	// el nuevo recibia el mismo cliente ya cerrado ("already closed").
	@Bean(destroyMethod = "")
	public Firestore firestore() {
		return FirestoreClient.getFirestore(firebaseApp);
	}

}
