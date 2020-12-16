package nl.stokpop.afterburnerreactive;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BurnerMessage {
    String message;
    String name;
    long durationInMillis;
}

