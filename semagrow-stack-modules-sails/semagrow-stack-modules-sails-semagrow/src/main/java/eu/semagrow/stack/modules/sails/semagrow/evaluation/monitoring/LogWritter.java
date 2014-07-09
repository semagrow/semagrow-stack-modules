package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;

/**
 * 
 * @author Giannis Mouchakis
 *
 */
public class LogWritter implements Runnable {

	BlockingQueue<String> queue;
	private Boolean finished;
	
	static final Path path = Paths.get(System.getProperty("user.home"), "semagrow_logs.log");
	static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
	private BufferedWriter writer;

	public LogWritter(BlockingQueue<String> queue) {
		this.queue = queue;
		finished = false;
		try {
			writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (  ! finished  ) {
			try {
				writer.write(queue.take());
				writer.newLine();
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		// empty remaining items in queue if any
		while (!queue.isEmpty()) {
			try {
				writer.write(queue.remove());
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// close writer (and flush remains in file)
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void finish() {
		finished = true;
	}

}
