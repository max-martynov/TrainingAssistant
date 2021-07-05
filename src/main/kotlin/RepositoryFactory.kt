import io.ktor.config.*

fun loadClientRepository(config: ApplicationConfig): ClientRepository =
    when (config.property("db.clientRepository").getString()) {
        "InMemoryClientRepository" -> {
            InMemoryClientRepository()
        }
        "InDatabaseClientRepository" -> {
            val connStr = config.property("db.connStr").getString()
            val driver = config.property("db.driver").getString()
            InDataBaseClientRepository(connStr, driver)
        }
        else -> {
            throw IllegalArgumentException("No correct repository specified")
        }
    }