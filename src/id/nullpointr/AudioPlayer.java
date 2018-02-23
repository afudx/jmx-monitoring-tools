package id.nullpointr;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {
	private String audioFilePath = "";
	private long playTime = 0;
	
	public AudioPlayer(String audioFile, long playTime) {
		this.audioFilePath = audioFile;
		this.playTime = playTime;
	}
	
	public void play() {
		File audioFile = new File(audioFilePath);
		AudioInputStream audioStream = null;
		AudioFormat format = null;
		Clip audioClip = null;
		
		try {
			
			audioStream = AudioSystem.getAudioInputStream(audioFile);
			audioClip = (Clip) AudioSystem.getClip();
			audioClip.open(audioStream);
			audioClip.loop(Clip.LOOP_CONTINUOUSLY);
			audioClip.start();
			
			Thread.sleep(playTime);
			
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			
			try {
				audioClip.close();
				audioStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
