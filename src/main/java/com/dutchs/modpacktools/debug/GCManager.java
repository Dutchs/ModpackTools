package com.dutchs.modpacktools.debug;

import com.dutchs.modpacktools.ModpackTools;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GCManager {
    private static final Map<String, Long> gc = new HashMap<>();
    private static final Map<String, GCTimer> timers = new HashMap<>();

    public void Tick() {
        List<GarbageCollectorMXBean> gcMxBeanList = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcMxBean : gcMxBeanList) {
            Long current = gcMxBean.getCollectionCount();
            if (gc.containsKey(gcMxBean.getName())) {
                Long old = gc.getOrDefault(gcMxBean.getName(), current);
                if (current > old) {
                    gc.replace(gcMxBean.getName(), current);
                    ModpackTools.logInfo(String.format("GC %s took: %dms", gcMxBean.getName(), current - old));
                }
                timers.get(gcMxBean.getName()).logGCDuration(current - old);
            } else {
                gc.put(gcMxBean.getName(), current);
                timers.put(gcMxBean.getName(), new GCTimer(gcMxBean.getName()));
            }
        }
    }

    public Collection<GCTimer> getTimers() {
        return timers.values();
    }
}
