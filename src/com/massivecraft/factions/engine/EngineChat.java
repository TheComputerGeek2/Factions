package com.massivecraft.factions.engine;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.chat.ChatFormatter;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.massivecore.Engine;
import com.massivecraft.massivecore.event.EventMassiveCorePlayerToRecipientChat;
import com.massivecraft.massivecore.util.MUtil;

public class EngineChat extends Engine
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static EngineChat i = new EngineChat();
	public static EngineChat get() { return i; }
	public EngineChat()
	{
		this.setPlugin(Factions.get());
	}
	
	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public void setActiveInner(boolean active)
	{
		if (!active) return;
		
		// If chat formatting is enabled in the MConf ...
		if (MConf.get().chatSetFormat)
		{
			Bukkit.getPluginManager().registerEvent(
				AsyncPlayerChatEvent.class,
				this,
				MConf.get().chatSetFormatAt,
				new EventExecutor()
				{
					@Override
					public void execute(Listener listener, Event event) throws EventException
					{
						try
						{
							if (!(event instanceof AsyncPlayerChatEvent)) return;
							((AsyncPlayerChatEvent) event).setFormat(MConf.get().chatSetFormatTo);
						}
						catch (Throwable t)
						{
							throw new EventException(t);
						}
					}
				},
				Factions.get(),
				true
			);
		}
		
		// If chat tag parsing is enabled in the MConf ...
		if (MConf.get().chatParseTags)
		{
			Bukkit.getPluginManager().registerEvent(
				AsyncPlayerChatEvent.class,
				this,
				MConf.get().chatParseTagsAt,
				new EventExecutor()
				{
					@Override
					public void execute(Listener listener, Event event) throws EventException
					{
						try
						{
							// If this is an AsyncPlayerChatEvent
							if (!(event instanceof AsyncPlayerChatEvent)) return;
							AsyncPlayerChatEvent casted = (AsyncPlayerChatEvent) event;
							
							// And the chatter is actually a player
							Player player = casted.getPlayer();
							if (MUtil.isntPlayer(player)) return;
							
							// Format the chat
							String format = casted.getFormat();
							format = ChatFormatter.format(format, player, null);
							casted.setFormat(format);
						}
						catch (Throwable t)
						{
							throw new EventException(t);
						}
					}
				},
				Factions.get(),
				true
			);
			
			Bukkit.getPluginManager().registerEvent(
				EventMassiveCorePlayerToRecipientChat.class,
				this,
				EventPriority.NORMAL,
				new EventExecutor()
				{
					@Override
					public void execute(Listener listener, Event event) throws EventException
					{
						try
						{
							// If this is an EventMassiveCorePlayerToRecipientChat ...
							if (!(event instanceof EventMassiveCorePlayerToRecipientChat)) return;
							EventMassiveCorePlayerToRecipientChat casted = (EventMassiveCorePlayerToRecipientChat) event;
							
							// Format the message
							String format = casted.getFormat();
							format = ChatFormatter.format(format, casted.getSender(), casted.getRecipient());
							casted.setFormat(format);
						}
						catch (Throwable t)
						{
							throw new EventException(t);
						}
					}
				},
				Factions.get(),
				true
			);
		}
	}
	
	// -------------------------------------------- //
	// UTIL
	// -------------------------------------------- //
	
	@Deprecated
	public static void setFormat(AsyncPlayerChatEvent event)
	{
		event.setFormat(MConf.get().chatSetFormatTo);
	}
	
	@Deprecated
	public static void parseTags(AsyncPlayerChatEvent casted)
	{
		Player player = casted.getPlayer();
		if (MUtil.isntPlayer(player)) return;
		
		String format = casted.getFormat();
		format = ChatFormatter.format(format, player, null);
		casted.setFormat(format);
	}
	
	@Deprecated
	public static void parseRelcolor(EventMassiveCorePlayerToRecipientChat event)
	{
		String format = event.getFormat();
		format = ChatFormatter.format(format, event.getSender(), event.getRecipient());
		event.setFormat(format);
	}
	
}
