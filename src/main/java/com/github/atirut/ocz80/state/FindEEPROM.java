package com.github.atirut.ocz80.state;

import com.github.atirut.ocz80.Arch;
import com.github.atirut.ocz80.OCZ80;

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
        byte[] eeprom = new byte[0];

        if (eepromUUID != null) {
            try {
                Object[] result = machine.invoke(eepromUUID, "get", new Object[0]);
                eeprom = (byte[])result[0];
            } catch (Exception e) {
                OCZ80.logger.error(e.toString());
                return new Transition(null, new ExecutionResult.Error("Error reading EEPROM"));
            }
        }

        return new Transition(new Run(arch, machine, eeprom), SLEEP_ZERO);
    }

    public void close() {
    }
}
