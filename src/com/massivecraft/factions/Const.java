package com.massivecraft.factions;

import org.bukkit.ChatColor;

public class Const
{
	// ASCII Map
	public static final int MAP_WIDTH = 48;
	public static final int MAP_HEIGHT = 8;
	public static final int MAP_HEIGHT_FULL = 17;
	
	public static final char[] MAP_KEY_CHARS = "\\/#?ç¬£$%=&^ABCDEFGHJKLMNOPQRSTUVWXYZÄÖÜÆØÅ1234567890abcdeghjmnopqrsuvwxyÿzäöüæøåâêîûô".toCharArray();
	public static final String MAP_KEY_WILDERNESS = ChatColor.GRAY.toString() + "-";
	public static final String MAP_KEY_SEPARATOR = ChatColor.AQUA.toString() + "+";
	public static final String MAP_KEY_OVERFLOW = ChatColor.MAGIC.toString() + "-" + ChatColor.RESET.toString();
	public static final String MAP_OVERFLOW_MESSAGE = MAP_KEY_OVERFLOW + ": Too Many Factions (>" + MAP_KEY_CHARS.length + ") on this Map.";
	
}
