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

            // --- NEW AMNESIA CODE ---
            mob.setTarget(null); // Makes the mob forget who it was attacking!
        }

        // Add the mob to our tracker, or reset its timer back to 100 if shot again!
        FROZEN_ENTITIES.put(target, FREEZE_DURATION_TICKS);

        // Play the custom freeze sound!
        target.level().playSound(null, target.blockPosition(), com.timegun.mod.TimeGun.FREEZE_SOUND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
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

            // --- NEW PARTICLE CODE START ---
            // Tell the server to broadcast particles to everyone looking at the mob!
            if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // We use SNOWFLAKE particles, spawning 2 per tick around the mob's body
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                        entity.getX(), entity.getY() + (entity.getBbHeight() / 2.0), entity.getZ(),
                        2, // number of particles
                        entity.getBbWidth() / 1.5, entity.getBbHeight() / 2.0, entity.getBbWidth() / 1.5, // spread XYZ
                        0.0 // speed
                );
            }
            // --- NEW PARTICLE CODE END ---

            // --- NEW CREEPER DEFUSE CODE START ---
            // If the frozen entity is a Creeper, force its explosion fuse to run in reverse!
            if (entity instanceof net.minecraft.world.entity.monster.Creeper creeper) {
                creeper.setSwellDir(-1);
            }
            // --- NEW CREEPER DEFUSE CODE END ---

            // If time is up, unfreeze them! Otherwise, save the new time.
            if (ticksRemaining <= 0) {
                unfreezeEntity(entity);
                iterator.remove();
            } else {
                entry.setValue(ticksRemaining);
            }
        }
    }

    // Prevents interacting with frozen entities AND stops trading when shooting point-blank
    @SubscribeEvent
    public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity livingTarget) {

            // 1. If the mob is ALREADY frozen, block all interaction completely
            if (FROZEN_ENTITIES.containsKey(livingTarget)) {
                event.setCanceled(true);
                return;
            }

            // 2. If the player is holding the Time Gun, intercept the click!
            if (event.getEntity().getItemInHand(event.getHand()).getItem() instanceof com.timegun.mod.item.TimeGunItem) {
                event.setCanceled(true); // This instantly stops the trading menu from opening

                // Freeze the mob and apply the cooldown manually
                if (!event.getLevel().isClientSide()) {
                    freezeEntity(livingTarget);
                    // changed the cooldown to 20 ticks (1 second)
                    event.getEntity().getCooldowns().addCooldown(event.getEntity().getItemInHand(event.getHand()), 20);
                }
            }
        }
    }

    // Fixes the Slime splitting bug where babies inherit the frozen brain!
    @SubscribeEvent
    public static void onEntityJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            // If it spawns with NoAI, but we aren't tracking it, wake it up!
            if (mob.isNoAi() && !FROZEN_ENTITIES.containsKey(mob)) {
                mob.setNoAi(false);
                mob.setNoGravity(false);
            }
        }
    }
}