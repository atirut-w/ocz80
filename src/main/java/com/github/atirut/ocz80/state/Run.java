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

    private short pc;

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
        OCZ80.logger.info(String.format("$%04x: $%02x", pc, read(pc++)));

        return new Transition(this, SLEEP_ZERO);
    }

    public void close() {

    }

    private byte read(short address) {
        if (mmap[address >> 12] == 0) {
            return eeprom[address & 0xfff];
        }
        else {
            return ram[mmap[address >> 12] - 1][address & 0xfff];
        }
    }
}
