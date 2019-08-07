package oPBoT;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class OPBoT {
	
	public static void main(String args[]) throws Exception {

		JDA jda = new JDABuilder("*").build();
		jda.addEventListener(new MyEventListener());
	}

	public static void sendToUser(User user, String message) {
		user.openPrivateChannel().queue((channel) -> {
			channel.sendMessage(message).queue();
		});
	}
	
	public static void sendToChannel(TextChannel channel, String message) {
		channel.sendMessage(message).queue();
	}
}