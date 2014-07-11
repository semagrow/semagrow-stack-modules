package eu.semagrow.stack.modules.sails.semagrow.evaluation.monitoring.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.lmax.disruptor.EventHandler;

public class LogEventHandler implements EventHandler<LogEvent>
{
	
	static final Path path = Paths.get(System.getProperty("user.home"), "semagrow_logs.log");
	static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
	private BufferedWriter writer;
	
	public LogEventHandler() {
		super();
		try {
			writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
			if (Files.size(path) == 0) {
				//TODO: write headers
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void onEvent(LogEvent event, long sequence, boolean endOfBatch)
    {
    	try {
    		if (event.get() instanceof String) {
    			writer.write( (String) event.get());
    		} else if (event.get() instanceof Integer) {
    			writer.write( (int) event.get());
    		} else {
    			writer.write(event.get().toString());
    		}
			writer.newLine();
			if (endOfBatch) {
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
