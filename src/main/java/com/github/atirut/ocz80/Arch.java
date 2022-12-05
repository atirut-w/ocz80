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
    private boolean initialized = false;
    private int memorySize;

    private byte[] eeprom;
    private byte[] ram;
    private byte[] mmap;

    private RegisterSet main;
    private RegisterSet shadow;

    public Arch(Machine machine) {
        this.machine = machine;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean recomputeMemory(Iterable<ItemStack> stacks) {
        OCZ80.logger.info("Recomputing memory...");
        memorySize = 0;

        for (ItemStack stack : stacks) {
            DriverItem driver = Driver.driverFor(stack);
            if (driver instanceof Memory) {
                memorySize += ((Memory)driver).amount(stack) * 1024;
            }
        }
        memorySize /= 4; // Full 192KB for a tier 1 stick is a little much
        OCZ80.logger.info("New total memory: " + memorySize / 1024 + "KB");

        if (initialized) {
            byte[] copy = new byte[memorySize];
            for (int i = 0; i < Math.min(memorySize, ram.length); i++) {
                copy[i] = ram[i];
            }
            ram = copy;
        }

        return memorySize > 0;
    }

    public boolean initialize() {
        ram = new byte[memorySize];
        mmap = new byte[16];
        mmap[0] = 0; // Map in the EEPROM on cold boot.

        main = new RegisterSet();
        shadow = new RegisterSet();

        try {
            String eepromUUID = machine.components().entrySet().stream().filter(i -> i.getValue().equals("eeprom")).map(i -> i.getKey()).findAny().orElse(null);
            eeprom = (byte[])(machine.invoke(eepromUUID, "get", new Object[0])[0]);
        } catch (Exception e) {
            eeprom = new byte[0];
        }

        return initialized = true;
    }

    public void close() {
        initialized = false;
    }

    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        return execute();
    }

    public void runSynchronized() {
        execute();
    }

    public void onSignal() {}

    public void onConnect() {}

    public void load(NBTTagCompound nbt) {}

    public void save(NBTTagCompound nbt) {}

    // VM functions start here.
    ExecutionResult execute() {
        switch (fetch()) {
            default:
                main.pc--;
                return new ExecutionResult.Error(String.format("Unknown opcode 0x%01x at 0x%04x", (int)read(main.pc), (int)main.pc));
        }
        return new ExecutionResult.SynchronizedCall();
    }

    byte fetch() {
        return read(main.pc++);
    }

    byte read(short address) {
        if (address >> 12 == 0) {
            if (eeprom.length > 0) {
                return eeprom[address & 0x0fff];
            }
            return 0;
        } else {
            if (mmap[(address >> 12 - 1)] * 4096 > memorySize - 1) {
                return 0;
            }
            return ram[mmap[(address >> 12 - 1)] * 4096];
        }
    }
}
