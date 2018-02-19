package id.nullpointr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.org.apache.xml.internal.utils.URI;

public class MainClass {
	
	public static void main(String[] args) {
		if(args.length <=0) {
			System.out.println("ERROR ");
		}else {
			BufferedReader bufferredReadder = null;
			FileReader fileReader = null;
			
			ExecutorService executor = Executors.newFixedThreadPool(30);
			
			String monitoring=args[0];
			
			if(monitoring.equalsIgnoreCase("MONITORING")) {
			
			try {
				String path=args[1];
				path=path.trim();
				System.out.println("Path : "+path);
				path=path.replaceAll(" ", "%20");
				System.out.println("Path Replace : "+path);
				fileReader = new FileReader(new URI(path).getPath());
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
					outputPath=outputPath.replaceAll(" ","%20");
					if(connectionData.length>6) {
						String nodeName=connectionData[6];
						executor.execute(new JmxDataGetter(connectionUrl, username, password, interval, maxCount, outputPath,nodeName));
					}else {
						executor.execute(new JmxDataGetter(connectionUrl, username, password, interval, maxCount, outputPath));
					}
					
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
		}else if("SUMMARY".equalsIgnoreCase(monitoring)){
			try {
			new SummaryMonitoring().summary(args[1], args[2], args[3]);
			System.out.println("Finish Process Transaction");
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
}
