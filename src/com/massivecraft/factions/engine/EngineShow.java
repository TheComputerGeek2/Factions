package com.massivecraft.factions.engine;

import com.massivecraft.factions.cmd.CmdFactions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsChunkChangeType;
import com.massivecraft.factions.event.EventFactionsFactionShowAsync;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.massivecore.Button;
import com.massivecraft.massivecore.Engine;
import com.massivecraft.massivecore.PriorityLines;
import com.massivecraft.massivecore.collections.MassiveList;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.mson.Mson;
import com.massivecraft.massivecore.util.TimeDiffUtil;
import com.massivecraft.massivecore.util.TimeUnit;
import com.massivecraft.massivecore.util.Txt;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EngineShow extends Engine
{
	// -------------------------------------------- //
	// CONSTANTS
	// -------------------------------------------- //
	private static final String BASENAME = "factions";
	private static final String BASENAME_ = BASENAME+"_";
	
	private static final String ID_ID = BASENAME_ + "id";
	private static final String ID_DESCRIPTION = BASENAME_ + "description";
	private static final String ID_AGE = BASENAME_ + "age";
	private static final String ID_FLAGS = BASENAME_ + "flags";
	private static final String ID_POWER = BASENAME_ + "power";
	private static final String ID_LANDVALUES = BASENAME_ + "landvalue";
	private static final String ID_BANK = BASENAME_ + "bank";
	private static final String ID_FOLLOWERS = BASENAME_ + "followers";
	private static final String ID_RELATIONS = BASENAME_ + "relations";
	
	private static final int PRIORITY_ID = 1000;
	private static final int PRIORITY_DESCRIPTION = 2000;
	private static final int PRIORITY_AGE = 3000;
	private static final int PRIORITY_FLAGS = 4000;
	private static final int PRIORITY_POWER = 5000;
	private static final int PRIORITY_LANDVALUES = 6000;
	private static final int PRIORITY_BANK = 7000;
	private static final int PRIORITY_FOLLOWERS = 9000;
	private static final int PRIORITY_RELATIONS = 10000;
	
	private static final String KEY_VALUE_PARSE_STRING = "<a>%s: <i>%s";
	
	private static final String FLAG_DESCRIPTION_DEFAULT = Txt.parse("<silver><italic>default");
	private static final String FLAG_DESCRIPTION_GLUE = Txt.parse(" <i>| ");
	
	// TODO should this be configurable or possibly specifyable by the player somehow?
	private static final int LIMIT_AGE_UNIT_COUNT = 3;
	
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static EngineShow i = new EngineShow();
	public static EngineShow get() { return i; }

	// -------------------------------------------- //
	// FACTION SHOW
	// -------------------------------------------- //

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFactionShow(EventFactionsFactionShowAsync event)
	{
		final CommandSender sender = event.getSender();
		final MPlayer mplayer = event.getMPlayer();
		final Faction faction = event.getFaction();
		final boolean normal = faction.isNormal();
		final Map<String, PriorityLines> idPriorityLines = event.getIdPriorityLiness();

		// ID
		if (mplayer.isOverriding()) addLinesId(idPriorityLines, faction);

		// DESCRIPTION
		addLinesDescription(idPriorityLines, faction);

		// SECTION: NORMAL
		if (normal) addLinesNormal(idPriorityLines, faction);
		
		// FOLLOWERS
		addLinesFollowers(idPriorityLines, faction, sender, normal);
		
		// RELATIONS
		addLinesRelations(idPriorityLines, faction, sender);
	}

	private static void addLineToMap(Map<String, PriorityLines> idPriorityLines, String id, int priority, String key, String value)
	{
		String line = Txt.parse(KEY_VALUE_PARSE_STRING, key, value);
		PriorityLines priorityLine = new PriorityLines(priority, line);
		idPriorityLines.put(id, priorityLine);
	}
	
	private static void addLinesId(Map<String, PriorityLines> idPriorityLines, Faction faction)
	{
		String factionId = faction.getId();
		addLineToMap(idPriorityLines, ID_ID, PRIORITY_ID, "ID", factionId);
	}
	
	private static void addLinesAge(Map<String, PriorityLines> idPriorityLines, Faction faction)
	{
		long ageMillis = faction.getCreatedAtMillis() - System.currentTimeMillis();
		LinkedHashMap<TimeUnit, Long> ageUnitsRaw = TimeDiffUtil.unitcounts(ageMillis, TimeUnit.getAllButMillis());
		Map<TimeUnit, Long> ageUnitcounts = TimeDiffUtil.limit(ageUnitsRaw, LIMIT_AGE_UNIT_COUNT);
		String ageDesc = TimeDiffUtil.formatedVerboose(ageUnitcounts, "<i>");
		addLineToMap(idPriorityLines, ID_AGE, PRIORITY_AGE, "Age", ageDesc);
	}
	
	private static void addLinesDescription(Map<String, PriorityLines> idPriorityLines, Faction faction)
	{
		String factionDescription = faction.getDescription();
		addLineToMap(idPriorityLines, ID_DESCRIPTION, PRIORITY_DESCRIPTION, "Description", factionDescription);
	}
	
	private static void addLinesNormal(Map<String, PriorityLines> idPriorityLines, Faction faction)
	{
		// AGE
		addLinesAge(idPriorityLines, faction);
		
		// FLAGS
		addLinesFlag(idPriorityLines, faction);
		
		// POWER
		addLinesPower(idPriorityLines, faction);
		
		// SECTION: ECON
		if (Econ.isEnabled())
		{
			// LANDVALUES
			addLinesLandValue(idPriorityLines, faction);
			
			// BANK
			if (MConf.get().bankEnabled) addLinesBank(idPriorityLines, faction);
		}
	}
	
	private static void addLinesFlag(Map<String, PriorityLines> idPriorityLines, Faction faction)
	{
		// We display all editable and non default ones. The rest we skip.
		Collection<String> flagDescs = new MassiveList<>();
		for (Entry<MFlag, Boolean> entry : faction.getFlags().entrySet())
		{
			MFlag mflag = entry.getKey();
			if (mflag == null) continue;
			
			Boolean value = entry.getValue();
			if (value == null) continue;
			
			if (!mflag.isInteresting(value)) continue;
			
			String flagDesc = Txt.parse(value ? "<g>" : "<b>") + mflag.getName();
			flagDescs.add(flagDesc);
		}
		
		String flagsDesc = flagDescs.isEmpty() ? FLAG_DESCRIPTION_DEFAULT : Txt.implode(flagDescs, FLAG_DESCRIPTION_GLUE);
		
		addLineToMap(idPriorityLines, ID_FLAGS, PRIORITY_FLAGS, "Flags", flagsDesc);
	}
	
	private static void addLinesPower(Map<String, PriorityLines> idPriorityLines, Faction faction)
	{
		double powerBoost = faction.getPowerBoost();
		
		String boost;
		if (powerBoost == 0D) boost = "";
		else boost = (powerBoost > 0D ? " (bonus: " : " (penalty: ") + powerBoost + ")";
		
		int landCount = faction.getLandCount();
		int powerRounded = faction.getPowerRounded();
		int powerMaxRounded = faction.getPowerMaxRounded();
		String powerValues = Txt.parse("%d/%d/%d%s", landCount, powerRounded, powerMaxRounded, boost);
		String powerDesc = "Land / Power / Maxpower";
		addLineToMap(idPriorityLines, ID_POWER, PRIORITY_POWER, powerDesc, powerValues);
	}
	
	private static void addLinesLandValue(Map<String, PriorityLines> idPriorityLines, Faction faction)
	{
		List<String> landValueLines = new MassiveList<>();
		long landCount = faction.getLandCount();
		for (EventFactionsChunkChangeType type : EventFactionsChunkChangeType.values())
		{
			Double money = MConf.get().econChunkCost.get(type);
			if (money == null || money == 0) continue;
			money *= landCount;
			
			String word = "Cost";
			if (money <= 0)
			{
				word = "Reward";
				money *= -1;
			}
			
			String key = Txt.parse("Total Land %s %s", type.toString().toLowerCase(), word);
			String value = Txt.parse("<h>%s", Money.format(money));
			String line = Txt.parse(KEY_VALUE_PARSE_STRING, key, value);
			landValueLines.add(line);
		}
		PriorityLines priorityLine = new PriorityLines(PRIORITY_LANDVALUES, landValueLines);
		idPriorityLines.put(ID_LANDVALUES, priorityLine);
	}
	
	private static void addLinesBank(Map<String, PriorityLines> idPriorityLines, Faction faction)
	{
		double bank = Money.get(faction);
		String bankDesc = Txt.parse("<h>%s", Money.format(bank, true));
		addLineToMap(idPriorityLines, ID_BANK, PRIORITY_BANK, "Bank", bankDesc);
	}
	
	private static void addLinesFollowers(Map<String, PriorityLines> idPriorityLines, Faction faction, CommandSender sender, boolean normal)
	{
		int followerCountOnline = 0;
		int followerCountOffline = 0;
		
		List<MPlayer> followers = faction.getMPlayers();
		for (MPlayer follower : followers)
		{
			if (follower.isOnline(sender))
			{
				followerCountOnline++;
			}
			else if (normal)
			{
				// For the non-faction we skip the offline members since they are far to many (infinite almost)
				followerCountOffline++;
			}
		}
		
		String factionName = ChatColor.stripColor(faction.getName());
		String headerTotalBase = Txt.parse("<a>Members %s/%s: ", followerCountOnline, followerCountOnline + followerCountOffline);
		Mson msonFollowers = makeMsonMemberList(headerTotalBase, factionName, sender);
		
		PriorityLines priorityLine = new PriorityLines(PRIORITY_FOLLOWERS, msonFollowers);
		idPriorityLines.put(ID_FOLLOWERS, priorityLine);
	}
	
	private static final Button PLAYER_BUTTON = new Button()
		.setName(">")
		.setClicking(true)
		.setCommand(CmdFactions.get().cmdFactionsStatus);
	
	private static Mson makeMsonMemberList(String headerBase, String factionName, CommandSender sender)
	{
		return Mson.fromParsedMessage(headerBase).add(
			PLAYER_BUTTON
			.setArgs("1", factionName, "Time")
			.setSender(sender)
			.render()
		);
	}
	
	private static void addLinesRelations(Map<String, PriorityLines> idPriorityLines, Faction faction, CommandSender sender)
	{
		String factionName = ChatColor.stripColor(faction.getName());
		String headerRelations = Txt.parse("<a>Relations: ");
		Mson msonRelations = Mson.fromParsedMessage(headerRelations).add(
			BUTTON_RELATION
			.setArgs("1", factionName)
			.setSender(sender)
			.render()
		);
		
		PriorityLines priorityLine = new PriorityLines(PRIORITY_RELATIONS, msonRelations);
		idPriorityLines.put(ID_RELATIONS, priorityLine);
	}
	
	private static final Button BUTTON_RELATION = new Button()
		.setName(">")
		.setClicking(true)
		.setCommand(CmdFactions.get().cmdFactionsRelation.cmdFactionsRelationList);
	
}
