package org.base.autoclick;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.java.Log;
import org.junit.Test;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;

/**
 * Created by liu on 2017/5/15.
 */

@Log
public class MonitorTest {

    static final long KB = 1 << 10;

    static final long MB = 1 << 20;

    static final long GB = 1 << 30;

    private static String toString(long info) {
        StringBuilder infoBuilder = new StringBuilder();

        long gb = info / GB;

        if (gb > 0) {
            infoBuilder.append(gb).append("G ");
        }

        long mb = (info) / MB;
        if (mb > gb << 10 && mb > 0) {
            mb = (info - gb >> 10) / MB;
            infoBuilder.append(mb).append("M ");
        }

        long kb = (info) / KB;

        if (kb > mb << 10 && kb > 0) {
            kb = (info - mb >> 10) / KB;
            infoBuilder.append(kb).append("K");
        }

        return infoBuilder.toString();
    }

    private static void printOperatingSystemInfo() {
        OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();
        //相当于System.getProperty("os.name").
        System.out.println("系统名称:" + system.getName());
        //相当于System.getProperty("os.version").
        System.out.println("系统版本:" + system.getVersion());
        //相当于System.getProperty("os.arch").
        System.out.println("操作系统的架构:" + system.getArch());
        //相当于 Runtime.availableProcessors()
        System.out.println("可用的内核数:" + system.getAvailableProcessors());

        if (isSunOsMBean(system)) {
            long totalPhysicalMemory = getLongFromOperatingSystem(system, "getTotalPhysicalMemorySize");
            long freePhysicalMemory = getLongFromOperatingSystem(system, "getFreePhysicalMemorySize");
            long usedPhysicalMemorySize = totalPhysicalMemory - freePhysicalMemory;

            System.out.println("总物理内存(M):" + toString(totalPhysicalMemory));
            System.out.println("已用物理内存(M):" + usedPhysicalMemorySize / MB);
            System.out.println("剩余物理内存(M):" + freePhysicalMemory / MB);

            long totalSwapSpaceSize = getLongFromOperatingSystem(system, "getTotalSwapSpaceSize");
            long freeSwapSpaceSize = getLongFromOperatingSystem(system, "getFreeSwapSpaceSize");
            long usedSwapSpaceSize = totalSwapSpaceSize - freeSwapSpaceSize;

            System.out.println("总交换空间(M):" + totalSwapSpaceSize / MB);
            System.out.println("已用交换空间(M):" + usedSwapSpaceSize / MB);
            System.out.println("剩余交换空间(M):" + freeSwapSpaceSize / MB);
        }
    }

    private static long getLongFromOperatingSystem(OperatingSystemMXBean operatingSystem, String methodName) {
        try {
            final Method method = operatingSystem.getClass().getMethod(methodName,
                (Class<?>[]) null);
            method.setAccessible(true);
            return (Long) method.invoke(operatingSystem, (Object[]) null);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            } else if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new IllegalStateException(e.getCause());
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void printCompilationInfo() {
        CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
        System.out.println("JIT编译器名称：" + compilation.getName());
        //判断jvm是否支持编译时间的监控
        if (compilation.isCompilationTimeMonitoringSupported()) {
            System.out.println("总编译时间：" + compilation.getTotalCompilationTime() + "秒");
        }
    }

    private static void printClassLoadingInfo() {
        ClassLoadingMXBean classLoad = ManagementFactory.getClassLoadingMXBean();
        System.out.println("已加载类总数：" + classLoad.getTotalLoadedClassCount());
        System.out.println("已加载当前类：" + classLoad.getLoadedClassCount());
        System.out.println("已卸载类总数：" + classLoad.getUnloadedClassCount());

    }

    private static void printRuntimeInfo() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        System.out.println("进程PID=" + runtime.getName().split("@")[0]);
        System.out.println("jvm规范名称:" + runtime.getSpecName());
        System.out.println("jvm规范运营商:" + runtime.getSpecVendor());
        System.out.println("jvm规范版本:" + runtime.getSpecVersion());
        //返回虚拟机在毫秒内的开始时间。该方法返回了虚拟机启动时的近似时间
        System.out.println("jvm启动时间（毫秒）:" + runtime.getStartTime());
        //相当于System.getProperties
        System.out.println("获取System.properties:" + runtime.getSystemProperties());
        System.out.println("jvm正常运行时间（毫秒）:" + runtime.getUptime());
        //相当于System.getProperty("java.vm.name").
        System.out.println("jvm名称:" + runtime.getVmName());
        //相当于System.getProperty("java.vm.vendor").
        System.out.println("jvm运营商:" + runtime.getVmVendor());
        //相当于System.getProperty("java.vm.version").
        System.out.println("jvm实现版本:" + runtime.getVmVersion());
        List<String> args = runtime.getInputArguments();
        if (args != null && !args.isEmpty()) {
            System.out.println("vm参数:");
            for (String arg : args) {
                System.out.println(arg);
            }
        }
        System.out.println("类路径:" + runtime.getClassPath());
        System.out.println("引导类路径:" + runtime.getBootClassPath());
        System.out.println("库路径:" + runtime.getLibraryPath());
    }

