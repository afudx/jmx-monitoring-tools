package id.nullpointr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat; 
import java.util.Date;
import java.util.HashMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxDataGetter implements Runnable{
	HashMap map = new HashMap<>();
	String credentials[] = new String[2];
	
	String serviceUrl = null;
	String username = null;
	String password = null;
	int maxCount = 0;
	int interval = 1;
	String outputPath = null;
	String nodeNameAdditional=null;
	AudioPlayer audio = null;
	float heapTreshold = 0;
	
	public JmxDataGetter(String serviceUrl, String username, String password, int interval, int maxCount, String outputPath) {
		this.serviceUrl = serviceUrl;
		this.username = username;
		this.password = password;
		this.interval = interval;
		this.maxCount = maxCount;
		this.outputPath = outputPath;
	}
	
	public JmxDataGetter(String serviceUrl, String username, String password, int interval, int maxCount, String outputPath,String nodename, float heapTreshold) {
		this.serviceUrl = serviceUrl;
		this.username = username;
		this.password = password;
		this.interval = interval;
		this.maxCount = maxCount;
		this.outputPath = outputPath;
		this.nodeNameAdditional=nodename;
		this.heapTreshold = heapTreshold;
		
		audio = new AudioPlayer("audio.wav", 2000);
	}
	
	public void proceed() {
		credentials[0] = username;
		credentials[1] = password;
		
		map.put("jmx.remote.credentials",credentials);
		
		JMXConnector connector = null;
		BufferedWriter writer = null;
		Object jmxMemoryObject = null;
		Object jmxHostnameObject = null;
		Object jmxNodeName = null;
		int count = 1;
		
		try {
			System.out.println("Connecting to: "+serviceUrl);
			connector = JMXConnectorFactory.newJMXConnector(new JMXServiceURL(serviceUrl), map);
			connector.connect();
			
			if(nodeNameAdditional.toLowerCase().contains("crappie") || nodeNameAdditional.toLowerCase().contains("bowfin")) {
				jmxHostnameObject = connector.getMBeanServerConnection().getAttribute(new ObjectName("jboss.as:core-service=server-environment"), "hostName");
				jmxNodeName = connector.getMBeanServerConnection().getAttribute(new ObjectName("jboss.as:core-service=server-environment"), "nodeName");
			}
			
			String hostname = jmxHostnameObject != null ? (String) jmxHostnameObject : nodeNameAdditional.split("_")[0];
			
			String nodeName = nodeNameAdditional != null ? nodeNameAdditional : (String) jmxNodeName;
			String fullpathFile = outputPath + File.separator + nodeName + ".csv";
			
			System.out.println("Writing to: "+fullpathFile);
			
			writer = new BufferedWriter(new FileWriter(fullpathFile));
			writer.append("timestamp,init,committed,used,max");
			writer.newLine();
			
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = null;
			
			while(count <= maxCount) {
				jmxMemoryObject = connector.getMBeanServerConnection().getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
				CompositeData memory = (CompositeData) jmxMemoryObject;
				date = new Date();
				
				float init = Float.parseFloat(memory.get("init").toString());
				float committed = Float.parseFloat(memory.get("committed").toString());
				float used = Float.parseFloat(memory.get("used").toString());
				float max = Float.parseFloat(memory.get("max").toString());
				
				StringBuffer heapData = new StringBuffer();
				heapData.append(dateFormat.format(date));
				heapData.append(",");
				heapData.append(init);
				heapData.append(",");
				heapData.append(committed);
				heapData.append(",");
				heapData.append(used);
				heapData.append(",");
				heapData.append(max);
				
			    writer.append(heapData.toString());
			    writer.newLine();
			    
			    
			    /* Flush tiap n jumlah data.
			     * Ketika flush data langsung ditulis ke file, biar data nggak ilang ketika ctrl+c atau kill */
			    
			    if(count % 10 == 0) {
			    	writer.flush();
			    	System.out.println(fullpathFile+" flushing!!");
			    }
			    
			    float currentHeapPresentage = (used / init) * 100;
			    
			    if(currentHeapPresentage > heapTreshold) {
			    	System.out.println("Heap usage of "+nodeNameAdditional+" reach more than heap treshold ("+heapTreshold+")");
			    	System.out.println("Current heap utilization: "+currentHeapPresentage+"%");
			    	
			    	audio.play();
			    }
			    	
			    
				Thread.sleep(1000 * interval);
				count++;
			}
			writer.flush();
	    	System.out.println(fullpathFile+" flushing!!");
			
		} catch (AttributeNotFoundException | InstanceNotFoundException | MalformedObjectNameException | MBeanException
				| ReflectionException | IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				connector.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	@Override
	public void run() {
		proceed();
	}

}
