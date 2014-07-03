package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Queue;

public class LogWritter implements Runnable {

	Queue<String> queue;
	private Boolean finished;
	
	static final Path path = Paths.get(System.getProperty("user.home"), "semagrow_logs.log");
	static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
	private BufferedWriter writer;

	public LogWritter(Queue<String> queue) {
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
		synchronized (queue) {
			while ( true ) {
				if (! queue.isEmpty() ) {
					try {
						writer.write(queue.remove());
						writer.newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						try {
							writer.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (finished) {
							try {
								writer.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						}
						queue.wait();
					} catch (InterruptedException e) {
						System.out.println("interapted");
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void finish() {
		finished = true;
	}

}
