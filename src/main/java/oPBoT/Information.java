package oPBoT;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Information {
	private HashMap<String, Integer> strikeNumbers = new HashMap<>();
	private JSONObject jsonInfo;
	
	public Information(GuildMessageReceivedEvent e) throws FileNotFoundException, IOException, ParseException {
		this.jsonInfo = (JSONObject) (new JSONParser().parse(new FileReader("usersInfo.json"))); 
		
		for(Member mensionedMember: e.getMessage().getMentionedMembers()) {
			
			setStrikeNumber(mensionedMember.toString(), jsonInfo.get(mensionedMember.toString()) == null ? 0 : Integer.parseInt(String.valueOf(jsonInfo.get(mensionedMember.toString()))));
		}
	}
	
	
	public int getStrikeNumber(String member) {
		return jsonInfo.get(member) == null ? 0 : strikeNumbers.get(member);
	}

	public void setStrikeNumber(String member, int strikeNumber) {
		strikeNumbers.put(member, strikeNumber);
		jsonInfo.put(member, strikeNumber);
		
		try (FileWriter file = new FileWriter("usersInfo.json")) {
			 
            file.write(jsonInfo.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
}
