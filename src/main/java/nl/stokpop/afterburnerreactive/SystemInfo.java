package nl.stokpop.afterburnerreactive;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SystemInfo {
    private int availableProcessors;
    private long maxMemory;
    private long freeMemory;
    private long totalMemory;
    private int threads;
    private List<String> threadNames;
}
