package nl.stokpop.afterburnerreactive;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
@Log4j2
public class AfterburnerReactiveApplication {

	@Bean
	RouterFunction<ServerResponse> routes(AfterburnerService afterburnerService) {
		return route()
			.GET("/system-info",
				request -> ok().body(afterburnerService.systemInfo(), SystemInfo.class))
			.GET("/delay",
				request -> {
					int requestDelayMillis = Integer.parseInt(request.queryParam("duration").orElse("100"));
					return ok().body(afterburnerService.delay(requestDelayMillis), BurnerMessage.class);
				})
			.GET("/remote/call",
				request -> {
					String path = request.queryParam("path").orElse("/");
					return ok().body(afterburnerService.remoteCall(path), String.class);
				})
			.GET("/remote/call-many",
				request -> {
					String path = request.queryParam("path").orElse("/");
					int count = Integer.parseInt(request.queryParam("count").orElse("10"));
					return ok().body(afterburnerService.remoteCallMany(path, count), String.class);
				})
			.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(AfterburnerReactiveApplication.class, args);
	}

}
