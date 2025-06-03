package dev.dubhe.curtain.features.player.menu;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.curtain.api.menu.CustomMenu;
import dev.dubhe.curtain.api.menu.control.AutoResetButton;
import dev.dubhe.curtain.api.menu.control.Button;
import dev.dubhe.curtain.api.menu.control.RadioList;
import dev.dubhe.curtain.features.player.fakes.IServerPlayer;
import dev.dubhe.curtain.features.player.helpers.EntityPlayerActionPack;
import dev.dubhe.curtain.features.player.helpers.EntityPlayerActionPack.Action;
import dev.dubhe.curtain.features.player.helpers.EntityPlayerActionPack.ActionType;
import dev.dubhe.curtain.utils.TranslationHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FakePlayerInventoryMenu extends CustomMenu {

    public final NonNullList<ItemStack> items;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offhand;
    private final NonNullList<ItemStack> buttons = NonNullList.withSize(13, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments;
    private final Player player;
    private final EntityPlayerActionPack ap;

    public FakePlayerInventoryMenu(Player player) {
        this.player = player;
        this.items = this.player.getInventory().items;
        this.armor = this.player.getInventory().armor;
        this.offhand = this.player.getInventory().offhand;
        this.ap = ((IServerPlayer) this.player).getActionPack();
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand, this.buttons);
        this.createButton();
        this.ap.setSlot(1);
    }

    @Override
    public int getContainerSize() {
        return this.items.size() + this.armor.size() + this.offhand.size() + this.buttons.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        for (ItemStack itemStack : this.armor) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        for (ItemStack itemStack : this.offhand) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    @Nonnull
    public ItemStack getItem(int slot) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        if (pair != null) {
            return pair.getFirst().get(pair.getSecond());
        } else {
            return ItemStack.EMPTY;
        }
    }

    public Pair<NonNullList<ItemStack>, Integer> getItemSlot(int slot) {
        switch (slot) {
            case 0 -> {
                return new Pair<>(buttons, 0);
            }
            case 1, 2, 3, 4 -> {
                return new Pair<>(armor, 4 - slot);
            }
            case 5, 6 -> {
                return new Pair<>(buttons, slot - 4);
            }
            case 7 -> {
                return new Pair<>(offhand, 0);
            }
            case 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 -> {
                return new Pair<>(buttons, slot - 5);
            }
            case 18, 19, 20, 21, 22, 23, 24, 25, 26,
                    27, 28, 29, 30, 31, 32, 33, 34, 35,
                    36, 37, 38, 39, 40, 41, 42, 43, 44 -> {
                return new Pair<>(items, slot - 9);
            }
            case 45, 46, 47, 48, 49, 50, 51, 52, 53 -> {
                return new Pair<>(items, slot - 45);
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int slot, int amount) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            return ContainerHelper.removeItem(list, slot, amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int slot) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            ItemStack itemStack = list.get(slot);
            list.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null) {
            list.set(slot, stack);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return this.player.isAlive() && !(player.distanceToSqr(this.player) > 64.0);
    }

    @Override
    public void clearContent() {
        for (List<ItemStack> list : this.compartments) {
            list.clear();
        }
    }

    private void createButton() {
        List<Button> hotBarList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            Component hotBarComponent = TranslationHelper.translate(
                    "curtain.rules.open_fake_player_inventory.menu.slot",
                    ChatFormatting.WHITE,
                    Style.EMPTY.withBold(true).withItalic(false),
                    i + 1
            );
            boolean defaultState = i == 0;
            Button button = new Button(defaultState, i + 1,
                    hotBarComponent,
                    hotBarComponent
            );
            int finalI = i + 1;
            button.addTurnOnFunction(() -> ap.setSlot(finalI));
            this.addButton(i + 9, button);
            hotBarList.add(button);
        }
        this.addButtonList(new RadioList(hotBarList, true));

        Button stopAll = new AutoResetButton("curtain.rules.open_fake_player_inventory.menu.stop_all");
        Button attackInterval14 = new Button(false, "curtain.rules.open_fake_player_inventory.menu.attack_interval_14");
        Button attackContinuous = new Button(false, "curtain.rules.open_fake_player_inventory.menu.attack_continuous");
        Button useContinuous = new Button(false, "curtain.rules.open_fake_player_inventory.menu.use_continuous");

        stopAll.addTurnOnFunction(() -> {
            attackInterval14.turnOffWithoutFunction();
            attackContinuous.turnOffWithoutFunction();
            useContinuous.turnOffWithoutFunction();
            ap.stopAll();
        });

        attackInterval14.addTurnOnFunction(() -> {
            ap.start(ActionType.ATTACK, Action.interval(14));
            attackContinuous.turnOffWithoutFunction();
        });
        attackInterval14.addTurnOffFunction(() -> ap.start(ActionType.ATTACK, Action.once()));

        attackContinuous.addTurnOnFunction(() -> {
            ap.start(ActionType.ATTACK, Action.continuous());
            attackInterval14.turnOffWithoutFunction();
        });
        attackContinuous.addTurnOffFunction(() -> ap.start(ActionType.ATTACK, Action.once()));

        useContinuous.addTurnOnFunction(() -> ap.start(ActionType.USE, Action.continuous()));
        useContinuous.addTurnOffFunction(() -> ap.start(ActionType.USE, Action.once()));

        this.addButton(0, stopAll);
        this.addButton(5, attackInterval14);
        this.addButton(6, attackContinuous);
        this.addButton(8, useContinuous);
    }
}
