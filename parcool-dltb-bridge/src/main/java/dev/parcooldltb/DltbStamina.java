package dev.parcooldltb;

import com.alrex.parcool.api.stamina.AbstractLocalStamina;
import net.minecraft.world.entity.player.Player;
import ru.xmeowwwly.ktstanima.client.ClientStaminaData;

/**
 * ParCool stamina backed by Stamina [DLTB].
 *
 * ParCool only creates this object for the LOCAL player on the client. The authoritative value lives on the
 * server inside DLTB and is mirrored to the client by DLTB itself (ClientStaminaData), so:
 *  - reading  = take DLTB's synchronized client copy,
 *  - spending = batch the cost and send it to the server once per tick (see ConsumePacket),
 *  - recovery = DLTB's own regeneration, so recover()/setValue() intentionally do nothing.
 */
public class DltbStamina extends AbstractLocalStamina {
    /** Stamina [DLTB] bar size: PlayerStamina starts at and is capped to 1000. */
    private static final double DLTB_MAX = 1000.0;

    /** Cost (DLTB units) collected this tick and not yet sent to the server. */
    private float pending = 0f;

    public DltbStamina(Player owner) {
        super(owner);
    }

    @Override
    public double max() {
        return DLTB_MAX;
    }

    @Override
    public double value() {
        return Math.max(0.0, ClientStaminaData.INSTANCE.getTargetStamina() - pending);
    }

    @Override
    public boolean isExhausted() {
        return ClientStaminaData.INSTANCE.isExhausted();
    }

    @Override
    public void setValue(double value) {
        // The value is owned by DLTB on the server.
    }

    @Override
    public void consume(double value) {
        if (value <= 0 || isInfinite() || isExhausted()) return;
        pending += (float) (value * BridgeConfig.COST_SCALE.get());
        setDirty();
    }

    @Override
    public void recover(double value) {
        // DLTB regenerates stamina by itself (after a short delay following any spending).
    }

    @Override
    public void tick() {
        if (pending > 0f) {
            BridgeNetwork.CHANNEL.sendToServer(new ConsumePacket(pending));
            pending = 0f;
        }
        // Keep ParCool's copy for other clients / the server up to date.
        setDirty();
    }

    @Override
    public boolean imposePenalty() {
        // DLTB already blocks sprinting, mining and attacking while exhausted; avoid a second penalty on top.
        return false;
    }

    @Override
    public boolean showHud() {
        // Stamina [DLTB] draws its own bar.
        return false;
    }
}
