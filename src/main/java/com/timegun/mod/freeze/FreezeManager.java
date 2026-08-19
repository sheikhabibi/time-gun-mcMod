package com.timegun.mod.freeze;

import com.timegun.mod.TimeGun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

// This annotation tells NeoForge to listen to the events inside this class!
@EventBusSubscriber(modid = TimeGun.MODID)
public class FreezeManager {

    // 5 seconds = 100 game ticks (Minecraft runs at 20 ticks per second)
    public static final int FREEZE_DURATION_TICKS = 100;

    // A map to track which entities are frozen and how much time they have left
    private static final Map<LivingEntity, Integer> FROZEN_ENTITIES = new WeakHashMap<>();

    public static void freezeEntity(LivingEntity target) {
        target.setNoGravity(true);
        target.setDeltaMovement(Vec3.ZERO);
        target.setSprinting(false);
        target.setJumping(false);

        if (target instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
        }

        // Add the mob to our tracker, or reset its timer back to 100 if shot again!
        FROZEN_ENTITIES.put(target, FREEZE_DURATION_TICKS);
    }

    public static void unfreezeEntity(LivingEntity target) {
        target.setNoGravity(false);

        if (target instanceof Mob mob) {
            mob.setNoAi(false);
        }
    }

    // This runs every single game tick on the server
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (FROZEN_ENTITIES.isEmpty()) return;

        Iterator<Map.Entry<LivingEntity, Integer>> iterator = FROZEN_ENTITIES.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, Integer> entry = iterator.next();
            LivingEntity entity = entry.getKey();
            int ticksRemaining = entry.getValue() - 1;

            // If the entity was killed or deleted, remove it from the list
            if (!entity.isAlive() || entity.isRemoved()) {
                iterator.remove();
                continue;
            }

            // If time is up, unfreeze them! Otherwise, save the new time.
            if (ticksRemaining <= 0) {
                unfreezeEntity(entity);
                iterator.remove();
            } else {
                entry.setValue(ticksRemaining);
            }
        }
    }
}