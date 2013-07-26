package thhi.vertx.mod

import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject
import groovy.json.JsonSlurper


class InventoryServerVerticle extends Verticle {

	def start () {

		def hostname = container.config.host
		def port = container.config.port as int

		createWebServer(hostname, port)
	}

	def createWebServer(hostname, port) {

		RouteMatcher rm = new RouteMatcher()

		rm.get("/nodes") { request ->

			def inventory = parseInventory()
			def params = collectParams(request)
			def nodes = inventory.collect { it.fqdn }
			// generic search
			if("search" in params) {
				nodes = genericSearch(inventory, params.search as List)
				// if there are more parameters..
				params.remove("search")
			}
			// special search
			if(params) {
				nodes.retainAll(specialSearch(inventory, params))
			}
			request.response.end(nodes.join("\n"))
		}

		HttpServer server = vertx.createHttpServer()
		server.requestHandler(rm.asClosure())
		server.listen(port, hostname)
	}

	def genericSearch(inventory, List searchTerms) {
		def nodes = []
		for(machine in inventory) {
			for(property in machine) {
				searchTerms.each {
					if(property.value && property.value.contains(it)) {
						nodes.add(machine.fqdn)
					}
				}
			}
		}
		return nodes
	}

	def specialSearch(inventory, Map searchParams) {
		def nodes = []
		for(machine in inventory) {
			for(property in machine) {
				searchParams.each {
					if(property.key.equals(it.key) && property.value && property.value.contains(it.value)) {
						nodes.add(machine.fqdn)
					}
				}
			}
		}
		return nodes
	}

	Map collectParams(request) {
		def params = [:]
		def entries = request.params.entries
		entries.each { entry ->
			if("search".equals(entry.key)) {
				params.put("search", entry.value.replaceAll("\\s+"," ").split("\\s"))
			} else {
				params.put(entry.key, entry.value.trim())
			}
		}
		return params
	}

	def parseInventory() {
		new JsonSlurper().parseText(vertx.sharedData.getMap("inventory")["inventory"].toString())
	}
}