    private static void printMemoryManagerInfo() {
        List<MemoryManagerMXBean> managers = ManagementFactory.getMemoryManagerMXBeans();
        if (managers != null && !managers.isEmpty()) {
            for (MemoryManagerMXBean manager : managers) {
                System.out.println("vm内存管理器：名称=" + manager.getName() + ",管理的内存区="
                    + Arrays.deepToString(manager.getMemoryPoolNames()) + ",ObjectName=" + manager.getObjectName());
            }
        }
    }

    private static void printGarbageCollectorInfo() {
        List<GarbageCollectorMXBean> garbages = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean garbage : garbages) {
            System.out.println("垃圾收集器：名称=" + garbage.getName() + ",收集=" + garbage.getCollectionCount() + ",总花费时间="
                + garbage.getCollectionTime() + ",内存区名称=" + Arrays.deepToString(garbage.getMemoryPoolNames()));
        }
    }

    private static void printMemoryInfo() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage headMemory = memory.getHeapMemoryUsage();
        System.out.println("head堆:");
        System.out.println("\t初始(M):" + toString(headMemory.getInit()));
        System.out.println("\t最大(上限):" + toString(headMemory.getMax()));
        System.out.println("\t当前(已使用)" + toString(headMemory.getUsed()));
        System.out.println("\t提交的内存(已申请):" + toString(headMemory.getCommitted()));
        System.out.println("\t使用率:" + headMemory.getUsed() * 100 / headMemory.getCommitted() + "%");

        System.out.println("non-head非堆:");
        MemoryUsage nonheadMemory = memory.getNonHeapMemoryUsage();
        System.out.println("\t初始(M):" + nonheadMemory.getInit() / MB);
        System.out.println("\t最大(上限)(M):" + nonheadMemory.getMax() / MB);
        System.out.println("\t当前(已使用)(M):" + nonheadMemory.getUsed() / MB);
        System.out.println("\t提交的内存(已申请)(M):" + nonheadMemory.getCommitted() / MB);
        System.out.println("\t使用率:" + nonheadMemory.getUsed() * 100 / nonheadMemory.getCommitted() + "%");
    }

    private static void printMemoryPoolInfo() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        if (pools != null && !pools.isEmpty()) {
            for (MemoryPoolMXBean pool : pools) {
                //只打印一些各个内存区都有的属性，一些区的特殊属性，可看文档或百度
                //最大值，初始值，如果没有定义的话，返回-1，所以真正使用时，要注意
                System.out.println("vm内存区:\n\t名称=" + pool.getName() + "\n\t所属内存管理者=" + Arrays.deepToString(pool.getMemoryManagerNames())
                    + "\n\t ObjectName=" + pool.getObjectName() + "\n\t初始大小(M)=" + pool.getUsage().getInit() / MB
                    + "\n\t最大(上限)(M)=" + pool.getUsage().getMax() / MB
                    + "\n\t已用大小(M)=" + pool.getUsage().getUsed() / MB
                    + "\n\t已提交(已申请)(M)=" + pool.getUsage().getCommitted() / MB
                    + "\n\t使用率=" + (pool.getUsage().getUsed() * 100 / pool.getUsage().getCommitted()) + "%");

            }
        }
    }

    private static void printThreadInfo() {
        ThreadMXBean thread = ManagementFactory.getThreadMXBean();
        System.out.println("ObjectName=" + thread.getObjectName());
        System.out.println("仍活动的线程总数=" + thread.getThreadCount());
        System.out.println("峰值=" + thread.getPeakThreadCount());
        System.out.println("线程总数（被创建并执行过的线程总数）=" + thread.getTotalStartedThreadCount());
        System.out.println("当初仍活动的守护线程（daemonThread）总数=" + thread.getDaemonThreadCount());

        //检查是否有死锁的线程存在
        long[] deadlockedIds = thread.findDeadlockedThreads();
        if (deadlockedIds != null && deadlockedIds.length > 0) {
            ThreadInfo[] deadlockInfos = thread.getThreadInfo(deadlockedIds);
            System.out.println("死锁线程信息:");
            System.out.println("\t\t线程名称\t\t状态\t\t");
            for (ThreadInfo deadlockInfo : deadlockInfos) {
                System.out.println("\t\t" + deadlockInfo.getThreadName() + "\t\t" + deadlockInfo.getThreadState()
                    + "\t\t" + deadlockInfo.getBlockedTime() + "\t\t" + deadlockInfo.getWaitedTime()
                    + "\t\t" + deadlockInfo.getStackTrace().toString());
            }
        }
        long[] threadIds = thread.getAllThreadIds();
        if (threadIds != null && threadIds.length > 0) {
            ThreadInfo[] threadInfos = thread.getThreadInfo(threadIds);
            System.out.println("所有线程信息:");
            System.out.println("\t\t线程名称\t\t\t\t\t状态\t\t\t\t\t线程id");
            for (ThreadInfo threadInfo : threadInfos) {
                System.out.println("\t\t" + threadInfo.getThreadName() + "\t\t\t\t\t" + threadInfo.getThreadState()
                    + "\t\t\t\t\t" + threadInfo.getThreadId());
            }
        }

    }

    private static boolean isSunOsMBean(OperatingSystemMXBean operatingSystem) {
        final String className = operatingSystem.getClass().getName();
        return "com.sun.management.OperatingSystem".equals(className)
            || "com.sun.management.UnixOperatingSystem".equals(className);
    }

    @Test
    public void monitor() throws MonitorException, URISyntaxException, IOException, AttachNotSupportedException {

        MonitoredHost local = MonitoredHost.getMonitoredHost("localhost");

        Set<?> vmlist = new HashSet<Object>(local.activeVms());
        for (Object process : vmlist) {
            MonitoredVm monitoredVm = local.getMonitoredVm(new VmIdentifier("//" + process));
            String processname = MonitoredVmUtil.mainClass(monitoredVm, true);
            log.info("process id : " + process + ", process name : " + processname);

            VirtualMachine vm = null;

            List<AttachProvider> providers = AttachProvider.providers();
            log.info("provider size : " + providers.size());
            for (AttachProvider provider : providers) {
                try {
                    vm = provider.attachVirtualMachine(process.toString());
                    log.info("id : " + vm.id());
                    break;
                } catch (Exception e) {
                    log.severe(e.getMessage());
                    continue;
                } finally {
                    vm.detach();
                }
            }

        }

//        //打印系统信息
//        System.out.println("===========打印系统信息==========");
//        printOperatingSystemInfo();
//        //打印编译信息
//        System.out.println("===========打印编译信息==========");
//        printCompilationInfo();
//        //打印类加载信息
//        System.out.println("===========打印类加载信息==========");
//        printClassLoadingInfo();
//        //打印运行时信息
//        System.out.println("===========打印运行时信息==========");
//        printRuntimeInfo();
//        //打印内存管理器信息
//        System.out.println("===========打印内存管理器信息==========");
//        printMemoryManagerInfo();
//        //打印垃圾回收信息
//        System.out.println("===========打印垃圾回收信息==========");
//        printGarbageCollectorInfo();
//        //打印vm内存
//        System.out.println("===========打印vm内存信息==========");
        printMemoryInfo();
//        //打印vm各内存区信息
//        System.out.println("===========打印vm各内存区信息==========");
//        printMemoryPoolInfo();
//        //打印线程信息
//        System.out.println("===========打印线程==========");
//        printThreadInfo();

    }
}
