package com.timegun.mod.freeze;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class FreezeManager {

    public static void freezeEntity(LivingEntity target) {
        // 1. Stop gravity so they stay exactly where they are (even mid-air)
        target.setNoGravity(true);

        // 2. Kill all physical momentum instantly
        target.setDeltaMovement(Vec3.ZERO);

        // 3. Turn off movement states that cause particles
        target.setSprinting(false);
        target.setJumping(false);

        // 4. Mobs have specific AI that we need to pause
        if (target instanceof Mob mob) {
            mob.setNoAi(true); // This entirely stops walking, attacking, and wandering
            mob.setPersistenceRequired(); // This ensures they don't despawn while frozen
        }
    }
}