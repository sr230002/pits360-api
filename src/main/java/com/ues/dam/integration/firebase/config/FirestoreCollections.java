package com.ues.dam.integration.firebase.config;

/**
 * Nombres de las colecciones de Firestore usadas por Pits360.
 *
 * <p>
 * Constantes centralizadas para que ningún servicio escriba el nombre de la
 * colección a mano.
 */
public final class FirestoreCollections {

	public static final String USUARIOS = "usuarios";
	public static final String VEHICULOS = "vehiculos";
	public static final String TALLERES = "talleres";
	public static final String SERVICIOS = "servicios";
	public static final String CITAS = "citas";
	public static final String ORDENES = "ordenes";
	public static final String ADJUNTOS = "adjuntos";
	public static final String ORDEN_ADJUNTOS = "orden_adjuntos";

	private FirestoreCollections() {
	}

}
