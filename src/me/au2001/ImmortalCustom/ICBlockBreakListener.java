package me.au2001.ImmortalCustom;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ICBlockBreakListener {

	public static ArrayList<ICBlockBreakListener> listeners = new ArrayList<ICBlockBreakListener>();
	
	public ICBlockBreakListener () {
		listeners.add(this);
	}

	public void onSilkTouch (ICBlockBreakEvent event) {}
	public void onAutoSmelt (ICBlockBreakEvent event) {}
	public void onItemBreak (ICBlockBreakEvent event) {}
	public void onBlockBreak (ICBlockBreakEvent event) {}
	public void preBlockBreak (ICBlockBreakEvent event) {}
	public void onBlockFortune (ICBlockBreakEvent event) {}
	public void onBlockBrokenUpdate (ICBlockBreakEvent event) {}
	public boolean onDropDown (ICBlockBreakEvent event, ItemStack drop) { return true; }
	public boolean onAutoPickup (ICBlockBreakEvent event, ItemStack drop) { return true; }
	
	public static class ICBlockBreakEvent extends BlockBreakEvent {

		boolean tool, minebow, silktouch, smelted, fortuned, autopickup, full, dropdown, expdrop, itembreak;
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		
		public ICBlockBreakEvent (BlockBreakEvent event) {
			this(event.getBlock(), event.getPlayer());
		}
		
		public ICBlockBreakEvent (Block block, Player player) {
			super(block, player);
		}
		
		public boolean isTool () { return tool; }
		public boolean isMineBow () { return minebow; }
		public boolean hasSilkTouched () { return silktouch; }
		public boolean hasSmelted () { return smelted; }
		public boolean hasFortuned () { return fortuned; }
		public boolean isAutoPickup () { return autopickup; }
		public boolean isFull () { return full; }
		public boolean hasDropDown () { return dropdown; }
		public boolean hasExp () { return expdrop; }
		public boolean hasItemBreak () { return itembreak; }

		public ArrayList<ItemStack> getDrops () { return drops; }

		public void preBlockBreak () {
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					listener.preBlockBreak(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void onBlockBreak () {
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					listener.onBlockBreak(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void onBlockBrokenUpdate () {
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					listener.onBlockBrokenUpdate(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void onSilkTouch () {
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					listener.onSilkTouch(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void onAutoSmelt () {
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					listener.onAutoSmelt(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void onBlockFortune () {
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					listener.onBlockFortune(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public boolean onAutoPickup (ItemStack drop) {
			boolean result = true;
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					result = result && listener.onAutoPickup(this, drop);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return result;
		}
		
		public boolean onDropDown (ItemStack drop) {
			boolean result = true;
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					result = result && listener.onDropDown(this, drop);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return result;
		}
		
		public void onItemBreak () {
			for (ICBlockBreakListener listener : ICBlockBreakListener.listeners) {
				try {
					listener.onItemBreak(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
