/*
 * Copyright (c) 2026, Adel Noureddine
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

package org.noureddine.joularjx.cpu;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.WString;
import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Cpu} implementation for Joular Core shared ring buffer.
 */
public class JoularCoreRingBufferCpu implements Cpu {

    private static final Logger logger = JoularJXLogging.getLogger();
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final int ENTRY_SIZE = 40; // 5 * f64 (8 bytes each)
    private static final int BUFFER_SIZE = 5;
    private static final int FILE_SIZE = 8 + BUFFER_SIZE * ENTRY_SIZE;

    private final String path;
    private ByteBuffer buffer;
    private HANDLE mappingHandle;
    private Pointer mappingPointer;
    private boolean initialized;

    interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        int FILE_MAP_READ = 0x0004;

        HANDLE OpenFileMappingW(int desiredAccess, boolean inheritHandle, WString name);

        Pointer MapViewOfFile(HANDLE hFileMappingObject, int dwDesiredAccess, int dwFileOffsetHigh, int dwFileOffsetLow,
                              int dwNumberOfBytesToMap);

        boolean UnmapViewOfFile(Pointer lpBaseAddress);

        boolean CloseHandle(HANDLE hObject);
    }

    /**
     * JNA handle type for Windows kernel objects used by {@link Kernel32}.
     */
    public static class HANDLE extends PointerType {
        /**
         * Creates an empty handle instance for JNA to populate.
         */
        public HANDLE() {
            super();
        }
    }

    /**
     * Creates a new Joular Core ring buffer CPU monitor instance.
     *
     * @param path ring buffer path or mapping name
     */
    public JoularCoreRingBufferCpu(final String path) {
        if (path == null || path.isBlank()) {
            logger.severe("Can't start because of missing Joular Core ring buffer path. Set it in config.properties under the '"
                    + AgentProperties.JOULAR_CORE_RINGBUFFER_PATH_PROPERTY + "' key.");
            System.exit(1);
        }
        this.path = path;
    }

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }

        try {
            logger.log(Level.INFO, () -> "Initializing Joular Core ring buffer source: " + path);
            if (IS_WINDOWS) {
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    mapFile(path);
                } else {
                    mapWindowsSharedMemory(path);
                }
            } else {
                mapFile(path);
            }

            initialized = true;
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Can't initialize Joular Core ring buffer (path: " + path + "). Exiting...");
            logger.throwing(getClass().getName(), "initialize", exception);
            System.exit(1);
        }
    }

    private void mapFile(String filePath) throws Exception {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            this.buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, FILE_SIZE);
        }
        this.buffer.order(ByteOrder.nativeOrder());
    }

    private void mapWindowsSharedMemory(String mappingName) throws Exception {
        mappingHandle = Kernel32.INSTANCE.OpenFileMappingW(Kernel32.FILE_MAP_READ, false, new WString(mappingName));
        if (mappingHandle == null || Pointer.nativeValue(mappingHandle.getPointer()) == 0) {
            throw new Exception("Could not open file mapping '" + mappingName + "'");
        }

        mappingPointer = Kernel32.INSTANCE.MapViewOfFile(mappingHandle, Kernel32.FILE_MAP_READ, 0, 0, FILE_SIZE);
        if (mappingPointer == null || Pointer.nativeValue(mappingPointer) == 0) {
            Kernel32.INSTANCE.CloseHandle(mappingHandle);
            mappingHandle = null;
            throw new Exception("Could not map view of file '" + mappingName + "'");
        }

        this.buffer = mappingPointer.getByteBuffer(0, FILE_SIZE);
        this.buffer.order(ByteOrder.nativeOrder());
    }

    @Override
    public double getCurrentPower(final double cpuLoad) {
        if (buffer == null) {
            return 0;
        }

        long head = buffer.getLong(0);
        int idx = (int) ((head - 1) % BUFFER_SIZE);
        if (idx < 0) {
            return 0;
        }

        int offset = 8 + idx * ENTRY_SIZE;
        // RingBufferStruct: cpu_power, gpu_power, total_power, cpu_usage, pid_app_power
        return buffer.getDouble(offset);
    }

    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public double getMaxPower(final double cpuLoad) {
        return 0;
    }

    @Override
    public void close() {
        if (IS_WINDOWS) {
            if (mappingPointer != null) {
                Kernel32.INSTANCE.UnmapViewOfFile(mappingPointer);
                mappingPointer = null;
            }
            if (mappingHandle != null) {
                Kernel32.INSTANCE.CloseHandle(mappingHandle);
                mappingHandle = null;
            }
        }
        buffer = null;
        initialized = false;
    }
}
