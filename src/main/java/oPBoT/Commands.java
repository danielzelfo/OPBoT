package oPBoT;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Timer;

import org.json.simple.parser.ParseException;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import oPBoT.info.Strike;

public class Commands {
	private JDA jda;
	private GuildMessageReceivedEvent event;
	private String userName;
	private AudioManager audioManager;
	private VoiceChannel connectedChannel;
	private String secondaryCommand;
	private Member selfMember;
	
	public Commands(JDA jda) {
		this.jda = jda;
		this.event = null;
		this.userName = "unknown";
		this.audioManager = null;
		this.connectedChannel = null;
		this.secondaryCommand = null;
		this.selfMember = null;
	}
	
	public Commands(GuildMessageReceivedEvent e) {
		this.jda = null;
		this.event = e;
		this.userName = e.getMember().getUser().getName();
		this.audioManager = e.getGuild().getAudioManager();
		this.connectedChannel = e.getGuild().getSelfMember().getVoiceState().getChannel();
		this.secondaryCommand = getSecondaryCommand(e.getMessage().getContentRaw().split(" "));
		this.selfMember = e.getGuild().getSelfMember();
	}

	public void ping() {
		OPBoT.sendToChannel(event.getChannel(), "Pong " + event.getJDA().getPing());
		return;
	}

