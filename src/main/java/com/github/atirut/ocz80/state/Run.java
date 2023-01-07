package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;
import com.github.atirut.ocz80.OCZ80;

import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;

public class Run extends State {
    private static ExecutionResult SYNCHRONOUS_CALL = new ExecutionResult.SynchronizedCall();
    private byte[] eeprom;

    public Run(Arch arch, Machine machine, byte[] eeprom) {
        super(arch, machine);
        this.eeprom = eeprom;
    }

    public boolean isInitialized() {
        return true;
    }

    public Transition runThreaded() {
        OCZ80.logger.info("runThreaded");
        return new Transition(this, SLEEP_ZERO);
    }

    public void close() {

    }
}
