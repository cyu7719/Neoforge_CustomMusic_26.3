package com.custommusic.item;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class JukeboxAlbumBlockItem extends BlockItem {
    private static long lastHoverTime = 0;
    
    public JukeboxAlbumBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int currentPage = 1;
        int maxPages = 1;
        var beData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (beData != null) {
            var tag = beData.copyTagWithoutId();
            maxPages = tag.getIntOr("MaxPages", 1);
            if (maxPages < 1) maxPages = 1;
            // CurrentPage is 0-indexed in the BE, so add 1 for display
            currentPage = tag.getIntOr("CurrentPage", 0) + 1;
            if (currentPage < 1) currentPage = 1;
            if (currentPage > maxPages) currentPage = maxPages;
        }
        tooltipComponents.accept(Component.literal("Page: " + currentPage + "/" + maxPages).withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Jukebox Album");
    }

    @Override
    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

}