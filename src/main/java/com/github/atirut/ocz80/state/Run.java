package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;
import com.github.atirut.ocz80.OCZ80;

import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;

public class Run extends State {
    public Run(Arch arch, Machine machine) {
        super(arch, machine);
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
