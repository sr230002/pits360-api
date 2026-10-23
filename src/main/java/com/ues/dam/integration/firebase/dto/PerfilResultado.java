package com.ues.dam.integration.firebase.dto;

import com.ues.dam.pits360.entities.Usuario;

/**
 * Resultado de actualizar el perfil: el usuario ya guardado y el token nuevo.
 *
 * @param usuario usuario guardado tras aplicar los cambios
 * @param token token JWT del correo nuevo; {@code null} si el correo no cambió
 */
public record PerfilResultado(Usuario usuario, String token) {
}
