package io.srinskit.apiserver;

import io.srinskit.adder.AdderService;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.core.http.HttpServerResponse;
import java.io.*;

public class APIServerVerticle extends AbstractVerticle {
	static String historyFileName = "data/api-server-history.txt";

	@Override
	public void start() {
		System.out.println("Starting an API server");
		Router router = Router.router(vertx);

		router.route("/add/:x/:y/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			AdderService adderService = AdderService.createProxy(vertx, "adder-service-address");
			Integer x = new Integer(routingContext.request().getParam("x"));
			Integer y = new Integer(routingContext.request().getParam("y"));
			adderService.operate(x, y, res -> {
				String reply = "";
				if (res.succeeded()) {
					reply = String.format("%d + %d = %d\n", x, y, res.result());
				} else {
					reply = String.format("%d + %d = %s\n", x, y, "ERROR, " + res.cause());
				}
				try {
					FileWriter fileWriter = new FileWriter(historyFileName, true);
					fileWriter.write(reply);
					fileWriter.close();
				} catch (IOException ex) {
					System.out.println("Error writing to history file");
					reply = "ERROR: " + ex.getMessage();
				}
				response.end(reply);
			});
		});

		router.route("/history").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.sendFile(historyFileName);
			response.end();
		});

		router.route("/clear_history").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			String reply = "Done";
			try {
				FileWriter fileWriter = new FileWriter(historyFileName);
				fileWriter.close();
			} catch (IOException ex) {
				System.out.println("Error writing to history file");
				reply = "ERROR: " + ex.getMessage();
			}
			response.end(reply);
		});

		vertx.createHttpServer().requestHandler(router).listen(8080);
	}

	@Override
	public void stop() {
		System.out.println("Stopping an API server");
	}
}
