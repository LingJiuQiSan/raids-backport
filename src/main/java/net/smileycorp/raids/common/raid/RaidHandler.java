package net.smileycorp.raids.common.raid;

import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.smileycorp.raids.common.RaidsContent;
import net.smileycorp.raids.config.EntityConfig;
import net.smileycorp.raids.config.RaidConfig;
import net.smileycorp.raids.config.raidevent.RaidSpawnTable;
import net.smileycorp.raids.config.raidevent.RaidTableLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RaidHandler {

	public static final NonNullList<Class<? extends EntityLiving>> RAIDERS = NonNullList.create();
	
	public static final Map<Class, RaidBuffs> RAID_BUFFS = Maps.newHashMap();
	
	public static void registerRaidBuffs(Class item, RaidBuffs buffs) {
		RAID_BUFFS.put(item, buffs);
	}
	
	public static void addRaider(Class<? extends EntityLiving> entity) {
		if (!RAIDERS.contains(entity) && entity != null) RAIDERS.add(entity);
	}
	
	public static boolean isRaider(Entity entity) {
		return entity == null ? false : RAIDERS.contains(entity.getClass());
	}
	
	public static boolean hasActiveRaid(Entity entity) {
		return isRaider(entity) && entity.getCapability(RaidsContent.RAIDER, null).hasActiveRaid();
	}
	
	public static boolean canBeCaptain(EntityLiving entity) {
		return EntityConfig.getCaptainChance(entity) > 0 || RaidConfig.getCaptainPriority(entity) > 0;
	}
	
	public static void findRaiders(World world, BlockPos pos) {
		for (EntityLiving entity : world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(pos).grow(48.0D), RaidHandler::hasActiveRaid))
			entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 60));
	}
	
	public static void spawnNewWave(Raid raid, BlockPos pos, int wave, boolean isBonusWave) {
		RaidSpawnTable table = raid.getTable();
		if (table == null) return;
		List<EntityLiving> entities = table.getWaveEntities(raid, pos, wave, isBonusWave);
		Collections.shuffle(entities);
		chooseRaidLeader(raid, wave, entities);
	}

	private static void chooseRaidLeader(Raid raid, int wave, List<EntityLiving> entities) {
		EntityLiving captain = null;
		int currentPriority = 0;
		for (EntityLiving entity : entities) {
			int priority = RaidConfig.getCaptainPriority(entity);
			if (priority > currentPriority) {
				captain = entity;
				currentPriority = priority;
			}
		}
		if (captain != null) raid.setLeader(wave, captain);
	}
	
	public static void applyRaidBuffs(EntityLiving entity, Raid raid, int wave, Random rand) {
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = entity.getItemStackFromSlot(slot);
			if (stack == null) continue;
			if (stack.isEmpty()) continue;
			for (Map.Entry<Class, RaidBuffs> buffs : RAID_BUFFS.entrySet())
				if (buffs.getKey().isAssignableFrom(stack.getItem().getClass()))
					stack = buffs.getValue().apply(stack, entity, raid, wave, rand);
			entity.setItemStackToSlot(slot, stack);
		}
	}
	
	public static RaidSpawnTable getSpawnTable(WorldServer world, BlockPos pos, EntityPlayerMP player, Random rand) {
		return RaidTableLoader.INSTANCE.getSpawnTable(RaidContext.Builder.of(world, rand).pos(pos).player(player).build());
	}
	
	public static RaidSpawnTable getSpawnTable(String table) {
		RaidTableLoader tables = RaidTableLoader.INSTANCE;
		if (table == null) return tables.spawnTables().size() > 0 ? tables.spawnTables().first() : null;
		return RaidTableLoader.INSTANCE.getSpawnTable(table);
	}
	
	public interface RaidBuffs {
		
		ItemStack apply(ItemStack stack, EntityLiving entity, Raid raid, int wave, Random rand);
	
	}
	
}
