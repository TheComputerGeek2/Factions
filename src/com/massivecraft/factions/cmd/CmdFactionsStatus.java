package com.massivecraft.factions.cmd;

import com.massivecraft.factions.comparator.ComparatorMPlayerInactivity;
import com.massivecraft.factions.cmd.type.TypeFaction;
import com.massivecraft.factions.cmd.type.TypeSortMPlayer;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.Parameter;
import com.massivecraft.massivecore.pager.Pager;
import com.massivecraft.massivecore.pager.Stringifier;
import com.massivecraft.massivecore.util.TimeDiffUtil;
import com.massivecraft.massivecore.util.TimeUnit;
import com.massivecraft.massivecore.util.Txt;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CmdFactionsStatus extends FactionsCommand
{
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public CmdFactionsStatus()
	{
		// Parameters
		this.addParameter(Parameter.getPage());
		this.addParameter(TypeFaction.get(), "faction", "you");
		this.addParameter(TypeSortMPlayer.get(), "sort", "time");
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public void perform() throws MassiveException
	{
		// Args
		int page = this.readArg();
		Faction faction = this.readArg(msenderFaction);
		Comparator<MPlayer> sortedBy = this.readArg(ComparatorMPlayerInactivity.get());

		// MPerm
		if (!MPerm.getPermStatus().has(msender, faction, true)) return;
		
		// Sort list
		final List<MPlayer> mplayers = faction.getMPlayers();
		Collections.sort(mplayers, sortedBy);
		
		// Pager Create
		String title = Txt.parse("<i>Status of %s<i>.", faction.describeTo(msender, true));
		final Pager<MPlayer> pager = new Pager<>(this, title, page, mplayers, new StringifierFactionStatus(msender));
		
		// Pager Message
		pager.messageAsync();
	}
	
	// TODO move this into its own class after the following points are discussed
	// Should this be an msonifier instead?
	// Do we want to use tooltips to provide additional information?
	// If so, what information should it contain?
	// Time until full power?
	// Time until expiration?
	private static class StringifierFactionStatus implements Stringifier<MPlayer>
	{
		private static final String ONLINE_NOW = Txt.parse("<lime>Online right now.");
		
		private final MPlayer msender;
		
		public StringifierFactionStatus(MPlayer msender)
		{
			this.msender = msender;
		}
		
		@Override
		public String toString(MPlayer mplayer, int index)
		{
			// Name
			String displayName = mplayer.getNameAndSomething(this.msender.getColorTo(mplayer).toString(), "");
			int length = 15 - displayName.length();
			length = length <= 0 ? 1 : length;
			String whiteSpace = Txt.repeat(" ", length);
			
			// Power
			double currentPower = mplayer.getPower();
			double maxPower = mplayer.getPowerMax();
			double percent = currentPower / maxPower;
			String color = Txt.parse(getColorString(percent));
			String power = Txt.parse("<art>Power: %s%.0f<gray>/<green>%.0f", color, currentPower, maxPower);
			
			// Time
			long lastActiveMillis = mplayer.getLastActivityMillis() - System.currentTimeMillis();
			Map<TimeUnit, Long> activeTimes = TimeDiffUtil.limit(TimeDiffUtil.unitcounts(lastActiveMillis, TimeUnit.getAllButMillis()), 3);
			String lastActive = mplayer.isOnline(this.msender) ? ONLINE_NOW : Txt.parse("<i>Last active: " + TimeDiffUtil.formatedMinimal(activeTimes, "<i>"));
			
			return Txt.parse("%s%s %s %s", displayName, whiteSpace, power, lastActive);
		}
		
		private static String getColorString(double percent)
		{
			if (percent > 0.75) return "<green>";
			if (percent > 0.5) return "<yellow>";
			if (percent > 0.25) return "<rose>";
			return "<red>";
		}
		
	}
	
}
