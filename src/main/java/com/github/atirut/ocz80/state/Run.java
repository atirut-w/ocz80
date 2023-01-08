package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;
import com.github.atirut.ocz80.OCZ80;

import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;

public class Run extends State {
    private static int KIBIBYTE = 1024;
    private static int PAGESIZE = 4 * KIBIBYTE;

    private static byte FLAG_C = 0x01;
    private static byte FLAG_N = 0x02;
    private static byte FLAG_PV = 0x04;

    private static byte FLAG_H = 0x10;
    private static byte FLAG_Z = 0x40;
    private static byte FLAG_S = (byte)0x80;

    private byte[] eeprom;
    private byte[][] ram;
    private byte[] mmap;

    private byte[] main = new byte[8];
    private byte[] alternate = new byte[8];

    private char pc;
    private char sp;
    private byte flags;
    private boolean running = true;

    private String logMessage = new String();
    private String crashMessage = new String();

    public Run(Arch arch, Machine machine, byte[] eeprom) {
        super(arch, machine);
        this.eeprom = new byte[PAGESIZE];
        System.arraycopy(eeprom, 0, this.eeprom, 0, Math.min(this.eeprom.length, eeprom.length));
        ram = new byte[arch.memorySize / PAGESIZE][PAGESIZE];
        mmap = new byte[16];
    }

    public boolean isInitialized() {
        return true;
    }

    public Transition runThreaded() {
        if (!running) {
            return new Transition(this, SLEEP_ZERO);
        }

        byte opcode = fetch();
        switch (opcode) {
            default: // Unprefixed opcodes
                Instruction op = new Instruction(opcode);
                switch (op.x) {
                    case 0:
                        switch (op.z) {
                            case 1:
                                if (op.q == 0) {
                                    writeRegisterPair(fetchShort(), op.p, false);
                                }
                                break;
                            case 2:
                                if (op.q == 0) {
                                    switch (op.p) {
                                        case 2:
                                            char address = fetchShort();
                                            write(address, main[5]);
                                            write((char)(address + 1), main[4]);
                                            break;
                                        case 3:
                                            write(fetchShort(), main[7]);
                                            break;
                                    }
                                } else {
                                    switch (op.p) {
                                        case 0:
                                            main[7] = read(readRegisterPair(0, false));
                                            break;
                                        case 1:
                                            main[7] = read(readRegisterPair(1, false));
                                            break;
                                        case 3:
                                            main[7] = read(fetchShort());
                                            break;
                                    }
                                }

                                break;
                            case 3:
                                if (op.q == 0) {
                                    char old = readRegisterPair(op.p, false);
                                    writeRegisterPair((char)(old + 1), op.p, false);
                                } else {
                                    char old = readRegisterPair(op.p, false);
                                    writeRegisterPair((char)(old - 1), op.p, false);
                                }

                                break;
                            case 6:
                                writeRegister(op.y, fetch());
                                break;
                        }
                        break;
                    case 1:
                        if (op.z == 6 && op.y == 6) {
                            running = false;
                            OCZ80.logger.info(String.format("CPU halted at $%04x", (int)(pc - 1)));
                            OCZ80.logger.info(String.format("AF = $%04x", (int)readRegisterPair(3, true)));
                            OCZ80.logger.info(String.format("BC = $%04x", (int)readRegisterPair(0, true)));
                            OCZ80.logger.info(String.format("DE = $%04x", (int)readRegisterPair(1, true)));
                            OCZ80.logger.info(String.format("HL = $%04x", (int)readRegisterPair(2, true)));
                        }
                        
                        break;
                    case 2:
                        alu(op.y, readRegister(op.z));
                        break;
                    case 3:
                        switch (op.z) {
                            case 1:
                                if (op.q == 0) {
                                    writeRegisterPair(pop(), op.p, true);
                                } else {
                                    switch (op.p) {
                                        case 0:
                                            pc = pop();
                                            break;
                                        case 3:
                                            sp = readRegisterPair(2, false);
                                            break;
                                    }
                                }

                                break;
                            case 2:
                                if (conditional(op.y)) {
                                    pc = fetchShort();
                                }
                                break;
                            case 3:
                                switch (op.y) {
                                    case 0:
                                        pc = fetchShort();
                                        break;
                                    case 2:
                                        return out((char)fetch(), readRegister(7));
                                    case 4:
                                        char onstack = pop();
                                        push(readRegisterPair(2, false));
                                        writeRegisterPair(onstack, 2, false);
                                        break;
                                    case 5:
                                        char de = readRegisterPair(1, false);
                                        writeRegisterPair(readRegisterPair(2, false), 1, false);
                                        writeRegisterPair(de, 2, false);
                                }
                                break;
                            case 5:
                                if (op.q == 0) {
                                    push(readRegisterPair(op.p, true));
                                } else {
                                    switch (op.p) {
                                        case 0:
                                            push((char)(pc + 2));
                                            pc = fetchShort();
                                            break;
                                    }
                                }

                                break;
                            case 6:
                                alu(op.y, fetch());
                                break;
                        }
                        break;
                }
                break;
        }

        return new Transition(this, SLEEP_ZERO);
    }

