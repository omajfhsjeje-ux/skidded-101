package com.zeus.krypton.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Map;

public class StorageEsp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgStorage = settings.createGroup("Storage");
    private final SettingGroup sgColors = settings.createGroup("Colors");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Maximum distance at which storage is rendered.")
        .defaultValue(128)
        .min(8)
        .sliderMax(512)
        .build());

    private final Setting<RenderMode> renderMode = sgGeneral.add(new EnumSetting.Builder<RenderMode>()
        .name("render-mode")
        .description("How storage blocks are rendered.")
        .defaultValue(RenderMode.Both)
        .build());

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draw lines from the player to storage.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> names = sgGeneral.add(new BoolSetting.Builder()
        .name("names")
        .description("Render the storage type above the block.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> distance = sgGeneral.add(new BoolSetting.Builder()
        .name("distance")
        .description("Render the distance to storage.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> ignoreEmpty = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-empty")
        .description("Do not render containers which are empty.")
        .defaultValue(false)
        .build());

    private final Setting<Boolean> chests = sgStorage.add(new BoolSetting.Builder()
        .name("chests")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> barrels = sgStorage.add(new BoolSetting.Builder()
        .name("barrels")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> shulkers = sgStorage.add(new BoolSetting.Builder()
        .name("shulkers")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> enderChests = sgStorage.add(new BoolSetting.Builder()
        .name("ender-chests")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> hoppers = sgStorage.add(new BoolSetting.Builder()
        .name("hoppers")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> furnaces = sgStorage.add(new BoolSetting.Builder()
        .name("furnaces")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> redstoneStorage = sgStorage.add(new BoolSetting.Builder()
        .name("redstone-storage")
        .description("Render dispensers and droppers.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> beacons = sgStorage.add(new BoolSetting.Builder()
        .name("beacons")
        .defaultValue(true)
        .build());

    private final Setting<SettingColor> chestColor = sgColors.add(new ColorSetting.Builder()
        .name("chest-color")
        .defaultValue(new SettingColor(255, 170, 0, 125))
        .build());

    private final Setting<SettingColor> barrelColor = sgColors.add(new ColorSetting.Builder()
        .name("barrel-color")
        .defaultValue(new SettingColor(160, 100, 50, 125))
        .build());

    private final Setting<SettingColor> shulkerColor = sgColors.add(new ColorSetting.Builder()
        .name("shulker-color")
        .defaultValue(new SettingColor(180, 80, 255, 125))
        .build());

    private final Setting<SettingColor> enderColor = sgColors.add(new ColorSetting.Builder()
        .name("ender-color")
        .defaultValue(new SettingColor(170, 0, 255, 125))
        .build());

    private final Setting<SettingColor> hopperColor = sgColors.add(new ColorSetting.Builder()
        .name("hopper-color")
        .defaultValue(new SettingColor(100, 100, 100, 125))
        .build());

    private final Setting<SettingColor> furnaceColor = sgColors.add(new ColorSetting.Builder()
        .name("furnace-color")
        .defaultValue(new SettingColor(100, 100, 100, 125))
        .build());

    private final Setting<SettingColor> dispenserColor = sgColors.add(new ColorSetting.Builder()
        .name("dispenser-color")
        .defaultValue(new SettingColor(255, 70, 70, 125))
        .build());

    private final Setting<SettingColor> beaconColor = sgColors.add(new ColorSetting.Builder()
        .name("beacon-color")
        .defaultValue(new SettingColor(0, 255, 255, 125))
        .build());

    private final Map<BlockPos, StorageType> visibleStorage = new HashMap<>();

    public StorageEsp() {
        super(KryptonAddon.CATEGORY, "storage-esp", "Krypton-style storage ESP for Meteor.");
    }

    @Override
    public void onActivate() {
        visibleStorage.clear();
    }

    @Override
    public void onDeactivate() {
        visibleStorage.clear();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null || mc.player == null) {
            return;
        }

        visibleStorage.clear();

        int radius = range.get().intValue();
        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) {
                        continue;
                    }

                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    Block block = state.getBlock();

                    StorageType type = getStorageType(block);

                    if (type == null) {
                        continue;
                    }

                    BlockEntity entity = mc.world.getBlockEntity(pos);

                    if (ignoreEmpty.get() && entity != null && isEmpty(entity)) {
                        continue;
                    }

                    visibleStorage.put(pos.toImmutable(), type);
                }
            }
        }

        for (Map.Entry<BlockPos, StorageType> entry : visibleStorage.entrySet()) {
            renderStorage(event, entry.getKey(), entry.getValue());
        }
    }

    private StorageType getStorageType(Block block) {
        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
            return chests.get() ? StorageType.CHEST : null;
        }

        if (block == Blocks.BARREL) {
            return barrels.get() ? StorageType.BARREL : null;
        }

        if (block instanceof net.minecraft.block.ShulkerBoxBlock) {
            return shulkers.get() ? StorageType.SHULKER : null;
        }

        if (block == Blocks.ENDER_CHEST) {
            return enderChests.get() ? StorageType.ENDER_CHEST : null;
        }

        if (block == Blocks.HOPPER) {
            return hoppers.get() ? StorageType.HOPPER : null;
        }

        if (block == Blocks.FURNACE ||
            block == Blocks.BLAST_FURNACE ||
            block == Blocks.SMOKER) {
            return furnaces.get() ? StorageType.FURNACE : null;
        }

        if (block == Blocks.DISPENSER || block == Blocks.DROPPER) {
            return redstoneStorage.get() ? StorageType.DISPENSER : null;
        }

        if (block == Blocks.BEACON) {
            return beacons.get() ? StorageType.BEACON : null;
        }

        return null;
    }

    private boolean isEmpty(BlockEntity entity) {
        if (entity instanceof ChestBlockEntity chest) {
            return chest.size() == 0;
        }

        if (entity instanceof ShulkerBoxBlockEntity shulker) {
            for (int i = 0; i < shulker.size(); i++) {
                if (!shulker.getStack(i).isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        if (entity instanceof HopperBlockEntity hopper) {
            for (int i = 0; i < hopper.size(); i++) {
                if (!hopper.getStack(i).isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        if (entity instanceof DispenserBlockEntity dispenser) {
            for (int i = 0; i < dispenser.size(); i++) {
                if (!dispenser.getStack(i).isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private void renderStorage(Render3DEvent event, BlockPos pos, StorageType type) {
        Box box = new Box(pos);

        Color color = getColor(type);

        int side = color.a;
        int line = Math.min(255, color.a + 50);

        if (renderMode.get() == RenderMode.Sides ||
            renderMode.get() == RenderMode.Both) {
            event.renderer.box(
                box,
                color,
                new Color(color.r, color.g, color.b, line),
                meteordevelopment.meteorclient.utils.render.ShapeMode.Sides,
                0
            );
        }

        if (renderMode.get() == RenderMode.Lines ||
            renderMode.get() == RenderMode.Both) {
            event.renderer.box(
                box,
                new Color(color.r, color.g, color.b, 0),
                new Color(color.r, color.g, color.b, line),
                meteordevelopment.meteorclient.utils.render.ShapeMode.Lines,
                0
            );
        }

        if (tracers.get()) {
            event.renderer.line(
                MinecraftClient.getInstance().player.getX(),
                MinecraftClient.getInstance().player.getY() + 1.0,
                MinecraftClient.getInstance().player.getZ(),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                new Color(color.r, color.g, color.b, 180)
            );
        }
    }

    private Color getColor(StorageType type) {
        return switch (type) {
            case CHEST -> chestColor.get();
            case BARREL -> barrelColor.get();
            case SHULKER -> shulkerColor.get();
            case ENDER_CHEST -> enderColor.get();
            case HOPPER -> hopperColor.get();
            case FURNACE -> furnaceColor.get();
            case DISPENSER -> dispenserColor.get();
            case BEACON -> beaconColor.get();
        };
    }

    private enum StorageType {
        CHEST,
        BARREL,
        SHULKER,
        ENDER_CHEST,
        HOPPER,
        FURNACE,
        DISPENSER,
        BEACON
    }

    public enum RenderMode {
        Sides,
        Lines,
        Both
    }
}
