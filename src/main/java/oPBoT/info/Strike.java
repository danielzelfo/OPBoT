package oPBoT.info;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;

import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Strike {
	private HashMap<String, Integer> strikeNumbers = new HashMap<>();
	private JSONObject userInfo;
	private JSONObject strikeInfo;
	
	public Strike(GuildMessageReceivedEvent e) throws FileNotFoundException, IOException, ParseException {
		//getting user info from json file
		this.userInfo = (JSONObject) (new JSONParser().parse(new FileReader("usersInfo.json")));
		this.strikeInfo =  (JSONObject) userInfo.get("strikeInfo") ;
		for(User mensionedUser: e.getMessage().getMentionedUsers())
			setStrikeNumber(mensionedUser.getId(), strikeInfo.get(mensionedUser.getId()) == null ? 0 : Integer.parseInt(String.valueOf(strikeInfo.get(mensionedUser.getId()))));
	}
	
	
	public int getStrikeNumber(String userId) {
		return strikeInfo.get(userId) == null ? 0 : strikeNumbers.get(userId);
	}

	public void setStrikeNumber(String userId, int strikeNumber) throws FileNotFoundException, IOException, ParseException {
		this.userInfo = (JSONObject) (new JSONParser().parse(new FileReader("usersInfo.json")));
		
		strikeNumbers.put(userId, strikeNumber);
		strikeInfo.put(userId, strikeNumber);
		
		userInfo.put("strikeInfo", strikeInfo.toJSONString());
		
		try (FileWriter file = new FileWriter("usersInfo.json")) {
            file.write(userInfo.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
}
