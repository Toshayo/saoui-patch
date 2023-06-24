package net.toshayo.saouipatch;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;

@Mod(
        modid = SAOUIPatchMod.MOD_ID,
        name = SAOUIPatchMod.NAME,
        version = SAOUIPatchMod.VERSION,
        certificateFingerprint = "ee4beef430d574ba7d8c096a4f7f9c6c755bd30f"
)
public class SAOUIPatchMod {
    public static final String MOD_ID = "saouipatch";
    public static final String NAME = "SAO UI Patch";
    public static final String VERSION = "@@VERSION@@";

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public static void onFingerPrintViolation(FMLFingerprintViolationEvent event) {
        if (!event.isDirectory) {
            System.err.println("A file failed to match with the signing key.");
            System.err.println("If you *know* this is a homebrew/custom build then this is expected, carry on.");
        }
    }
}
