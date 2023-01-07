package com.github.atirut.ocz80;

import li.cil.oc.api.machine.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@Architecture.Name("Zilog Z80")
public class Arch implements Architecture {
    private final Machine machine;

    public Arch(Machine machine) {
        this.machine = machine;
    }

    public boolean isInitialized() {
        return false;
    }

    public boolean recomputeMemory(Iterable<ItemStack> stacks) {
        return false;
    }

    public boolean initialize() {
        return false;
    }

    public void close() {
    }

    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        return new ExecutionResult.SynchronizedCall();
    }

    public void runSynchronized() {
    }

    public void onSignal() {}

    public void onConnect() {}

    public void load(NBTTagCompound nbt) {}

    public void save(NBTTagCompound nbt) {}
}
