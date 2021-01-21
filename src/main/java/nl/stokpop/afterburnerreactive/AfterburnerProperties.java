package nl.stokpop.afterburnerreactive;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AfterburnerProperties {

    private final String afterburnerName;
    private final String databaseConnectQuery;

    public AfterburnerProperties(
        @Value(value = "${spring.afterburner.name:AfterburnerReactive}") final String afterburnerName,
        @Value(value = "${afterburner.database.connect.query:SELECT 1}") final String databaseConnectQuery) {
        this.afterburnerName = afterburnerName;
        this.databaseConnectQuery = databaseConnectQuery;
    }

    public String getAfterburnerName() {
        return afterburnerName;
    }

    public String getDatabaseConnectQuery() {
        return databaseConnectQuery;
    }

}