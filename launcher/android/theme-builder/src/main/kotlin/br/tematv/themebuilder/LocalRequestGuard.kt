package br.tematv.themebuilder

/**
 * Barra pedidos de outros sites abertos no mesmo navegador (D-03): `Host` só do próprio endereço
 * (contra DNS rebinding) e escritas só em JSON e sem `Origin` estranho (força a pré-consulta CORS).
 */
class LocalRequestGuard(port: Int) {

    private val allowedHosts = setOf("localhost:$port", "127.0.0.1:$port")
    private val allowedOrigins = allowedHosts.map { "http://$it" }.toSet()

    /** Motivo da recusa, ou `null` se o pedido pode seguir. */
    fun rejectionReason(method: String, host: String?, origin: String?, contentType: String?): String? {
        if (host == null || host.lowercase() !in allowedHosts) return "Host não permitido"
        if (method == "GET" || method == "HEAD") return null
        if (origin != null && origin.lowercase() !in allowedOrigins) return "Origem não permitida"
        if (method in BODY_METHODS && contentType?.lowercase()?.startsWith("application/json") != true) {
            return "Envie os dados em JSON"
        }
        return null
    }

    private companion object {
        val BODY_METHODS = setOf("POST", "PUT")
    }
}
