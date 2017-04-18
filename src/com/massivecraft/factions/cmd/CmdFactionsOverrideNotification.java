package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Factions;
import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.command.type.primitive.TypeBooleanYes;
import com.massivecraft.massivecore.util.IdUtil;
import com.massivecraft.massivecore.util.Txt;

public class CmdFactionsOverrideNotification extends FactionsCommand
{
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public CmdFactionsOverrideNotification()
	{
		// Aliases
		this.addAliases("adminNotification");

		// Parameters
		this.addParameter(TypeBooleanYes.get(), "on/off", "flip");
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public void perform() throws MassiveException
	{
		// Args
		boolean target = this.readArg(!msender.areOverrideNotificationsEnabled());
		
		// Apply
		msender.setOverrideNotificationsEnabled(target);
		
		// Inform
		String desc = Txt.parse(msender.areOverrideNotificationsEnabled() ? "<g>ENABLED" : "<b>DISABLED");
		
		String messageYou = Txt.parse("<i>%s %s <i>override mode notifications.", msender.getDisplayName(msender), desc);
		String messageLog = Txt.parse("<i>%s %s <i>override mode notifications.", msender.getDisplayName(IdUtil.getConsole()), desc);
		
		msender.message(messageYou);
		Factions.get().log(messageLog);
	}
	
}
