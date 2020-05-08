package nl.stokpop.afterburnerreactive;

import lombok.Value;

@Value
public class BurnerMessage {
    String message;
    String name;
    long durationInMillis;
}

