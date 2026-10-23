package com.ues.dam.integration.firebase.repository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.persistence.Transient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Convierte entidades JPA en documentos planos de Firestore y viceversa.
 * No depende de Firestore, asi que se puede probar sin emulador ni credenciales.
 */
@Component
public class DocumentMapper {

	/** Jackson 3 no tipa {@code convertValue(entity, Map.class)}: se usa TypeReference. */
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper mapper;

	public DocumentMapper() {
		SimpleModule module = new SimpleModule();
		// .toString() ISO-8601 sin segundos: "2026-10-21T10:30" (el de Jackson seria "10:30:00").
		module.addSerializer(LocalDateTime.class, new ToStringSerializer(LocalDateTime.class));
		module.addSerializer(LocalDate.class, new ToStringSerializer(LocalDate.class));
		// Firestore necesita el passwordHash, que @JsonIgnore oculta en las respuestas HTTP.
		this.mapper = JsonMapper.builder().disable(MapperFeature.USE_ANNOTATIONS).addModule(module).build();
	}

	/**
	 * Entidad -> documento. Quita claves nulas (recursivo en mapas anidados, no
	 * en listas) y campos marcados con {@code @Transient}.
	 */
	public Map<String, Object> toMap(Object entity) {
		Map<String, Object> raw = mapper.convertValue(entity, MAP_TYPE);
		Map<String, Object> document = new LinkedHashMap<>();
		raw.forEach((key, value) -> {
			if (value != null) {
				document.put(key, stripNulls(value));
			}
		});
		// shortcut: @Transient solo se quita en la raiz; un POJO anidado ya viene convertido a mapa y pierde su tipo, upgrade si una entidad anidada lo usa.
		for (String transientField : transientFields(entity.getClass())) {
			document.remove(transientField);
		}
		return document;
	}

	/**
	 * Documento -> entidad. Las claves desconocidas se ignoran (Jackson 3 las
	 * ignora por defecto) y los LocalDateTime guardados como texto ISO se
	 * parsean a mano.
	 */
	public <T> T fromMap(Map<String, Object> map, Class<T> type) {
		Map<String, Object> source = new LinkedHashMap<>(map);
		for (Field field : allFields(type)) {
			if (field.getType() == LocalDateTime.class && source.get(field.getName()) instanceof String text) {
				source.put(field.getName(), LocalDateTime.parse(text));
			}
		}
		return mapper.convertValue(source, type);
	}

	private Object stripNulls(Object value) {
		if (value instanceof Map<?, ?> nested) {
			Map<String, Object> clean = new LinkedHashMap<>();
			nested.forEach((key, nestedValue) -> {
				if (nestedValue != null) {
					clean.put(String.valueOf(key), stripNulls(nestedValue));
				}
			});
			return clean;
		}
		return value;
	}

	private List<String> transientFields(Class<?> type) {
		List<String> names = new ArrayList<>();
		for (Field field : allFields(type)) {
			if (field.isAnnotationPresent(Transient.class)) {
				names.add(field.getName());
			}
		}
		return names;
	}

	private List<Field> allFields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
			for (Field field : current.getDeclaredFields()) {
				if (!field.isSynthetic()) {
					fields.add(field);
				}
			}
		}
		return fields;
	}
}
