package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;
import com.github.atirut.ocz80.OCZ80;

import li.cil.oc.api.machine.Machine;

public class Run extends State {
    private static int KIBIBYTE = 1024;
    private static int PAGESIZE = 4 * KIBIBYTE;

    private byte[] eeprom;

    public Run(Arch arch, Machine machine, byte[] eeprom) {
        super(arch, machine);
        this.eeprom = new byte[PAGESIZE];
        System.arraycopy(eeprom, 0, this.eeprom, 0, Math.min(this.eeprom.length, eeprom.length));
    }

    public boolean isInitialized() {
        return true;
    }

    public Transition runThreaded() {
        return new Transition(this, SLEEP_ZERO);
    }

    public void close() {

    }
}
