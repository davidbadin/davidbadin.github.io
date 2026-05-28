package sk.punkacidetom.pd2026.core.model

data class Stage(
    val code: String,
    val displayName: String,
)

// Stage definitions — never translated; display names are the source of truth.
// Adding a new stage: add an entry here and the Timetable picks it up automatically.
object Stages {
    val all = listOf(
        Stage(code = "A", displayName = "Punk For Children Stage"),
        Stage(code = "B", displayName = "United Stage"),
    )

    private val byCode = all.associateBy { it.code }

    fun displayName(code: String): String = byCode[code]?.displayName ?: code
}
