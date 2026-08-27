package com.mindflow.nova.ui.screens.lessons

data class MatchingPair(
    val id: Int,
    val term: String,
    val match: String
)

/**
 * Contenido del minijuego de relación de conceptos para la misión
 * "Reconocer mis derechos". El wireframe de Figma usaba texto Lorem Ipsum de
 * relleno; acá se reemplaza por vocabulario real del mismo universo temático
 * de NOVA, ya que el backend todavía no expone estos pares.
 */
object MatchingMockData {

    val reconocerDerechosPairs = listOf(
        MatchingPair(1, "Igualdad", "Mismos derechos"),
        MatchingPair(2, "Dignidad", "Respeto"),
        MatchingPair(3, "Equidad", "Justicia"),
        MatchingPair(4, "Empoderamiento", "Autonomía"),
        MatchingPair(5, "Discriminación", "Exclusión"),
        MatchingPair(6, "Violencia", "Daño"),
        MatchingPair(7, "Denuncia", "Protección"),
        MatchingPair(8, "Consentimiento", "Voluntad"),
        MatchingPair(9, "Diversidad", "Inclusión")
    )
}
