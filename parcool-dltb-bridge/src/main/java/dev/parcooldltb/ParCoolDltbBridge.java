package dev.parcooldltb;

import com.alrex.parcool.api.stamina.RegisterParCoolStaminaTypeEvent;
import com.alrex.parcool.api.stamina.StaminaTypeEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

/**
 * Registers "parcooldltb:dltb" as a ParCool stamina type.
 * Once selected in parcool-server.toml, ParCool reads/spends the stamina bar of Stamina [DLTB].
 */
@Mod(ParCoolDltbBridge.MOD_ID)
public class ParCoolDltbBridge {
    public static final String MOD_ID = "parcooldltb";
    public static final ResourceLocation STAMINA_ID = new ResourceLocation(MOD_ID, "dltb");

    public ParCoolDltbBridge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BridgeConfig.SPEC);
        BridgeNetwork.register();
        listenOnParCoolBus();
    }

    /**
     * ParCool posts RegisterParCoolStaminaTypeEvent on ITS OWN mod event bus, inside its constructor.
     * Our own mod bus would never see it, so we attach to ParCool's bus.
     * This works because mods.toml makes this mod load BEFORE ParCool.
     */
    private static void listenOnParCoolBus() {
        ModContainer container = ModList.get().getModContainerById("parcool")
                .orElseThrow(() -> new IllegalStateException("[parcooldltb] ParCool is not loaded"));
        if (!(container instanceof FMLModContainer parCool)) {
            throw new IllegalStateException("[parcooldltb] Unexpected ParCool container type: " + container.getClass());
        }
        parCool.getEventBus().addListener(ParCoolDltbBridge::onRegisterStaminaType);
    }

    private static void onRegisterStaminaType(RegisterParCoolStaminaTypeEvent event) {
        event.register(new StaminaTypeEntry<>(
                STAMINA_ID,
                "dltb",
                (owner, old) -> new DltbStamina(owner)
        ));
    }
}
