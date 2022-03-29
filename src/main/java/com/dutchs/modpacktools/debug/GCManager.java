package com.dutchs.modpacktools.debug;

import com.dutchs.modpacktools.ModpackTools;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GCManager {
    private static final Map<String, Long> GC = new HashMap<>();
    private static final Map<String, GCTimer> Timers = new HashMap<>();

    public void Tick() {
        List<GarbageCollectorMXBean> gcMxBeanList = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcMxBean : gcMxBeanList) {
            Long current = gcMxBean.getCollectionCount();
            if (GC.containsKey(gcMxBean.getName())) {
                Long old = GC.getOrDefault(gcMxBean.getName(), current);
                if (current > old) {
                    GC.replace(gcMxBean.getName(), current);

//                    ModpackTools.logInfo(String.format("GC %s took: %dms", gcMxBean.getName(), current - old));
                }
                Timers.get(gcMxBean.getName()).logGCDuration(current - old);
            } else {
                GC.put(gcMxBean.getName(), current);
                Timers.put(gcMxBean.getName(), new GCTimer(gcMxBean.getName()));
            }
        }
    }

    public Collection<GCTimer> getTimers() {
        return Timers.values();
    }
}
