package tld.sima.armorstand.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import tld.sima.armorstand.Main;
import tld.sima.armorstand.events.created.ArmorstandMovedEvent;

public class RotationClass {
	
	private Main plugin = Main.getPlugin(Main.class);
	private ArrayList<UUID> seenStands;
	
	public void InsertionDegrees(UUID standuuid, double degrees) {
		seenStands = new ArrayList<UUID>();
		moveStand(standuuid, degrees, new Vector(0, 0, 0));
	}
	
	public void moveStand(UUID standuuid, double radians, Vector movement) {
		// Check if stand isn't loaded, or already interacted or the inputs are equal to 0
		if (Bukkit.getEntity(standuuid) == null || (radians == 0.0 && movement.equals(new Vector(0, 0, 0)))) {
			return;
		}
		Location parentLocation = Bukkit.getEntity(standuuid).getLocation().clone();
		
		if(plugin.getSmartParent().containsKey(standuuid)) {
			seenStands.add(standuuid);
			ArrayList<UUID> children = plugin.getSmartParent().get(standuuid);
			for(UUID child : children) {
				Entity childEntity = Bukkit.getEntity(child);
				if(seenStands.contains(child) || childEntity == null) {
					continue;
				}
				Location delta;
				if(radians != 0.0) {
					Location childLocation = childEntity.getLocation();
					Location vector = childLocation.clone().subtract(parentLocation);
					VectorEuler euler = new VectorEuler(vector);
					euler.addRadian(radians * Math.PI/180);
					
					Location a = parentLocation.clone().add(euler.getX(), childLocation.getY(), euler.getZ());
					delta = a.subtract(childLocation);
				}else {
					delta = new Location(childEntity.getWorld(), 0, 0, 0);
				}
				delta.add(movement);
				moveStand(child, radians, delta.toVector());
			}
		}else if (plugin.getParentMap().containsKey(standuuid)) {
			seenStands.add(standuuid);
			int radius = plugin.getParentMap().get(standuuid);
			List<Entity> entities = Bukkit.getEntity(standuuid).getNearbyEntities(radius, radius, radius);
			for(Entity childEntity : entities) {
				if(childEntity == null || seenStands.contains(childEntity.getUniqueId()) || !childEntity.getType().equals(EntityType.ARMOR_STAND)) {
					continue;
				}
				UUID child = childEntity.getUniqueId();
				Location delta;
				if(radians != 0.0) {
					Location childLocation = childEntity.getLocation();
					Location vector = childLocation.clone().subtract(parentLocation);
					VectorEuler euler = new VectorEuler(vector);
					euler.addRadian(radians * Math.PI/180);

					Location a = parentLocation.clone().add(euler.getX(), childLocation.getY(), euler.getZ());
					delta = a.subtract(childLocation);
				}else {
					delta = new Location(childEntity.getWorld(), 0, 0, 0);
				}
				delta.add(movement);
				moveStand(child, radians, delta.toVector());
			}
		}

		parentLocation.add(movement.getX(), 0, movement.getZ());
		parentLocation.setYaw(parentLocation.getYaw() + (float)radians);
		ArmorstandMovedEvent ame = new ArmorstandMovedEvent(Bukkit.getEntity(standuuid), parentLocation, (radians != 0.0));
		plugin.getServer().getPluginManager().callEvent(ame);
		
		if(!ame.isCancelled()) {
			Bukkit.getEntity(standuuid).teleport(parentLocation);
		}
	}
}
