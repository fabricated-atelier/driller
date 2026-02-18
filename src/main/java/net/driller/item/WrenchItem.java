package net.driller.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class WrenchItem extends Item {

    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if(!useOnContext.getLevel().isClientSide()){
            if (useOnContext.getItemInHand().has(DataComponents.CUSTOM_DATA)) {
                useOnContext.getItemInHand().set(DataComponents.CUSTOM_DATA, null);
                useOnContext.getPlayer().displayClientMessage(Component.literal("Refreshed Wrench!"), true);
            }
        }
        return super.useOn(useOnContext);
    }
}
