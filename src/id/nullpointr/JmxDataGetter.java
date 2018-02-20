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
	
	public JmxDataGetter(String serviceUrl, String username, String password, int interval, int maxCount, String outputPath) {
		this.serviceUrl = serviceUrl;
		this.username = username;
		this.password = password;
		this.interval = interval;
		this.maxCount = maxCount;
		this.outputPath = outputPath;
	}
	
	public JmxDataGetter(String serviceUrl, String username, String password, int interval, int maxCount, String outputPath,String nodename) {
		this.serviceUrl = serviceUrl;
		this.username = username;
		this.password = password;
		this.interval = interval;
		this.maxCount = maxCount;
		this.outputPath = outputPath;
		this.nodeNameAdditional=nodename;
		
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
				
				StringBuffer heapData = new StringBuffer();
				heapData.append(dateFormat.format(date));
				heapData.append(",");
				heapData.append(memory.get("init"));
				heapData.append(",");
				heapData.append(memory.get("committed"));
				heapData.append(",");
				heapData.append(memory.get("used"));
				heapData.append(",");
				heapData.append(memory.get("max"));
				
			    writer.append(heapData.toString());
			    writer.newLine();
			    
			    
			    /* Flush tiap n jumlah data.
			     * Ketika flush data langsung ditulis ke file, biar data nggak ilang ketika ctrl+c atau kill */
			    
			    if(count % 10 == 0) {
			    	writer.flush();
			    	System.out.println(fullpathFile+" flushing!!");
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