	public void join() {
		//checking if bot has permission to join
		if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.VOICE_CONNECT)) {
			OPBoT.sendToChannel(event.getChannel(), "I do not have permissions to join a voice channel.");
			return;
		}
		VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
		// checking if user is in a channel
		if (connectedChannel == null) {
			OPBoT.sendToChannel(event.getChannel(), "You are not connected to a voice channel, " + userName + ".");
			return;
		}
		// Checking is bot is already trying to connect
		if (audioManager.isAttemptingToConnect()) {
			OPBoT.sendToChannel(event.getChannel(), "The bot is already trying to connect, " + userName + ".");
			return;
		}
		//joining
		audioManager.openAudioConnection(connectedChannel);
		OPBoT.sendToChannel(event.getChannel(), "Connected to the voice channel!");
		return;
	}

	public void leave() {
		String userName = event.getMember().getUser().getName();
		AudioManager audioManager = event.getGuild().getAudioManager();
		VoiceChannel connectedChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
		// Checks if the bot is connected to a voice channel.
		if (connectedChannel == null) {
			// Get slightly fed up at the user.
			OPBoT.sendToChannel(event.getChannel(), "I am not connected to a voice channel, " + userName + ".");
			return;
		}
		// Disconnect from the channel.
		audioManager.closeAudioConnection();
		// Notify the user.
		OPBoT.sendToChannel(event.getChannel(), "Disconnected from the voice channel!");
		return;
	}

	public void play() {
		if (connectedChannel == null) {
			join();
		}
		//audioManager.setSendingHandler(new MySendHandler());
		return;
	}

	public void kick() {
		//no users mentioned
		if (event.getMessage().getMentionedMembers().isEmpty()) {
			OPBoT.sendToChannel(event.getChannel(), "You must mension at least 1 person to be kicked, " + userName + ".");
			return;
		}

		//user does not have permission to ban
		if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
			OPBoT.sendToChannel(event.getChannel(), "You do not have permission to kick users, " + userName + ".");
			return;
		}

		//bot does not have permission to kick members
		if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
			OPBoT.sendToChannel(event.getChannel(), "I do not have permission to kick members.");
			return;
		}

		//looping through each member
		for (User user: event.getMessage().getMentionedUsers()) {
			Member member = event.getGuild().getMember(user);

			//user being kicked is of higher hierarchy
			if (!event.getMember().canInteract(member)) {
				OPBoT.sendToChannel(event.getChannel(), "You cannot kick: " + member.getEffectiveName() + "because they are higher in the hierarchy than you.");
				continue;
			}

			//kicking user
			event.getGuild().getController().kick(member).queue(
				success -> OPBoT.sendToChannel(event.getChannel(), "Kicked " + member.getEffectiveName() + "."),
				//error kicking
				error -> {
					if (error instanceof PermissionException) {

						OPBoT.sendToChannel(event.getChannel(), "Permission error occured while kicking " + member.getEffectiveName() + ".\n" + error.getMessage());
					} else {
						OPBoT.sendToChannel(event.getChannel(), "Unknown error occured while kicking " + member.getEffectiveName() + ".\n" + error.getClass().getSimpleName() + "\n" + error.getMessage());
					}
				});
		}

		return;
	}
	
	//BAN METHOD
	/*
	 * deletionDays is for number of days back messages will be deleted
	 */
	
	public void ban(User user, Guild guild, int deletionDays, TextChannel channel) {
		guild.getController().ban(guild.getMember(user), deletionDays).queue(
				success -> OPBoT.sendToChannel(channel, "<@" + user.getId() + "> has been banned."),
				//error banning
				error -> {
					if (error instanceof PermissionException)
						OPBoT.sendToChannel(channel, "Permission error occured while banning <@" + user.getId() + ">.\n " + error.getMessage());
					else
						OPBoT.sendToChannel(channel, "Unknown error occured while banning <@" + user.getId() + ">.\n" + error.getClass().getSimpleName() + "\n" + error.getMessage());
				}
			);
		
	}
	
	
	public void ban() {
		ban( secondaryCommand.split(" ").length >= 2 ? Integer.parseInt(secondaryCommand.split(" ")[secondaryCommand.split(" ").length - 1]) : 0 );
	}
	
	public void ban(int deletionDays) {
		//no users mentioned
		if (event.getMessage().getMentionedMembers().isEmpty()) {
			OPBoT.sendToChannel(event.getChannel(), "You must mension at least 1 person to be banned, " + userName + ".");
			return;
		}

		//user does not have permission to ban
		if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
			OPBoT.sendToChannel(event.getChannel(), "You do not have permission to ban users, " + userName + ".");
			return;
		}

		//bot does not have the permission to ban
		if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
			OPBoT.sendToChannel(event.getChannel(), "I do not have permission to ban members.");
			return;
		}
		
		//looping through each member
		for (User user: event.getMessage().getMentionedUsers()) {
			Member member = event.getGuild().getMember(user);

			//user being banned is of higher hierarchy
			if (!event.getMember().canInteract(member)) {
				OPBoT.sendToChannel(event.getChannel(), "You cannot ban: " + member.getEffectiveName() + "because they are higher in the hierarchy than you.");
				continue;
			}

			ban(user, event.getGuild(), deletionDays, event.getChannel());
		}
	}
	
	

	//UNBAN COMMAND
	public void unban() {
		 unban(secondaryCommand.replace("<@", "").replace("> ", ""));
	}
	public void unban(String userID) {
		//trying to unban more than one user at a time
		if ((secondaryCommand.length() - secondaryCommand.replace("<@", "").length()) > 2) {
			OPBoT.sendToChannel(event.getChannel(), "You can only unban one user at a time, " + userName + ".");
			return;
		}
		//bot does not have permission to unban
		if (!selfMember.hasPermission(Permission.BAN_MEMBERS)) {
			OPBoT.sendToChannel(event.getChannel(), "I do not have permission to unban members.");
			return;
		}

		//user does not have permission to unban
		if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
			OPBoT.sendToChannel(event.getChannel(), "You do not have permission to unban users. You must have the Ban Members permission, " + userName + ".");
			return;
		}


		event.getGuild().getController().unban(userID).reason("Unbanned By " + event.getMember().getEffectiveName()).queue(
			success -> {

				OPBoT.sendToChannel(event.getChannel(), "<@" + userID + "> has been unbanned.");
			},
			//error unbanning
			error -> {

				if (error instanceof PermissionException) {
					OPBoT.sendToChannel(event.getChannel(), "Permission Error unbanning user.\n" + error.getMessage());
				} else {
					OPBoT.sendToChannel(event.getChannel(), "Unknown error while unbanning user.\n" + error.getClass().getSimpleName() + "\n" + error.getMessage());
				}
			}
		);

		return;
	}

	//INVITE COMMAND
	/*
	 * USER MUST IN THE SERVER !!!
	 * THIS COMMAND IS TO INVITE A USER TO ANOTHER SERVER
	 */
	public void invite() {
		String inviteCode = secondaryCommand.split(" ")[secondaryCommand.split(" ").length - 1];
		if (inviteCode.length() != 6) {
			
			return;
		}
		for (User user: event.getMessage().getMentionedUsers()) {
			OPBoT.sendToUser(user,
				userName + " has invited you to a server!\n" +
				"https://discord.gg/" + inviteCode
			);

		}
	}


	//STRIKE METHOD
	/*
	 * STRIKE 1 = PRIVATE MESSAGE WARNING WITH DETAULT ON STRIKES
	 * STRIKE 2 = PRIVATE MESSAGE + 2 DAY SUSPENSION
	 * STRIKE 3 = PRIVATE MESSAGE + BAN 
	 */
	public void strike(Strike usersInfo) throws FileNotFoundException, IOException, ParseException {
		//no users mentioned
		if (event.getMessage().getMentionedMembers().isEmpty()) {
			OPBoT.sendToChannel(event.getChannel(), "You must mension at least 1 person to be banned, " + userName + ".");
			return;
		}

		//user does not have permission to ban
		if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
			OPBoT.sendToChannel(event.getChannel(), "You do not have permission to strike users, " + userName + ". You must have the permission to ban members");
			return;
		}

		//bot does not have the permission to ban
		if (!selfMember.hasPermission(Permission.BAN_MEMBERS) || !selfMember.hasPermission(Permission.KICK_MEMBERS)) {
			OPBoT.sendToChannel(event.getChannel(), "I do not have permission to give members strikes. I need the permission to kick members and ban members");
			return;
		}
		for (User user: event.getMessage().getMentionedUsers()) {
			Member member = event.getGuild().getMember(user);
			
			//user being banned is of higher hierarchy
			if (!event.getMember().canInteract(member)) {
				OPBoT.sendToChannel(event.getChannel(), "You cannot give <@" + user.getId() + "> a strike because they are higher in the hierarchy than you.");
				continue;
			}
			
			usersInfo.setStrikeNumber(user.getId(), usersInfo.getStrikeNumber(user.getId()) + 1 );
			OPBoT.sendToChannel(event.getChannel(), "Strike " + usersInfo.getStrikeNumber(user.getId()) + ", <@" + user.getId() + ">" + (usersInfo.getStrikeNumber(user.getId()) >= 3 ? "\nYOU'RE OUT!" : ""));
			switch(usersInfo.getStrikeNumber(user.getId())) {
				case 1:
					OPBoT.sendToUser(user, "You have recieved your first strike from the server " + event.getGuild().getName() + "\n"
										 + "Recieving a second strike will result in a 2 day suspension!"
					);
					break;
				case 2:
					OPBoT.sendToUser(user, "You have recieved your second strike from the server " + event.getGuild().getName() + "\n"
							+ "You have been banned from " + event.getGuild().getName() + " for 2 days :/\n" 
							+ "Recieving a third strike will result in a ban!"
					);
					suspend(0, 2);
					break;
				case 3:
					OPBoT.sendToUser(user, "You have recieved your third strike from the server " + event.getGuild().getName() + "\n"
							+ "You have been banned from " + event.getGuild().getName() + "."
					);
					ban(7);
					break;
					
				default:
					OPBoT.sendToUser(user, "You have recieved your third strike from the server " + event.getGuild().getName() + "\n"
							+ "You have been banned from " + event.getGuild().getName() + "."
					);
					ban(7);
					break;
					
			}
		}
		
		
	}
	
	//SUSPEND METHOD
	/*
	 * !suspend <int duration> <optional int deleteDays>
	 * 
	 * ISSUE -- suspension delay to unban goes away once a new message event happens!
	 */
	
	//this method is used by the EventListener
	public void suspend() {
		int numberMembersMensioned = event.getMessage().getMentionedMembers().size();
		if( secondaryCommand.split(" ").length == numberMembersMensioned + 1 ) {
			suspend(Integer.parseInt( secondaryCommand.split(" ")[secondaryCommand.split(" ").length - 1]), 0);
			return;
		}
		if ( secondaryCommand.split(" ").length == numberMembersMensioned + 2) {
			suspend(Integer.parseInt( secondaryCommand.split(" ")[secondaryCommand.split(" ").length - 2]), Integer.parseInt( secondaryCommand.split(" ")[secondaryCommand.split(" ").length - 1]));
			return;
		}
	}
	
	//this method is used by the strike method
	public void suspend(int duration, int deletionDays) {
		//no users mentioned
		if (event.getMessage().getMentionedMembers().isEmpty()) {
			OPBoT.sendToChannel(event.getChannel(), "You must mension at least 1 person to be suspended, " + userName + ".");
			return;
		}
		
		for(User suspendedUser: event.getMessage().getMentionedUsers()) {
			OPBoT.sendToChannel(event.getChannel(), "<@"+suspendedUser.getId() + "> will be suspended for " + duration + " days.");
			suspend(event.getMessage().getTextChannel(),  suspendedUser.getId(), event.getGuild().getId(), duration, deletionDays);
		}
		
		return;
	}
	
	//this method is used by the EventScheduler
	public void suspend(TextChannel textChannel, String userId, String guildId, int duration, int deletionDays) {
		
		User user = jda.getUserById(userId);
		//user does not have permission to ban
		if (!textChannel.getGuild().getMember(user).hasPermission(Permission.BAN_MEMBERS)) {
			OPBoT.sendToChannel(textChannel, "You do not have permission to suspend members, " + userName + ". You must have the Ban Members permission.");
			return;
		}

		//bot does not have the permission to ban
		if (!textChannel.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
			OPBoT.sendToChannel(textChannel, "I do not have permission to ban members.");
			return;
		}
		ban(user, jda.getGuildById(guildId), deletionDays, textChannel);

		/*
		 * 
		 * schedule UNBAN !!!
		 * 
		 */
		
		return;
	}
	
	
	private String getSecondaryCommand(String[] args) {
		String newArgs = "";
		for (int i = 1; i < args.length; i++) {
			newArgs += args[i] + " ";
		}
		return newArgs;
	}
}