    public void close() {

    }

    private boolean conditional(int condition) {
        switch (condition) {
            case 0:
                return (flags & FLAG_Z) == 0;
            case 1:
                return (flags & FLAG_Z) > 0;
            case 2:
                return (flags & FLAG_C) == 0;
            case 3:
                return (flags & FLAG_C) > 0;
            case 4:
                return (flags & FLAG_PV) == 0;
            case 5:
                return (flags & FLAG_PV) > 0;
            case 6:
                return (flags & FLAG_S) == 0;
            case 7:
                return (flags & FLAG_S) > 0;
        }
        return false;
    }

    private void alu(int op, byte operand) {
        // OCZ80.logger.info("ALU operation " + op + " on " + operand);

        switch (op) {
            case 7:
                // TODO: Signed mode

                if (main[7] == operand) {
                    flags |= FLAG_Z;
                } else {
                    flags &= ~FLAG_Z;
                }

                if (main[7] < operand) {
                    flags |= FLAG_C;
                } else {
                    flags &= ~FLAG_C;
                }

                break;
        }
    }

    private void push(char data) {
        sp -= 2;
        write(sp, (byte)(data & 0xff));
        write((char)(sp + 1), (byte)(data >> 8));
    }

    private char pop() {
        byte lsb = read(sp);
        char data = (char)((read((char)(sp + 1)) << 8) | lsb);
        sp += 2;
        return data;
    }

    private byte readRegister(int register) {
        if (register == 6) {
            return read(readRegisterPair(2, false));
        } else {
            return main[register];
        }
    }

    private void writeRegister(int register, byte data) {
        if (register == 6) {
            // return read(readRegisterPair(2, false));
            write(readRegisterPair(2, false), data);
        } else {
            main[register] = data;
        }
    }

    private char readRegisterPair(int pair, boolean af) {
        if (pair < 3) {
            return (char)((main[pair * 2] << 8) | main[(pair * 2) + 1]);
        } else {
            if (!af) {
                return sp;
            } else {
                return (char)((main[7] << 8) | flags);
            }
        }
    }

    private void writeRegisterPair(char data, int pair, boolean af) {
        if (pair < 3) {
            main[pair * 2] = (byte)(data >> 8);
            main[(pair * 2) + 1] = (byte)data;
        } else {
            if (!af) {
                sp = data;
            } else {
                main[7] = (byte)(data >> 8);
                flags = (byte)data;
            }
        }
    }

    private byte fetch() {
        return read(pc++);
    }

    private char fetchShort() {
        byte lsb = fetch();
        return (char)((fetch() << 8) | lsb);
    }

    private byte read(char address) {
        if (mmap[address >> 12] == 0) {
            return eeprom[address & 0xfff];
        } else {
            if (mmap[address >> 12] - 1 < ram.length) {
                return ram[mmap[address >> 12] - 1][address & 0xfff];
            }
            return 0;
        }
    }

    private void write(char address, byte data) {
        if (mmap[address >> 12] == 0) {
            return;
        } else if (mmap[address >> 12] - 1 < ram.length) {
            ram[mmap[address >> 12] - 1][address & 0xfff] = data;
        }
    }

    private Transition out(char address, byte data) {
        // OCZ80.logger.info(String.format("I/O out at $%04x: $%02x", (int)address, (int)data));

        switch (address) {
            case 0:
                if (data == 0) {
                    OCZ80.logger.info(logMessage);
                    logMessage = new String();
                } else {
                    logMessage += (char)data;
                }
                break;
            case 1:
                return new Transition(null, new ExecutionResult.Error(String.format("$%02x", (int)data & 0xff)));
            case 2:
                if (data == 0) {
                    return new Transition(null, new ExecutionResult.Error(crashMessage));
                }
                crashMessage += (char)data;
                break;
            default:
                if ((address >> 4) == 0x1) {
                    mmap[address & 0xf] = data;
                }

                break;
        }

        return new Transition(this, SLEEP_ZERO);
    }

    private class Instruction {
        final byte x, y, z, p, q;

        public Instruction(byte opcode) {
            x = (byte)((opcode >> 6) & 0x03);
            y = (byte)((opcode >> 3) & 0x07);
            z = (byte)(opcode & 0x07);

            p = (byte)(y >> 1);
            q = (byte)(y % 2);
        }
    }
}
