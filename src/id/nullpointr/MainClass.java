package id.nullpointr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClass {
	
	public static void main(String[] args) {
		if(args.length <=0) {
			
		}else {
			BufferedReader bufferredReadder = null;
			FileReader fileReader = null;
			
			ExecutorService executor = Executors.newFixedThreadPool(30);
			
			try {
				fileReader = new FileReader(args[0]);
				bufferredReadder = new BufferedReader(fileReader);
				String sCurrentLine;
				
				while ((sCurrentLine = bufferredReadder.readLine()) != null) {
					String connectionData[] = sCurrentLine.split(",");
					String connectionUrl = connectionData[0];
					String username = connectionData[1];
					String password = connectionData[2];
					int interval = Integer.parseInt(connectionData[3]);
					int maxCount = Integer.parseInt(connectionData[4]);
					String outputPath= connectionData[5];
					
					executor.execute(new JmxDataGetter(connectionUrl, username, password, interval, maxCount, outputPath));
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
 
				try {
					if (bufferredReadder != null)
						bufferredReadder.close();
					if (fileReader != null)
						fileReader.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				
				executor.shutdown();
				while (!executor.isTerminated()) {
		        }
				
				System.out.println("All process done");

			}
		}
		
	}
}
