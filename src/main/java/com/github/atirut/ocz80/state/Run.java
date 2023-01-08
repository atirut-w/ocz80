package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;
import com.github.atirut.ocz80.OCZ80;

import li.cil.oc.api.machine.Machine;

public class Run extends State {
    private static int KIBIBYTE = 1024;
    private static int PAGESIZE = 4 * KIBIBYTE;

    private byte[] eeprom;
    private byte[][] ram;
    private byte[] mmap;

    private byte[] main = new byte[8];
    private byte[] alternate = new byte[8];

    private short pc;
    private short sp;
    private byte flags;
    private boolean running = true;

    public Run(Arch arch, Machine machine, byte[] eeprom) {
        super(arch, machine);
        this.eeprom = new byte[PAGESIZE];
        System.arraycopy(eeprom, 0, this.eeprom, 0, Math.min(this.eeprom.length, eeprom.length));
        ram = new byte[arch.memorySize / PAGESIZE][PAGESIZE];
        mmap = new byte[8];
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
                            case 6:
                                main[op.y] = fetch();
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
                    case 3:
                        switch (op.z) {
                            case 3:
                                switch (op.y) {
                                    case 2:
                                        out(fetch(), main[7]);
                                        break;
                                }
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

    private short readRegisterPair(int pair, boolean af) {
        if (pair < 3) {
            return (short)((main[pair * 2] << 8) | main[(pair * 2) + 1]);
        } else {
            if (!af) {
                return sp;
            } else {
                return (short)((main[7] << 8) | flags);
            }
        }
    }

    private byte fetch() {
        return read(pc++);
    }

    private byte read(short address) {
        if (mmap[address >> 12] == 0) {
            return eeprom[address & 0xfff];
        }
        else {
            if (mmap[address >> 12] - 1 < ram.length){
                return ram[mmap[address >> 12] - 1][address & 0xfff];
            }
            return 0;
        }
    }

    private void out(short address, byte data) {
        OCZ80.logger.info(String.format("I/O out at $%04x: $%02x", (int)address, (int)data));
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
