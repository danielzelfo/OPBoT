package oPBoT.events;

import oPBoT.Commands;
import oPBoT.OPBoT;
import oPBoT.info.Strike;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;


public class MyEventListener extends ListenerAdapter {
	private static String commandChar = "!";
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		if (e.getMember().getUser().isBot() || !e.getMessage().isFromType(ChannelType.TEXT)) return;
		
		String[] args = e.getMessage().getContentRaw().split(" ");
		Commands command = new Commands(e);
		
		Strike strikesInfo = null;
		try {
			strikesInfo = new Strike(e);
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}
		

		//PING COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "ping")) {
			command.ping();
			return;
		}

		// JOIN COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "join")) {
			command.join();
			return;
		}

		//LEAVE COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "leave")) {
			command.leave();
			return;
		}

		//PLAY COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "play")) {
			command.play();
		}

		//KICK COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "kick")) {
			command.kick();
			return;
		}

		//BAN COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "ban")) {
			command.ban();
			return;
		}

		//UNBAN COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "unban")) {
			command.unban();
			return;
		}

		//INVITE COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "invite")) {
			command.invite();
			return;
		}
		
		//STRIKE COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "strike")) {
			try {
				command.strike(strikesInfo);
			} catch (IOException | ParseException e1) {
				e1.printStackTrace();
				OPBoT.sendToChannel(e.getChannel(), "OPBoT has encountered an issue.");
			}
			return;
		}
		
		//SUSPEND COMMAND
		if (args[0].equalsIgnoreCase(commandChar + "suspend")) {
			command.suspend();
			return;
		}
	}


}