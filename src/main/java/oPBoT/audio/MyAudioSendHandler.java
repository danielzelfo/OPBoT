package oPBoT.audio;

import net.dv8tion.jda.core.audio.AudioSendHandler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;


/**
 * This is a wrapper around AudioPlayer which makes it behave as an AudioSendHandler for JDA. As JDA calls canProvide
 * before every call to provide20MsAudio(), we pull the frame in canProvide() and use the frame we already pulled in
 * provide20MsAudio().
 */
public class MyAudioSendHandler implements AudioSendHandler {
	private final AudioPlayer audioPlayer;
	private AudioFrame lastFrame;

	/**
	 * @param audioPlayer Audio player to wrap.
	 */
	public MyAudioSendHandler(AudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	public boolean canProvide() {
		if (lastFrame == null) {
			lastFrame = audioPlayer.provide();
		}

		return lastFrame != null;
	}

	public byte[] provide20MsAudio() {
		if (lastFrame == null) {
			lastFrame = audioPlayer.provide();
		}

		byte[] data = lastFrame != null ? lastFrame.getData() : null;
		lastFrame = null;

		return data;
	}

	public boolean isOpus() {
		return true;
	}
}