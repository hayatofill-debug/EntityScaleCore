package com.omuraisu.entityscalecore.scaling;

import com.omuraisu.entityscalecore.api.MobScaleResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.SkullBlock;

import java.util.ArrayList;
import java.util.List;

public class MobEquipmentScaling {

    private static final double MIN_WEAPON_ATTACK_DAMAGE = 3.5;

    public static void apply(Mob mob, MobScaleResult result) {
        double multiplier = Math.max(1.0, Math.min(10.0, result.multiplier()));
        EquipmentTier tier = selectTier(mob, multiplier);

        selectAndEquipArmorSet(mob, multiplier, tier);

        selectAndEquipWeapon(mob, multiplier, tier);
    }

    private static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }

    public static boolean isWeaponCandidate(Mob mob, ItemStack stack, EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND || !isAllowedWeaponForMob(mob, stack)) {
            return false;
        }

        var modifiers = stack.getAttributeModifiers(slot);
        double attackDamage = 0.0;

        for (var entry : modifiers.entries()) {
            var attribute = entry.getKey();
            var modifier = entry.getValue();

            if (modifier.getOperation() != net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION) {
                continue;
            }

            if ("attribute.name.generic.attack_damage".equals(attribute.getDescriptionId())) {
                attackDamage += Math.abs(modifier.getAmount());
            }
        }

        return attackDamage >= MIN_WEAPON_ATTACK_DAMAGE;
    }

    private static boolean isAllowedWeaponForMob(Mob mob, ItemStack stack) {
        if (mob instanceof net.minecraft.world.entity.monster.Zombie) {
            return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.SHOVELS);
        }
        return false;
    }

    public static List<ItemStack> collectWeaponCandidates(Mob mob) {
        List<ItemStack> candidates = new ArrayList<>();

        for (var item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (isWeaponCandidate(mob, stack, EquipmentSlot.MAINHAND)) {
                candidates.add(stack);
            }
        }

        return candidates;
    }

    public static ItemStack selectWeaponCandidate(Mob mob, double multiplier) {
        List<ItemStack> candidates = collectWeaponCandidates(mob);
        if (candidates.isEmpty()) {
            return ItemStack.EMPTY;
        }

        double randomRange = Math.max(0.0, Math.min(2.0, (multiplier - 3.0) / 3.5));
        double targetWeaponScore = multiplier + (Math.random() * 2.0 - 1.0) * randomRange;
        targetWeaponScore = Math.max(3.5, Math.min(10.0, targetWeaponScore));

        ItemStack selected = ItemStack.EMPTY;
        double selectedDifference = Double.MAX_VALUE;

        for (ItemStack candidate : candidates) {
            double weaponScore = calculateWeaponScore(candidate);
            double difference = Math.abs(weaponScore - targetWeaponScore);

            if (difference < selectedDifference) {
                selected = candidate;
                selectedDifference = difference;
            }
        }

        return selected.copy();
    }

    private static void selectAndEquipWeapon(Mob mob, double multiplier, EquipmentTier tier) {
        ItemStack currentWeapon = mob.getItemBySlot(EquipmentSlot.MAINHAND);

        if (!currentWeapon.isEmpty() && isWeaponCandidate(mob, currentWeapon, EquipmentSlot.MAINHAND)) {
            return;
        }

        if (multiplier <= 3.5 || Math.random() >= (multiplier - 3.5) / 6.5 * 0.7) {
            return;
        }

        ItemStack selectedWeapon = selectWeaponCandidate(mob, multiplier);
        if (selectedWeapon.isEmpty()) {
            return;
        }

        if (tier == EquipmentTier.ENCHANTED) {
            selectedWeapon = applyEnchantment(mob, selectedWeapon);
        }

        mob.setItemSlot(EquipmentSlot.MAINHAND, selectedWeapon);
    }

    private static int getMaxArmorPieces(double multiplier) {
        double clamped = Math.max(1.0, Math.min(10.0, multiplier));

        if (clamped < 4.0) return 1;
        if (clamped < 6.0) return 2;
        if (clamped < 8.0) return 3;
        return 4;
    }

    private static List<EquipmentSlot> selectArmorSlots(Mob mob, int maxPieces) {
        List<EquipmentSlot> slots = new ArrayList<>();
        slots.add(EquipmentSlot.HEAD);
        slots.add(EquipmentSlot.CHEST);
        slots.add(EquipmentSlot.LEGS);
        slots.add(EquipmentSlot.FEET);

        for (int i = slots.size() - 1; i > 0; i--) {
            int j = mob.getRandom().nextInt(i + 1);
            EquipmentSlot temp = slots.get(i);
            slots.set(i, slots.get(j));
            slots.set(j, temp);
        }

        return new ArrayList<>(slots.subList(0, Math.min(maxPieces, slots.size())));
    }

    private static double calculateArmorSetScore(List<ItemStack> armorSet, List<EquipmentSlot> slots) {
        double totalScore = 0.0;
        for (int i = 0; i < armorSet.size(); i++) {
            ItemStack stack = armorSet.get(i);
            if (!stack.isEmpty()) {
                totalScore += calculateArmorScore(stack, slots.get(i));
            }
        }
        return totalScore;
    }

    private static List<List<ItemStack>> collectArmorCandidateSets(List<EquipmentSlot> slots) {
        List<List<ItemStack>> candidateSets = new ArrayList<>();
        for (EquipmentSlot slot : slots) {
            candidateSets.add(collectArmorCandidates(slot));
        }
        return candidateSets;
    }

    private static List<ItemStack> selectBestArmorSet(List<EquipmentSlot> slots, List<List<ItemStack>> candidateSets, double targetScore) {
        List<ItemStack> bestSet = new ArrayList<>();
        double[] bestDifference = { Double.MAX_VALUE };

        selectBestArmorSetRecursive(slots, candidateSets, targetScore, 0, new ArrayList<>(), bestDifference, bestSet);
        return bestSet;
    }

    private static List<ItemStack> selectArmorSet(Mob mob, double multiplier) {
        int maxPieces = getMaxArmorPieces(multiplier);
        List<EquipmentSlot> slots = selectArmorSlots(mob, maxPieces);

        if (slots.isEmpty()) {
            return new ArrayList<>();
        }

        List<List<ItemStack>> candidateSets = collectArmorCandidateSets(slots);
        for (List<ItemStack> candidates : candidateSets) {
            if (candidates.isEmpty()) {
                return new ArrayList<>();
            }
        }

        double targetScore = getTargetEquipmentScore(multiplier);
        return selectBestArmorSet(slots, candidateSets, targetScore);
    }

    private static void selectBestArmorSetRecursive(
            List<EquipmentSlot> slots,
            List<List<ItemStack>> candidateSets,
            double targetScore,
            int index,
            List<ItemStack> currentSet,
            double[] bestDifference,
            List<ItemStack> bestSet
    ) {
        if (index >= slots.size()) {
            double currentScore = calculateArmorSetScore(currentSet, slots);
            double difference = Math.abs(currentScore - targetScore);

            if (difference < bestDifference[0]) {
                bestDifference[0] = difference;
                bestSet.clear();
                bestSet.addAll(currentSet);
            }
            return;
        }

        List<ItemStack> candidates = candidateSets.get(index);
        for (ItemStack candidate : candidates) {
            currentSet.add(candidate);
            selectBestArmorSetRecursive(slots, candidateSets, targetScore, index + 1, currentSet, bestDifference, bestSet);
            currentSet.remove(currentSet.size() - 1);
        }
    }

    private static double getTargetEquipmentScore(double multiplier) {
        double clamped = Math.max(1.0, Math.min(10.0, multiplier));

        int lower = (int) Math.floor(clamped);
        int upper = (int) Math.ceil(clamped);

        if (lower == upper) {
            return getExactTargetEquipmentScore(lower);
        }

        double lowerScore = getExactTargetEquipmentScore(lower);
        double upperScore = getExactTargetEquipmentScore(upper);
        double progress = clamped - lower;

        return lowerScore + (upperScore - lowerScore) * progress;
    }

    private static double getExactTargetEquipmentScore(int multiplier) {
        return switch (multiplier) {
            case 1 -> 0.0;
            case 2 -> 2.0;
            case 3 -> 4.0;
            case 4 -> 6.0;
            case 5 -> 9.0;
            case 6 -> 12.0;
            case 7 -> 16.0;
            case 8 -> 19.0;
            case 9 -> 22.0;
            case 10 -> 25.0;
            default -> throw new IllegalArgumentException("Multiplier must be between 1 and 10");
        };
    }

    public static List<ItemStack> collectArmorCandidates(EquipmentSlot slot) {
        List<ItemStack> candidates = new ArrayList<>();
        if (!isArmorSlot(slot)) {
            return candidates;
        }

        for (var item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);

            if (stack.is(net.minecraft.world.item.Items.ELYTRA)) {
                continue;
            }

            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SkullBlock) {
                continue;
            }

            if (Mob.getEquipmentSlotForItem(stack) == slot) {
                candidates.add(stack);
            }
        }

        return candidates;
    }

    private static void selectAndEquipArmorSet(Mob mob, double multiplier, EquipmentTier tier) {
        List<ItemStack> armorSet = selectArmorSet(mob, multiplier);
        if (armorSet.isEmpty()) {
            return;
        }

        for (ItemStack armor : armorSet) {
            if (armor.isEmpty()) {
                continue;
            }

            EquipmentSlot slot = Mob.getEquipmentSlotForItem(armor);
            if (tier == EquipmentTier.ENCHANTED) {
                armor = applyEnchantment(mob, armor);
            }

            mob.setItemSlot(slot, armor.copy());
        }
    }

    private static double calculateArmorScore(ItemStack stack, EquipmentSlot slot) {
        double score = 0.0;
        var modifiers = stack.getAttributeModifiers(slot);

        for (var entry : modifiers.entries()) {
            var attribute = entry.getKey();
            var modifier = entry.getValue();

            if (modifier.getOperation() != net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION) {
                continue;
            }

            String attributeId = attribute.getDescriptionId();
            if ("attribute.name.generic.armor".equals(attributeId) || "attribute.name.generic.armor_toughness".equals(attributeId)) {
                score += Math.abs(modifier.getAmount());
            }
        }

        return score;
    }

    private static ItemStack applyEnchantment(Mob mob, ItemStack stack) {
        return EnchantmentHelper.enchantItem(mob.getRandom(), stack, 30, false);
    }

    private static double calculateWeaponScore(ItemStack stack) {
        double score = 0.0;
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);

        for (var entry : modifiers.entries()) {
            var attribute = entry.getKey();
            var modifier = entry.getValue();

            if (modifier.getOperation() != net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION) {
                continue;
            }

            if ("attribute.name.generic.attack_damage".equals(attribute.getDescriptionId())) {
                score += Math.abs(modifier.getAmount());
            }
        }

        return score;
    }

    private static EquipmentTier selectTier(Mob mob, double multiplier) {
        double[] chances = getChances(multiplier);
        double roll = mob.getRandom().nextDouble();

        if (roll < chances[0]) {
            return EquipmentTier.NORMAL;
        } else if (roll < chances[0] + chances[1]) {
            return EquipmentTier.HIGH_QUALITY;
        } else {
            return EquipmentTier.ENCHANTED;
        }
    }

    private static double[] getChances(double multiplier) {
        int lower = (int) Math.floor(multiplier);
        int upper = (int) Math.ceil(multiplier);

        if (lower == upper) {
            return getExactChances(lower);
        }

        double[] lowerChances = getExactChances(lower);
        double[] upperChances = getExactChances(upper);
        double progress = multiplier - lower;

        return new double[]{
                interpolate(lowerChances[0], upperChances[0], progress),
                interpolate(lowerChances[1], upperChances[1], progress),
                interpolate(lowerChances[2], upperChances[2], progress)
        };
    }

    private static double[] getExactChances(int multiplier) {
        return switch (multiplier) {
            case 1 -> new double[]{ 0.90, 0.10, 0.00 };
            case 2 -> new double[]{ 0.82, 0.16, 0.02 };
            case 3 -> new double[]{ 0.74, 0.22, 0.04 };
            case 4 -> new double[]{ 0.66, 0.27, 0.07 };
            case 5 -> new double[]{ 0.58, 0.31, 0.11 };
            case 6 -> new double[]{ 0.51, 0.34, 0.15 };
            case 7 -> new double[]{ 0.44, 0.36, 0.20 };
            case 8 -> new double[]{ 0.38, 0.37, 0.25 };
            case 9 -> new double[]{ 0.33, 0.37, 0.30 };
            case 10 -> new double[]{ 0.28, 0.37, 0.35 };
            default -> throw new IllegalArgumentException("Multiplier must be between 1 and 10");
        };
    }

    private static double interpolate(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private enum EquipmentTier {
        NORMAL,
        HIGH_QUALITY,
        ENCHANTED
    }
}