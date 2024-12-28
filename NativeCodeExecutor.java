import java.lang.foreign.*;

public class NativeCodeExecutor {
    public static final Arena arena = Arena.global();
    private static final int page_size = 4096;
    private static MemorySegment sigaction_struct;
    private static final int trap_signal = 0x12; // SIGCONT

    private static long time() {
        return System.currentTimeMillis();
    }

    private static void echo(String tag, long start, long end) {
        System.out.printf("time[%s]: %d ms\n", tag, end - start);
    }

    static void perror(String prefix) throws Throwable {
        MemorySegment addr = Linker.nativeLinker().defaultLookup().findOrThrow("perror");
        var descriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
        var handle = Linker.nativeLinker().downcallHandle(addr, descriptor);
        MemorySegment prefix_addr = arena.allocateFrom("perror[%s]".formatted(prefix));
        handle.invokeExact(prefix_addr);
    }

    // void* mmap(void* addr, size_t len, int prot, int flags, int fd, size_t offset)
    private static MemorySegment mmap(int len) throws Throwable {
        MemorySegment addr = Linker.nativeLinker().defaultLookup().findOrThrow("mmap");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG
        );

        var handle = Linker.nativeLinker().downcallHandle(addr, descriptor);
        int prot = 0x02 | 0x04;
        int flags = 0x02 | 0x20;
        addr = (MemorySegment)handle.invokeExact((long)0, (long)len, prot, flags, -1, (long)0);
        if (addr.address() == -1) {
            System.out.println("mmap failed!");
            perror("mmap");
            System.exit(-1);
        }
        return addr.reinterpret(len);
    }

    private static void init_sigaction(long address) {
        MemoryLayout sigaction_layout = MemoryLayout.structLayout(
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG
        );
        sigaction_struct = arena.allocate(sigaction_layout);
        sigaction_struct.set(ValueLayout.JAVA_LONG,0, address);
        sigaction_struct.set(ValueLayout.JAVA_LONG, 8, 0);
        sigaction_struct.set(ValueLayout.JAVA_INT, 16, 0);
        sigaction_struct.set(ValueLayout.JAVA_INT, 20, 0);
        sigaction_struct.set(ValueLayout.JAVA_LONG, 24, 0);
    }

    private static void install_signal_handler(long address) throws Throwable {
        MemorySegment addr = Linker.nativeLinker().defaultLookup().findOrThrow("sigaction");
        var descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_LONG
        );
        var handle = Linker.nativeLinker().downcallHandle(addr, descriptor);
        int ret = (int)handle.invokeExact(trap_signal, sigaction_struct.address(), (long)0);
        if (ret == -1) {
            System.out.println("sigaction failed!");
            perror("sigaction");
            System.exit(-1);
        }
    }

    public static void install_code(byte[] code) throws Throwable {
        int map_size = code.length + (code.length % page_size);
        map_size = Math.max(map_size, page_size);
        MemorySegment mapped_addr = mmap(map_size);

        // Copy machine code into mapped region.
        for (int i = 0; i < code.length; i++) {
            mapped_addr.set(ValueLayout.JAVA_BYTE, i, code[i]);
        }

        init_sigaction(mapped_addr.address());
        install_signal_handler(mapped_addr.address());
    }

    // int kill(pid_t pid, int sig)
    public static void execute() throws Throwable {
        MemorySegment addr = Linker.nativeLinker().defaultLookup().findOrThrow("kill");
        var descriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT);
        var handle = Linker.nativeLinker().downcallHandle(addr, descriptor);
        long pid = ProcessHandle.current().pid();
        int ret = (int)handle.invokeExact(pid, trap_signal);
        if (ret == -1) {
            System.out.println("kill failed!");
            perror("kill");
            System.exit(-1);
        }
    }
}