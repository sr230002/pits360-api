package com.ues.dam.integration.firebase.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;

/**
 * Repositorio generico sobre una coleccion de Firestore. No es {@code @Component}:
 * cada servicio crea el suyo pasando la coleccion y como se lee/escribe el id.
 *
 * @param <T> tipo de la entidad mapeada
 */
public class FirestoreRepository<T> {

	private static final String UPDATED_AT = "actualizadoEn";

	private final Firestore db;
	private final String collection;
	private final Class<T> type;
	private final Function<T, String> idOf;
	private final BiConsumer<T, String> idSetter;
	private final DocumentMapper mapper = new DocumentMapper();

	public FirestoreRepository(Firestore db, String collection, Class<T> type, Function<T, String> idOf,
			BiConsumer<T, String> idSetter) {
		this.db = db;
		this.collection = collection;
		this.type = type;
		this.idOf = idOf;
		this.idSetter = idSetter;
	}

	public List<T> findAll() {
		try {
			return db.collection(collection).get().get().getDocuments().stream().map(this::toEntity).toList();
		} catch (Exception e) {
			throw fail("findAll", e);
		}
	}

	public Optional<T> findById(String id) {
		try {
			DocumentSnapshot snapshot = db.collection(collection).document(id).get().get();
			return snapshot.exists() ? Optional.of(toEntity(snapshot)) : Optional.empty();
		} catch (Exception e) {
			throw fail("findById", e);
		}
	}

	/**
	 * Escribe el documento completo. Si la entidad no trae id, se genera uno
	 * UUID y se le devuelve ya asignado.
	 */
	public T save(T entity) {
		try {
			String id = idOf.apply(entity);
			if (id == null || id.isBlank()) {
				id = UUID.randomUUID().toString();
				idSetter.accept(entity, id);
			}
			Map<String, Object> document = mapper.toMap(entity);
			document.put(UPDATED_AT, FieldValue.serverTimestamp());
			db.collection(collection).document(id).set(document).get();
			return entity;
		} catch (Exception e) {
			throw fail("save", e);
		}
	}

	public void delete(String id) {
		try {
			db.collection(collection).document(id).delete().get();
		} catch (Exception e) {
			throw fail("delete", e);
		}
	}

	/** Consulta por igualdad en un solo campo (no requiere indice compuesto). */
	public List<T> findBy(String field, Object value) {
		try {
			return db.collection(collection).whereEqualTo(field, value).get().get().getDocuments().stream()
					.map(this::toEntity).toList();
		} catch (Exception e) {
			throw fail("findBy", e);
		}
	}

	private T toEntity(DocumentSnapshot snapshot) {
		Map<String, Object> data = snapshot.getData();
		return mapper.fromMap(data == null ? Map.of() : data, type);
	}

	private RuntimeException fail(String operation, Exception cause) {
		if (cause instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
		return new RuntimeException("Firestore " + operation + " fallo en la coleccion '" + collection + "': "
				+ cause.getMessage(), cause);
	}
}
