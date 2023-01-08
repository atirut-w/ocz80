package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;

import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;

public abstract class State {
    protected static ExecutionResult SLEEP_ZERO = new ExecutionResult.Sleep(0);
    
    protected final Arch arch;
    protected final Machine machine;

    protected State(Arch arch, Machine machine) {
        this.arch = arch;
        this.machine = machine;
    }

    public abstract boolean isInitialized();

    public abstract Transition runThreaded();

    public void runSynchronized() {
    }

    public abstract void close();
}
