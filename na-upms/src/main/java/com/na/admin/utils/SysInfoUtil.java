package com.na.admin.utils;

import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.*;
import oshi.util.Util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author pg
 * @date 2025年11月06日
 */

public class SysInfoUtil {

    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public static Map<String, Object> collect() {
        Map<String, Object> result = new LinkedHashMap<>();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        result.put("system", systemInfo(os));
        result.put("cpu", cpuInfo(hal.getProcessor()));
        result.put("memory", memoryInfo(hal.getMemory()));
        result.put("disk", diskInfo(os));
        result.put("network", networkInfo(hal.getNetworkIFs()));
        result.put("jvm", jvmInfo());

        // 1️⃣ 系统基本信息
        result.put("system2", getSystemInfo());

        // 2️⃣ JVM 信息
        result.put("jvm2", getJvmInfo());

        // 3️⃣ 内存信息
//        result.put("memory2", getMemoryInfo());

        // 4️⃣ CPU 信息
//        result.put("cpu2", getCpuInfo());

        // 5️⃣ 磁盘信息
//        result.put("disk2", getDiskInfo());

        // 6️⃣ 网络 & 用户信息
        result.put("runtime2", getRuntimeInfo());

        return result;
    }

    // ================= 系统 =================
    private static Map<String, Object> systemInfo(OperatingSystem os) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("os", os.getFamily());
        map.put("version", os.getVersionInfo().toString());
        map.put("bitness", os.getBitness());
        map.put("processCount", os.getProcessCount());
        map.put("threadCount", os.getThreadCount());
        return map;
    }

    // ================= CPU =================
    private static Map<String, Object> cpuInfo(CentralProcessor cpu) {
        Map<String, Object> map = new LinkedHashMap<>();

        long[] prevTicks = cpu.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] ticks = cpu.getSystemCpuLoadTicks();

        map.put("name", cpu.getProcessorIdentifier().getName());
        map.put("physicalCores", cpu.getPhysicalProcessorCount());
        map.put("logicalCores", cpu.getLogicalProcessorCount());
        map.put("systemUsage", DF.format(cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100) + "%");
        map.put("loadAverage", Arrays.toString(cpu.getSystemLoadAverage(3)));

        return map;
    }

    // ================= 内存 =================
    private static Map<String, Object> memoryInfo(GlobalMemory memory) {
        Map<String, Object> map = new LinkedHashMap<>();

        long total = memory.getTotal();
        long available = memory.getAvailable();

        map.put("total", formatSize(total));
        map.put("used", formatSize(total - available));
        map.put("available", formatSize(available));
        map.put("usage", DF.format((double) (total - available) / total * 100) + "%");

        return map;
    }

    // ================= 磁盘 =================
    private static List<Map<String, Object>> diskInfo(OperatingSystem os) {
        List<Map<String, Object>> list = new ArrayList<>();

        for (OSFileStore fs : os.getFileSystem().getFileStores()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", fs.getName());
            map.put("mount", fs.getMount());
            map.put("type", fs.getType());
            map.put("total", formatSize(fs.getTotalSpace()));
            map.put("usable", formatSize(fs.getUsableSpace()));
            map.put("usage", DF.format(
                    (double) (fs.getTotalSpace() - fs.getUsableSpace()) / fs.getTotalSpace() * 100
            ) + "%");
            list.add(map);
        }
        return list;
    }

    // ================= 网络 =================
    private static List<Map<String, Object>> networkInfo(List<NetworkIF> ifs) {
        List<Map<String, Object>> list = new ArrayList<>();

        for (NetworkIF net : ifs) {
            net.updateAttributes();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", net.getName());
            map.put("displayName", net.getDisplayName());
            map.put("mac", net.getMacaddr());
            map.put("ipv4", Arrays.toString(net.getIPv4addr()));
            map.put("rxBytes", formatSize(net.getBytesRecv()));
            map.put("txBytes", formatSize(net.getBytesSent()));
            list.add(map);
        }
        return list;
    }

    // ================= JVM =================
    private static Map<String, Object> jvmInfo() {
        Map<String, Object> map = new LinkedHashMap<>();

        Runtime rt = Runtime.getRuntime();

        map.put("javaVersion", System.getProperty("java.version"));
        map.put("javaVendor", System.getProperty("java.vendor"));
        map.put("javaHome", System.getProperty("java.home"));
        map.put("heapMax", formatSize(rt.maxMemory()));
        map.put("heapTotal", formatSize(rt.totalMemory()));
        map.put("heapFree", formatSize(rt.freeMemory()));

        return map;
    }

    // ================= 工具 =================
    private static String formatSize(long size) {
        if (size <= 0) return "0";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return DF.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    private static Map<String, Object> getSystemInfo() {
        Map<String, Object> map = new LinkedHashMap<>();

        Properties props = System.getProperties();
        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        map.put("osName", props.getProperty("os.name"));
        map.put("osVersion", props.getProperty("os.version"));
        map.put("osArch", props.getProperty("os.arch"));
        map.put("availableProcessors", osBean.getAvailableProcessors());
        map.put("systemLoadAverage", osBean.getSystemLoadAverage());

        return map;
    }

    private static Map<String, Object> getJvmInfo() {
        Map<String, Object> map = new LinkedHashMap<>();

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        map.put("javaVersion", System.getProperty("java.version"));
        map.put("javaVendor", System.getProperty("java.vendor"));
        map.put("javaHome", System.getProperty("java.home"));
        map.put("jvmName", runtimeBean.getVmName());
        map.put("jvmVersion", runtimeBean.getVmVersion());
        map.put("startTime", new Date(runtimeBean.getStartTime()));
        map.put("uptime(ms)", runtimeBean.getUptime());
        map.put("inputArguments", runtimeBean.getInputArguments());

        return map;
    }

    private static Map<String, Object> getMemoryInfo() {
        Map<String, Object> map = new LinkedHashMap<>();

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();

        map.put("heapInit", heap.getInit());
        map.put("heapUsed", heap.getUsed());
        map.put("heapMax", heap.getMax());

        map.put("nonHeapInit", nonHeap.getInit());
        map.put("nonHeapUsed", nonHeap.getUsed());
        map.put("nonHeapMax", nonHeap.getMax());

        return map;
    }

    private static Map<String, Object> getCpuInfo() {
        Map<String, Object> map = new LinkedHashMap<>();

        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        map.put("processCpuLoad", osBean.getProcessCpuLoad());
        map.put("systemCpuLoad", osBean.getSystemCpuLoad());
        map.put("systemLoadAverage", osBean.getSystemLoadAverage());

        return map;
    }

    private static List<Map<String, Object>> getDiskInfo() {
        List<Map<String, Object>> list = new ArrayList<>();

        for (File root : File.listRoots()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("path", root.getAbsolutePath());
            map.put("total", root.getTotalSpace());
            map.put("free", root.getFreeSpace());
            map.put("usable", root.getUsableSpace());
            list.add(map);
        }
        return list;
    }

    private static Map<String, Object> getRuntimeInfo() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("userName", System.getProperty("user.name"));
        map.put("userDir", System.getProperty("user.dir"));
        map.put("tmpDir", System.getProperty("java.io.tmpdir"));
        map.put("fileEncoding", System.getProperty("file.encoding"));

        return map;
    }

}
