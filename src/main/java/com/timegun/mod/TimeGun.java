package com.timegun.mod;

import com.timegun.mod.item.TimeGunItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TimeGun.MODID)
public class TimeGun {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "timegun";

    // 1. Create the Deferred Register for Items
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 2. Register the Time Gun Item (setting max stack size to 1 since it's a tool/gun)
    public static final DeferredItem<Item> TIME_GUN = ITEMS.registerItem("time_gun",
            properties -> new TimeGunItem(properties.stacksTo(1)));

    public TimeGun(IEventBus modEventBus) {
        // 3. Register the deferred register to the mod event bus
        ITEMS.register(modEventBus);

        // 4. Register the event to add our item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

    // 5. Add the item to the Tools & Utilities creative tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TIME_GUN);
        }
    }
}