package thhi.vertx.mod

import org.vertx.groovy.core.http.HttpClient
import org.vertx.groovy.platform.Verticle

class InventoryReaderVerticle extends Verticle {

	static HttpClient client

	def start() {

		def inventoryHost = container.config.host
		def inventoryPort = container.config.port

		client = vertx.createHttpClient(host: inventoryHost, port: inventoryPort)
		vertx.setPeriodic(30000) { requestInventory() }
	}

	def requestInventory() {
		client.getNow("/report.json") { response ->
			response.bodyHandler { data -> putInventory(data) }
		}
	}

	void putInventory(data) {
		vertx.sharedData.getMap("inventory").put("inventory", data)
	}

	void getInventory() {
		vertx.sharedData.getMap("inventory")["inventory"]
	}
}
