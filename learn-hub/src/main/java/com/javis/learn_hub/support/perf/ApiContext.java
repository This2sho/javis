package com.javis.learn_hub.support.perf;

import java.util.ArrayList;
import java.util.List;

public class ApiContext {
    private final long startTime;
    private final List<MethodRecord> records = new ArrayList<>();

    public ApiContext(long startTime) {
        this.startTime = startTime;
    }

    public void add(String method, long time) {
        records.add(new MethodRecord(method, time));
    }

    public long totalTime() {
        return System.currentTimeMillis() - startTime;
    }

    public List<MethodRecord> getRecords() {
        return records;
    }
}
