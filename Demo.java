public class Demo {
    private static final byte[] machine_code = new byte[] {
            // prologue
            (byte) 0x55,                                            // push rbp
            (byte) 0x48, (byte) 0x89, (byte) 0xe5,                  // mov rbp, rsp

            // make room for stack-allocated buffer
            (byte) 0x48, (byte) 0x83, (byte) 0xec, (byte) 0x20,     // sub rsp, 0x20

            // load "Hello Machine!\n" into the buffer
            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe0,     // mov [rbp-32], 'H'
            (byte) 0x48, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe1,     // mov [rbp-31], 'e'
            (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe2,     // mov [rbp-30], 'l'
            (byte) 0x6c, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe3,     // mov [rbp-29], 'l'
            (byte) 0x6c, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe4,     // mov [rbp-28], 'o'
            (byte) 0x6f, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe5,     // mov [rbp-27], ' '
            (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe6,     // mov [rbp-26], 'M'
            (byte) 0x4d, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe7,     // mov [rbp-25], 'a'
            (byte) 0x61, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe8,     // mov [rbp-24], 'c'
            (byte) 0x63, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xe9,     // mov [rbp-23], 'h'
            (byte) 0x68, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xea,     // mov [rbp-22], 'i'
            (byte) 0x69, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xeb,     // mov [rbp-21], 'n'
            (byte) 0x6e, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xec,     // mov [rbp-20], 'e'
            (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xed,     // mov [rbp-19], '!'
            (byte) 0x21, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x48, (byte) 0xc7, (byte) 0x45, (byte) 0xee,     // mov [rbp-19], '\n'
            (byte) 0x0a, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            // load syscall (1 = write)
            (byte) 0x48, (byte) 0xc7, (byte) 0xc0, (byte) 0x01,     // mov rax, 0x01
            (byte) 0x00, (byte) 0x00, (byte) 0x00,

            // load file descriptor (stdout = 1)
            (byte) 0x48, (byte) 0xc7, (byte) 0xc7, (byte) 0x01,     // mov rdi, 0x01
            (byte) 0x00, (byte) 0x00, (byte) 0x00,

            // load buffer address
            (byte) 0x48, (byte) 0x8d, (byte) 0x75, (byte) 0xe0,     // lea rsi, [rbp-32]

            // load how many bytes to write
            (byte) 0x48, (byte) 0xc7, (byte) 0xc2, (byte) 0x0f,     // mov rdx, 14
            (byte) 0x00, (byte) 0x00, (byte) 0x00,

            // call write
            (byte) 0x0f, (byte) 0x05,                               // syscall

            // epilogue
            (byte) 0x48, (byte) 0x89, (byte) 0xec,                  // mov rsp, rbp
            (byte) 0x5d,                                            // pop rbp
            (byte) 0xc3                                             // ret
    };

    public static void main(String[] args) throws Throwable {
        NativeCodeExecutor.install_code(machine_code);
        NativeCodeExecutor.execute();
    }
}