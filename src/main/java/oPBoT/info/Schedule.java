package oPBoT.info;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Schedule {
	private JSONObject userInfo;
	private JSONObject schedule;
	
	public Schedule() throws FileNotFoundException, IOException, ParseException {
		//getting user info from json file
		this.userInfo = (JSONObject) (new JSONParser().parse(new FileReader("usersInfo.json")));
		this.schedule =  (JSONObject) userInfo.get("schedule") ;
		
	}
	
	public JSONObject getSchedule() {
		return this.schedule;
	}
	
	public Map<Integer, ArrayList<Integer>> getScheduledEvent(Instant time) throws FileNotFoundException, IOException, ParseException {
		Map<Integer, ArrayList<Integer>> events = new HashMap<>();
		
		//updating userInfo from JSON file
		this.userInfo = (JSONObject) (new JSONParser().parse(new FileReader("usersInfo.json")));
		this.schedule =  (JSONObject) userInfo.get("schedule") ;
		
		JSONObject eventsJSON = (JSONObject) schedule.get(time.toString());
		for(Iterator event = eventsJSON.keySet().iterator(); event.hasNext();) {
		    int userId = (int) event.next();
		    String[] action = ( (String) eventsJSON.get(userId)).split(", ");
		    
		    //converting string array to integer arraylist
		    ArrayList<Integer> actionList = new ArrayList<Integer>(action.length);
		    for(int i = 0; i < action.length; i++)
		      actionList.add(Integer.parseInt(action[i]));
		    
		    //adding to HashMap
		    events.put(userId, actionList);
		}
		return events;
				
	}
	
	/*
	 * time format:
	 * 
	 * event ex: kick, ban ...
	 * int[] : first number represents action
	 * 		kick - 1
	 * 		ban - 2
	 * 		strike - 3
	 * 		suspend -4
	 * 		unban -5
	 * 			numbers after that are parameters
	 * 	ex: [4, 2, 7] = suspend for 2 days and delete last 7 days of messeages sent from the user
	 */
	public void setScheduledEvent(Instant time, String userId, ArrayList<Integer> action) throws FileNotFoundException, IOException, ParseException {
		//updating userInfo from JSON file
		this.userInfo = (JSONObject) (new JSONParser().parse(new FileReader("usersInfo.json")));
		this.schedule =  (JSONObject) userInfo.get("schedule") ;
		
		//getting saved event(s) for time being scheduled
		JSONObject events = schedule.get(time.toString()) == null ? new JSONObject() : (JSONObject) schedule.get(time.toString());
		
		//adding new event
		events.put(userId, action.toString().substring(1, 3*action.size()-1));
		
		//saving to JSON file
		schedule.put(time.toString(), events.toJSONString());
		userInfo.put("schedule", schedule.toJSONString());
		try (FileWriter file = new FileWriter("usersInfo.json")) {
            file.write(userInfo.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
}
