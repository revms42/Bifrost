package org.ajar.bifrost.client.model;

import java.util.List;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class MappingInventory {

	public static final String INVENTORY_KEY = "inventory.location"; 
	
	private List<BifrostPersistenceMapping> inventory;

	public MappingInventory(List<BifrostPersistenceMapping> inventory) {
		super();
		this.inventory = inventory;
	}

	public List<BifrostPersistenceMapping> getInventory() {
		return inventory;
	}

	public void setInventory(List<BifrostPersistenceMapping> inventory) {
		this.inventory = inventory;
	}
	
}
