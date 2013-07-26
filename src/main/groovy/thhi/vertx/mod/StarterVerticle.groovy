package thhi.vertx.mod

import org.vertx.groovy.platform.Verticle


public class StarterVerticle extends Verticle {

	def start() {

		container.with {

			def readerConfig = config.reader

			deployWorkerVerticle("groovy:" + InventoryReaderVerticle.class.name, readerConfig) { result ->
				logger.info("Deployed InventoryReader ${result.result()}")
			}

			def serverconfig = config.server

			deployWorkerVerticle("groovy:" + InventoryServerVerticle.class.name) { result ->
				logger.info("Deployed InventoryServer ${result.result()}")
			}
		}
	}
}
