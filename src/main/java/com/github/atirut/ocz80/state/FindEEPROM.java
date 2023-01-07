package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;

import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;

public class FindEEPROM extends State {
    public FindEEPROM(Arch arch, Machine machine) {
        super(arch, machine);
    }

    public boolean isInitialized() {
        return false;
    }

    public Transition runThreaded() {
        // I don't know how this works tbh
        String eepromUUID = machine.components().entrySet().stream().filter(i -> i.getValue().equals("eeprom")).map(i -> i.getKey()).findAny().orElse(null);
        if (eepromUUID == null) {
            return new Transition(null, new ExecutionResult.Error("No EEPROM found"));
        }

        try {
            Object[] result = machine.invoke(eepromUUID, "get", new Object[0]);
            byte[] eeprom = (byte[])result[0];

            return new Transition(new Run(arch, machine, eeprom), SLEEP_ZERO);
        } catch (Exception e) {

        }

        return new Transition(null, new ExecutionResult.Error("Could not read EEPROM"));
    }

    public void close() {
    }
}
