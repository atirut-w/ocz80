package com.github.atirut.ocz80;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Memory;
import li.cil.oc.api.machine.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.github.atirut.ocz80.state.*;

@Architecture.Name("Zilog Z80")
public class Arch implements Architecture {
    private final Machine machine;
    private State state;
    private int memorySize;

    public Arch(Machine machine) {
        this.machine = machine;
    }

    public boolean isInitialized() {
        return state != null && state.isInitialized();
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
        close();
        state = new FindEEPROM(this, machine);
        return true;
    }

    public void close() {
        if (state != null) {
            state.close();  
        }
        state = null;
    }

    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        Transition transition = state.runThreaded();
        state = transition.nexState;
        return transition.executionResult;
    }

    public void runSynchronized() {
        state.runSynchronized();
    }

    public void onSignal() {}

    public void onConnect() {}

    public void load(NBTTagCompound nbt) {}

    public void save(NBTTagCompound nbt) {}
}
