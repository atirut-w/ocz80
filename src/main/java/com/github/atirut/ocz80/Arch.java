package com.github.atirut.ocz80;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Memory;
import li.cil.oc.api.machine.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@Architecture.Name("Zilog Z80")
public class Arch implements Architecture {
    private final Machine machine;
    private int memorySize;

    public Arch(Machine machine) {
        this.machine = machine;
    }

    public boolean isInitialized() {
        return false;
    }

    public boolean recomputeMemory(Iterable<ItemStack> components) {
        memorySize = 0;

        for (final ItemStack i : components) {
            final DriverItem driver = Driver.driverFor(i);
            if (driver instanceof Memory) {
                memorySize += ((Memory)driver).amount(i) * 1024;
            }
        }
        memorySize /= 4;
        OCZ80.logger.info("New memory size: " + memorySize / 1024 + "KB");

        return memorySize > 0;
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
