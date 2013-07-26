package thhi.vertx.mod

import org.vertx.groovy.platform.Verticle


public class StarterVerticle extends Verticle {

	def start() {

		container.with {

			deployWorkerVerticle("groovy:" + InventoryReaderVerticle.class.name, config.reader) { result ->
				logger.info("Deployed InventoryReader ${result.result()}")
			}

			deployWorkerVerticle("groovy:" + InventoryServerVerticle.class.name, config.server) { result ->
				logger.info("Deployed InventoryServer ${result.result()}")
			}
		}
	}
}
