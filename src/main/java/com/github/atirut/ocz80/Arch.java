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

    private short[] main;
    private short[] shadow;
    private short pc;
    private boolean running = true;

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

        try {
            String eepromUUID = machine.components().entrySet().stream().filter(i -> i.getValue().equals("eeprom")).map(i -> i.getKey()).findAny().orElse(null);
            Object[] result = machine.invoke(eepromUUID, "get", new Object[0]);
            eeprom = (byte[]) result[0];
        } catch (Exception e) {
            OCZ80.logger.error("Could not read EEPROM: " + e.toString());
            eeprom = new byte[0];
        }

        return memorySize > 0;
    }

    public boolean initialize() {
        ram = new byte[memorySize];
        mmap = new byte[16];
        mmap[0] = 0; // Map in the EEPROM on cold boot.

        main = new short[8];
        shadow = new short[8];
        pc = 0;

        return initialized = true;
    }

    public void close() {
        initialized = false;
    }

    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        return new ExecutionResult.SynchronizedCall();
    }

    public void runSynchronized() {
        execute();
    }

    public void onSignal() {}

    public void onConnect() {}

    public void load(NBTTagCompound nbt) {}

    public void save(NBTTagCompound nbt) {}

    // VM functions start here.
    void execute() {
        if (running == false) {
            return;
        }

        byte op = fetch();
        
        // Check for prefixes
        switch (op) {
            default:
                Instruction inst = new Instruction(op);
                switch (inst.x) {
                    case 0:
                        switch (inst.z) {
                            case 6:
                                main[inst.y] = fetch();
                        }
                    case 1:
                        if (inst.z == 6 && inst.y == 6) {
                            running = false;
                        }
                    case 3:
                        switch (inst.z) {
                            case 3:
                                switch (inst.y) {
                                    case 2:
                                        OCZ80.logger.info("IO out");
                                        fetch();
                                }
                        }
                }
        }
    }

    byte fetch() {
        return read(pc++);
    }

    byte read(short address) {
        if (address >> 12 == 0) {
            if (eeprom.length > 0) {
                return eeprom[(address & 0x0fff) % eeprom.length];
            }
            return 0;
        } else {
            if (mmap[(address >> 12 - 1)] * 4096 > memorySize - 1) {
                return 0;
            }
            return ram[mmap[(address >> 12 - 1)] * 4096];
        }
    }

    private class Instruction {
        int x, y, z, p, q;

        public Instruction(byte opcode) {
            x = (opcode >> 6) & 0b11;
            y = (opcode >> 3) & 0b111;
            z = opcode & 0b111;
            
            p = y >> 1;
            q = y % 2;
        }
    }
}
