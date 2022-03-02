package io.github.projectet.ae2things.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.item.AETItems;
import io.github.projectet.ae2things.storage.DISKCellInventory;
import io.github.projectet.ae2things.storage.IDISKCellItem;
import io.github.projectet.ae2things.util.Constants;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class Command {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.getRoot().addChild(commandRoot);
            dispatcher.getRoot().addChild(recoverArg);
            dispatcher.getRoot().addChild(copyUUID);
        });
    }

    static LiteralCommandNode<ServerCommandSource> commandRoot = CommandManager.literal(AE2Things.MOD_ID).executes(Command::help).build();

    static LiteralCommandNode<ServerCommandSource> recoverArg = CommandManager.literal(AE2Things.MOD_ID)
            .then(CommandManager.literal("recover")
                    .then(CommandManager
                            .argument("uuid", UuidArgumentType.uuid())
                            .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                            .executes(context -> spawnDrive(context, context.getArgument("uuid", UUID.class)))
                    )
            )
            .build();

    static LiteralCommandNode<ServerCommandSource> copyUUID = CommandManager.literal(AE2Things.MOD_ID)
            .then(CommandManager.literal("getuuid")
                    .executes(Command::getUUID)
            )
            .build();

    private static int help(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText("Available Argument(s): "), false);
        context.getSource().sendFeedback(new LiteralText("/ae2things recover <UUID> - Spawns a drive with the given UUID, if it doesn't exist, does not spawn any item."), false);
        context.getSource().sendFeedback(new LiteralText("/ae2things getuuid - Gets the UUID of the drive in the player's hand if it has a UUID. Returns the DISKS uuid."), false);
        return 0;
    }

    private static int spawnDrive(CommandContext<ServerCommandSource> context, UUID uuid) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();

        if(AE2Things.STORAGE_INSTANCE.hasUUID(uuid)) {
            ItemStack stack = new ItemStack(AETItems.DISK_DRIVE_64K);
            NbtCompound nbt = new NbtCompound();

            nbt.putUuid(Constants.DISKUUID, uuid);
            nbt.putLong(DISKCellInventory.ITEM_COUNT_TAG, AE2Things.STORAGE_INSTANCE.getOrCreateDisk(uuid).itemCount);
            stack.setNbt(nbt);

            player.giveItemStack(stack);

            context.getSource().sendFeedback(new TranslatableText("command.ae2things.recover_success", player.getDisplayName(), uuid), true);
            return 0;
        }
        else {
            context.getSource().sendError(new TranslatableText("command.ae2things.recover_fail", uuid));
            return 1;
        }
    }

    private static int getUUID(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerEntity player = context.getSource().getPlayer();
        ItemStack mainStack = player.getMainHandStack();
        if(mainStack.getItem() instanceof IDISKCellItem) {
            if(mainStack.hasNbt() && mainStack.getNbt().contains(Constants.DISKUUID)) {
                Text text = copyToClipboard(mainStack.getNbt().getUuid(Constants.DISKUUID).toString());
                context.getSource().sendFeedback(new TranslatableText("command.ae2things.getuuid_success", text), false);
                return 0;
            }
            else {
                context.getSource().sendError(new TranslatableText("command.ae2things.getuuid_fail_nouuid"));
                return 1;
            }
        }
        context.getSource().sendError(new TranslatableText("command.ae2things.getuuid_fail_notdisk"));
        return 1;
    }

    private static Text copyToClipboard(String string) {
        return new LiteralText(string).styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, string))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click")))
                .withInsertion(string)
                .withColor(Formatting.GREEN));
    }
}
