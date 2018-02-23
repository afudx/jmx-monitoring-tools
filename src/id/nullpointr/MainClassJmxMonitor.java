package id.nullpointr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainClassJmxMonitor {

	public static void main(String[] args) {
		if (args.length <= 0) {
			System.err.println("Parameter invalid.");
		} else {
			BufferedReader bufferredReadder = null;
			FileReader fileReader = null;

			ExecutorService executor = Executors.newFixedThreadPool(50);

			String monitoring = args[0];

			if (monitoring.equalsIgnoreCase("MONITORING")) {

				try {
					String path = args[1];
					path = path.trim();
					System.out.println("Path : " + path);
					path = path.replaceAll(" ", "%20");
					System.out.println("Path Replace : " + path);
					File file = new File(path);
					fileReader = new FileReader(file.getAbsolutePath());
					bufferredReadder = new BufferedReader(fileReader);
					String sCurrentLine;

					while ((sCurrentLine = bufferredReadder.readLine()) != null) {
						String connectionData[] = sCurrentLine.split(",");
						String connectionUrl = connectionData[0];
						if (!connectionUrl.startsWith("#")) {
							String username = connectionData[1];
							String password = connectionData[2];
							int interval = Integer.parseInt(connectionData[3]);
							int maxCount = Integer.parseInt(connectionData[4]);
							String outputPath = connectionData[5];
							outputPath = outputPath.replaceAll(" ", "%20");
							if (connectionData.length > 6) {
								String nodeName = connectionData[6];
								int heapTreshold = Integer.parseInt(connectionData[7]);
								executor.execute(new JmxDataGetter(connectionUrl, username, password, interval,
										maxCount, outputPath, nodeName,heapTreshold));
							} else {
								executor.execute(new JmxDataGetter(connectionUrl, username, password, interval,
										maxCount, outputPath));
							}
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
			} else if ("SUMMARY".equalsIgnoreCase(monitoring)) {
				SummaryMonitoring summaryMonitoring = new SummaryMonitoring();
				String inputPath = args[1];
				String fileName = "";
				String startTime = args[2];
				String endTime = args[3];
				try {
					summaryMonitoring.summary(inputPath, "", startTime, endTime);
					System.out.println("Finish Process Transaction");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if("SUM-NMON".equals(monitoring)) {
				new SummaryMonitoringNmonMemory();
				System.out.println("FINISH");
			}else {
				System.out.println("NOT MENU");
			}
			

		}
	}
}
