package oPBoT.events;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Timer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import oPBoT.OPBoT;
import oPBoT.info.Schedule;
import oPBoT.info.Strike;
import oPBoT.Commands;

public class EventScheduler {
	private JSONObject schedule;
	private ArrayList<Timer> queuedEvents = new ArrayList(schedule.size());
	/*
	 * 24 DAYS ARE THE MAXIMUM EVENT DELAY !!!
	 */
	public EventScheduler(JDA jda) throws FileNotFoundException, IOException, ParseException {
		Schedule savedSchedule = new Schedule();
		
		//need to change JSON to be: schedule > textChannel >  time > userId > action !!!!!!!!!
		String textChannelId = "";
		for (Iterator event = savedSchedule.getSchedule().keySet().iterator(); event.hasNext();) {
			Instant time = Instant.parse((String) event.next());
			Map < Integer, ArrayList < Integer >> scheduledEvent = savedSchedule.getScheduledEvent(time);
			Duration duration = Duration.between(Instant.now(), time);

			scheduleEvent(jda.getTextChannelById(textChannelId), scheduledEvent, duration);
			

		}
	}

	public void scheduleEvent(TextChannel textChannel, Map < Integer, ArrayList < Integer >> event, Duration duration) {
		Commands newCommands = new Commands(textChannel.getJDA());
		int eventIndex = 0;
		for (Iterator subEvent = event.keySet().iterator(); subEvent.hasNext();) {

			int userId = (int) subEvent.next();
			ArrayList < Integer > action = (ArrayList < Integer > ) event.get(userId);

			switch (action.get(0)) {
				case 1:
					queuedEvents.add(
						new Timer((int) duration.toMillis(), new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg) {
								newCommands.kick(userId);
							}
						})
					);
					break;
				case 2:
					queuedEvents.add(
						new Timer((int) duration.toMillis(), new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg) {
								newCommands.ban(userId, action.get(2));
							}
						})
					);
					break;
				case 3:
					queuedEvents.add(
						new Timer((int) duration.toMillis(), new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg) {
								Strike strikeInfo = new Strike(userId);
								newCommands.strike(strikeInfo);
							}
						})
					);
					break;
				case 4:
					queuedEvents.add(
						new Timer((int) duration.toMillis(), new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg) {
								newCommands.suspend(textChannel, Integer.toString(userId), action.get(1), action.get(2));
							}
						})
					);
					break;
				case 5:
					queuedEvents.add(
						new Timer((int) duration.toMillis(), new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg) {
								newCommands.unban(Integer.toString(userId));
							}
						})
					);
					break;
				default:
					//invalid action code
					break;

			}
			queuedEvents.get(eventIndex).setRepeats(false);
			queuedEvents.get(eventIndex).start();
			eventIndex++;
		}
	}

}