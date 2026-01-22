import java.lang.foreign.*;

public class NativeCodeExecutor {
    public static final Arena arena = Arena.global();
    private static final int page_size = 4096;

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
            perror("mmap");
            System.exit(-1);
        }
        return addr.reinterpret(len);
    }

    public static MemorySegment map_code(byte[] code) throws Throwable {
        int map_size = code.length + (code.length % page_size);
        MemorySegment mapped_addr = mmap(map_size);

        // Copy machine code into mapped region.
        for (int i = 0; i < code.length; i++) {
            mapped_addr.set(ValueLayout.JAVA_BYTE, i, code[i]);
        }

        return mapped_addr;
    }

    public static void execute(MemorySegment addr) throws Throwable {
        var descriptor = FunctionDescriptor.ofVoid();
        var handle = Linker.nativeLinker().downcallHandle(addr, descriptor);
        handle.invokeExact();
    }
}
