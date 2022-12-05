package com.github.atirut.ocz80;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = OCZ80.MODID, name = OCZ80.NAME, version = OCZ80.VERSION)
public class OCZ80 {
    public static final String MODID = "ocz80";
    public static final String NAME = "OCZ80";
    public static final String VERSION = "1.12.2-0.1.0";

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        li.cil.oc.api.Machine.add(com.github.atirut.ocz80.Arch.class);
    }
}
