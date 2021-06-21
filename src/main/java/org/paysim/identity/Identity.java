package org.paysim.identity;

import com.opencsv.bean.CsvBindByName;

import java.util.Map;

/**
 * A core "identity" for an account in PaySim requiring an `id` and a `name`.
 */
public abstract class Identity {
    @CsvBindByName
    public final String id;

    @CsvBindByName
    public final String name;

    protected Identity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract Map<String, Object> asMap();
}
