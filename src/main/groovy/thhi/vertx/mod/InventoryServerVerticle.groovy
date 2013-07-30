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

	def columnConfig = { container.config.columns }

	def createWebServer(hostname, port) {

		RouteMatcher rm = new RouteMatcher()

		rm.get("/nodes") { request ->

			def inventory = parseInventory()
			def params = collectParams(request)
			// generic search
			if("search" in params) {
				inventory.retainAll(genericSearch(inventory, params.search as List))
				// if there are more parameters..
				params.remove("search")
			}
			// special search
			if(params) {
				params.each {
					inventory.retainAll(specialSearch(inventory, it))
				}
			}
			def nodes = inventory.collect { it.fqdn }
			request.response.end(nodes.join("\n"))
		}

		rm.get("/inventory"){ request ->
			request.response.end(getInventory())
		}

		rm.get("/inventory/columns"){ request ->
			request.response.end(new JsonArray(columnConfig()).encode())
		}

		rm.get("/") { request ->
			request.response.sendFile("web/index.html")
		}

		rm.getWithRegEx(".*") { request ->
			request.response.sendFile("web${request.uri}")
		}

		HttpServer server = vertx.createHttpServer()
		server.requestHandler(rm.asClosure())
		server.listen(port, hostname)
	}

	Set genericSearch(inventory, List searchTerms) {
		def nodes = [] as Set
		inventory.each { machine ->
			machine.each { property ->
				searchTerms.each {
					if(property.value && property.value.contains(it)) {
						nodes.add(machine)
					}
				}
			}
		}
		return nodes
	}

	Set specialSearch(inventory, param) {
		def nodes = [] as Set
		inventory.each { machine ->
			for(property in machine) {
				if(property.key.equals(param.key) && property.value && property.value.contains(param.value)) {
					nodes.add(machine)
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

	String getInventory() {
		vertx.sharedData.getMap("inventory")["inventory"].toString()
	}

	def parseInventory() {
		new JsonSlurper().parseText(getInventory())
	}
}