package com.massivecraft.factions.engine;

import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.massivecore.Engine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class EngineOverrideNotification extends Engine
{
	private static EngineOverrideNotification i = new EngineOverrideNotification();
	public static EngineOverrideNotification get() { return i; }
	
	@EventHandler
	public void onJoin(PlayerJoinEvent playerJoinEvent)
	{
		MPlayer mPlayer = MPlayerColl.get().get(playerJoinEvent.getPlayer());
		if (!mPlayer.isOverriding()) return;
		if (!mPlayer.areOverrideNotificationsEnabled()) return;
		
		// Warn them that they are in admin mode
		mPlayer.message("Reminder: you have faction override mode enabled");
	}
}
