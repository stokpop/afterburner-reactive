package nl.stokpop.afterburnerreactive;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Log4j2
public class AfterburnerService {

    final AfterburnerProperties props;

    private final WebClient webClient;

    public Mono<BurnerMessage> delay(int requestDelayMillis) {
        long startTime = System.currentTimeMillis();
        return Mono.delay(Duration.ofMillis(requestDelayMillis)).then(Mono.defer(burnerMessageSupplier(startTime, requestDelayMillis)));
    }

    public Mono<BurnerMessage> delayWithSleep(int requestDelayMillis) {
        long startTime = System.currentTimeMillis();
        return Mono.defer(burnerMessageSupplierWithSleep(startTime, requestDelayMillis)).subscribeOn(Schedulers.boundedElastic());
    }

    private Supplier<Mono<BurnerMessage>> burnerMessageSupplierWithSleep(long startTime, long requestDelayMillis) {
        log.info("create with sleep in BurnerMessageSupplier, duration {}, startTime {}", requestDelayMillis, startTime);
        return () -> {
            try {
                Thread.sleep(requestDelayMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("mono with sleep just in BurnerMessageSupplier, duration {}, startTime {}", requestDelayMillis, startTime);
            BurnerMessage burnerMessage = createBurnerMessage(startTime, requestDelayMillis);
            return Mono.just(burnerMessage);
        };
    }

    private Supplier<Mono<BurnerMessage>> burnerMessageSupplier(long startTime, long requestDelayMillis) {
        log.info("create in BurnerMessageSupplier, duration {}, startTime {}", requestDelayMillis, startTime);
        return () -> {
            log.info("mono just in BurnerMessageSupplier, duration {}, startTime {}", requestDelayMillis, startTime);
            BurnerMessage burnerMessage = createBurnerMessage(startTime, requestDelayMillis);
            return Mono.just(burnerMessage);
        };
    }

    private BurnerMessage createBurnerMessage(long startTime, long requestDelayMillis) {
        long realDelayMillis = System.currentTimeMillis() - startTime;
        String msg = String.format("This was a delay of %s", requestDelayMillis);
        return BurnerMessage.builder()
            .message(msg)
            .name(props.getAfterburnerName())
            .durationInMillis(realDelayMillis)
            .build();
    }

    public Mono<String> remoteCall(String path) {
        return webClient
            .get()
            .uri(path)
            .retrieve()
            .bodyToMono(String.class);
    }

    public Flux<String> remoteCallMany(String path, int count) {
        Stream<String> paths = IntStream
            .range(0, count)
            .mapToObj(i -> path.replace("XYZ", String.valueOf(i * 100)));

        return Flux.fromStream(paths)
            .parallel()
            .runOn(Schedulers.elastic())
            .flatMap(this::remoteCall)
            .sequential();
    }

    public Mono<SystemInfo> systemInfo() {
        return Mono.fromSupplier(createSystemInfo()).subscribeOn(Schedulers.boundedElastic());
    }

    private Supplier<SystemInfo> createSystemInfo() {
        return () -> {

            Exception exception = new Exception("check stacktrace");
            log.error("stacktrace check!", exception);

            log.info("create new system info");

            Runtime runtime = Runtime.getRuntime();

            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

            List<String> threadNames = threadSet.stream()
                .map(t -> t.getName() + "-" + t.getState())
                .sorted()
                .collect(Collectors.toUnmodifiableList());

            return SystemInfo.builder()
                .availableProcessors(runtime.availableProcessors())
                .freeMemory(runtime.freeMemory())
                .maxMemory(runtime.maxMemory())
                .totalMemory(runtime.totalMemory())
                .threads(threadSet.size())
                .threadNames(threadNames)
                .build();
        };
    }
}
