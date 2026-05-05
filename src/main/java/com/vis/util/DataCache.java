package com.vis.util;

import com.vis.model.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataCache {

    private static DataCache instance;

    // ── CACHED DATA ───────────────────────────────
    public List<Vehicle>         vehicles;
    public List<Customer>        customers;
    public List<Violation>       violations;
    public List<InsuranceRecord> insurance;
    public List<PoliceReport>    reports;
    public List<ServiceRecord>   services;

    // Flag — true when preload is complete
    private final AtomicBoolean ready =
            new AtomicBoolean(false);

    private DataCache() {}

    public static DataCache getInstance() {
        if (instance == null)
            instance = new DataCache();
        return instance;
    }

    public boolean isReady() {
        return ready.get();
    }

    public void markReady() {
        ready.set(true);
    }

    // Call this to invalidate after any write operation
    public void invalidate() {
        ready.set(false);
        vehicles   = null;
        customers  = null;
        violations = null;
        insurance  = null;
        reports    = null;
        services   = null;
    }
}