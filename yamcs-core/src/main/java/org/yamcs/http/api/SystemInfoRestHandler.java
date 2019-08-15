package org.yamcs.http.api;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.yamcs.YamcsServer;
import org.yamcs.YamcsVersion;
import org.yamcs.http.ForbiddenException;
import org.yamcs.http.HttpException;
import org.yamcs.http.InternalServerErrorException;
import org.yamcs.protobuf.RootDirectory;
import org.yamcs.protobuf.SystemInfo;

public class SystemInfoRestHandler extends RestHandler {

    @Route(rpc = "YamcsManagement.GetSystemInfo")
    public void getSystemInfo(RestRequest req) throws HttpException {
        if (!req.getUser().isSuperuser()) {
            throw new ForbiddenException("Access is limited to superusers");
        }

        YamcsServer yamcs = YamcsServer.getServer();

        SystemInfo.Builder b = SystemInfo.newBuilder()
                .setYamcsVersion(YamcsVersion.VERSION)
                .setRevision(YamcsVersion.REVISION)
                .setServerId(yamcs.getServerId());

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        b.setUptime(runtime.getUptime());
        b.setJvm(runtime.getVmName() + " " + runtime.getVmVersion() + " (" + runtime.getVmVendor() + ")");
        b.setWorkingDirectory(new File("").getAbsolutePath());
        b.setConfigDirectory(yamcs.getConfigDirectory().toAbsolutePath().toString());
        b.setDataDirectory(yamcs.getDataDirectory().toAbsolutePath().toString());
        b.setCacheDirectory(yamcs.getCacheDirectory().toAbsolutePath().toString());
        b.setJvmThreadCount(Thread.activeCount());

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memory.getHeapMemoryUsage();
        b.setHeapMemory(heap.getCommitted());
        b.setUsedHeapMemory(heap.getUsed());
        if (heap.getMax() != -1) {
            b.setMaxHeapMemory(heap.getMax());
        }
        MemoryUsage nonheap = memory.getNonHeapMemoryUsage();
        b.setNonHeapMemory(nonheap.getCommitted());
        b.setUsedNonHeapMemory(nonheap.getUsed());
        if (nonheap.getMax() != -1) {
            b.setMaxNonHeapMemory(nonheap.getMax());
        }

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        b.setOs(os.getName() + " " + os.getVersion());
        b.setArch(os.getArch());
        b.setAvailableProcessors(os.getAvailableProcessors());
        b.setLoadAverage(os.getSystemLoadAverage());

        try {
            for (Path root : FileSystems.getDefault().getRootDirectories()) {
                FileStore store = Files.getFileStore(root);
                b.addRootDirectories(RootDirectory.newBuilder()
                        .setDirectory(root.toString())
                        .setType(store.type())
                        .setTotalSpace(store.getTotalSpace())
                        .setUnallocatedSpace(store.getUnallocatedSpace())
                        .setUsableSpace(store.getUsableSpace()));
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }

        completeOK(req, b.build());
    }
}
