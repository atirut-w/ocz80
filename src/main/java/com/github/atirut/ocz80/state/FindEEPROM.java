package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;
import li.cil.oc.api.machine.Machine;

public class FindEEPROM extends State {
    public FindEEPROM(Arch arch, Machine machine) {
        super(arch, machine);
    }

    public boolean isInitialized() {
        return false;
    }

    public Transition runThreaded() {
        return new Transition(new Run(arch, machine), SLEEP_ZERO);
    }

    public void close() {
    }
}
