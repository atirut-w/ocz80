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
    private char[] ram = new char[0];

    public Arch(Machine machine) {
        this.machine = machine;
    }

    public boolean isInitialized() {
        return true;
    }

    public boolean recomputeMemory(Iterable<ItemStack> stacks) {
        OCZ80.logger.info("Recomputing memory...");
        int total = 0;

        for (ItemStack stack : stacks) {
            DriverItem driver = Driver.driverFor(stack);
            if (driver instanceof Memory) {
                total += ((Memory)driver).amount(stack) * 1024;
            }
        }
        total /= 4; // Full 192KB for a tier 1 stick is a little much

        OCZ80.logger.info("New total memory: " + total / 1024 + "KB");
        char[] copy = new char[total];
        for (int i = 0; i < Math.min(total, ram.length); i++) {
            copy[i] = ram[i];
        }
        ram = copy;

        return total > 0;
    }

    public boolean initialize() {
        return true;
    }

    public void close() {}

    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        return new ExecutionResult.SynchronizedCall();
    }

    public void runSynchronized() {}

    public void onSignal() {}

    public void onConnect() {}

    public void load(NBTTagCompound nbt) {}

    public void save(NBTTagCompound nbt) {}
}
