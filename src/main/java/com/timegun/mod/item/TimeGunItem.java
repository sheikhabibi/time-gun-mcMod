package com.timegun.mod.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class TimeGunItem extends Item {
    // We will move this to a config file in Phase 16
    private static final double RANGE = 20.0;

    public TimeGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // 1. Only run gameplay logic on the Server (Guideline 17)
        if (!level.isClientSide()) {

            // 2. Calculate where the player is looking
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getViewVector(1.0F);
            Vec3 endPosition = eyePosition.add(lookVector.x * RANGE, lookVector.y * RANGE, lookVector.z * RANGE);

            // Create a bounding box to search for entities within our line of sight
            AABB searchArea = player.getBoundingBox().expandTowards(lookVector.scale(RANGE)).inflate(1.0D);

            // 3. Find the closest entity in our line of sight
            LivingEntity target = null;
            double closestDistance = RANGE * RANGE; // We use squared distance for performance!

            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, searchArea, e -> e != player && !e.isSpectator())) {
                // inflate(0.3D) gives the mob a slightly more generous hitbox (standard for weapons)
                AABB entityBox = entity.getBoundingBox().inflate(0.3D);
                Optional<Vec3> hitPos = entityBox.clip(eyePosition, endPosition);

                if (hitPos.isPresent()) {
                    double distance = eyePosition.distanceToSqr(hitPos.get());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        target = entity;
                    }
                }
            }

            // 4. Did we hit something?
            if (target != null) {
                // Success! Print it to the console for now.
                System.out.println("Time Gun Hit: " + target.getName().getString());
            } else {
                System.out.println("Time Gun Fired: Missed!");
            }
        }

        // 5. Return the modern interaction result
        return InteractionResult.SUCCESS;
    }
}