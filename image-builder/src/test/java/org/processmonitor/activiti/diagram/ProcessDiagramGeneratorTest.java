package org.processmonitor.activiti.diagram;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.junit.Ignore;


public class ProcessDiagramGeneratorTest extends PluggableActivitiTestCase {

	private static String FINANCIALREPORT_PROCESS_KEY = "financialReport";

	private List<String> processInstanceIds;

	protected void setUp() throws Exception {
		super.setUp();
		repositoryService
				.createDeployment()
				.addClasspathResource(
						"org/activiti/examples/bpmn/usertask/FinancialReportProcess.bpmn20.xml")
				.addClasspathResource(
						"org/activiti/examples/bpmn/usertask/FinancialReportProcess.png")
				.deploy();

		processInstanceIds = new ArrayList<String>();

		for (int i = 0; i < 4; i++) {
			processInstanceIds.add(runtimeService.startProcessInstanceByKey(
					FINANCIALREPORT_PROCESS_KEY, "BUSINESS-KEY-" + i).getId());
		}
	}

	protected void tearDown() throws Exception {
		for (org.activiti.engine.repository.Deployment deployment : repositoryService
				.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
		super.tearDown();
	}
	
	@Ignore("Generator provides platform dependent images (fonts) see http://forums.activiti.org/en/viewtopic.php?f=6&t=4647&start=0")
	public void testGenerateProcessDefinition() throws IOException {
	    String id = FINANCIALREPORT_PROCESS_KEY;
	    DiagramLayerGenerator generator = new BasicProcessDiagramGenerator( (RepositoryServiceImpl) repositoryService );
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put(AbstractProcessDiagramLayerGenerator.PROCESS_DEFINITION_ID, id);
	    
	    InputStream expectedStream = new FileInputStream("src/test/resources/org/processmonitor/activiti/diagram/BasicProcessDiagramGeneratorTest.testSimpleProcessDefinition.png" );
	    assertTrue(isEqual(expectedStream, generator.generateLayer("png", params)));
	}
	
	public void testOneNodeHighlight() throws IOException {
	    String id = FINANCIALREPORT_PROCESS_KEY;
	    DiagramLayerGenerator generator = new HighlightNodeDiagramLayer( (RepositoryServiceImpl) repositoryService );
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put( HighlightNodeDiagramLayer.PROCESS_DEFINITION_ID, id);
	    List<String> highlightedActivities = new ArrayList<String>();
	    highlightedActivities.add("writeReportTask");
	    params.put( HighlightNodeDiagramLayer.HIGHLIGHTED_ACTIVITIES, highlightedActivities);
	    
	    InputStream expectedStream = new FileInputStream("src/test/resources/org/processmonitor/activiti/diagram/HighlightNodeDiagramLayer.testOneNodeHighlight.png" );   
	    assertTrue( isEqual(expectedStream, generator.generateLayer("png", params)));	    
	}
	
	public void testMergeLayers() throws IOException {
	    DiagramLayerGenerator generator = new MergeLayersGenerator();
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put( "img1", getBytes("src/test/resources/org/processmonitor/activiti/diagram/MergeLayerGenerator.testMergeLayers1.png"));
	    params.put( "img2", getBytes("src/test/resources/org/processmonitor/activiti/diagram/MergeLayerGenerator.testMergeLayers2.png"));
	    
	    InputStream expectedStream = new FileInputStream("src/test/resources/org/processmonitor/activiti/diagram/MergeLayerGenerator.testMergeLayers.png" );  
	 	assertTrue( isEqual(expectedStream, generator.generateLayer("png", params)));
	}
	
	/**
	 * get byte[] from fileName
	 * @param string
	 * @return
	 */
	private Object getBytes(String fileName) {
	    //create file object
	    File file = new File( fileName );
	      FileInputStream fin = null;
		 try
		    {
		      //create FileInputStream object
			 fin = new FileInputStream(fileName);
		     
		      /*
		       * Create byte array large enough to hold the content of the file.
		       * Use File.length to determine size of the file in bytes.
		       */
		     
		     
		       byte fileContent[] = new byte[(int)file.length()];
		     
		       /*
		        * To read content of the file in byte array, use
		        * int read(byte[] byteArray) method of java FileInputStream class.
		        *
		        */
		       fin.read(fileContent);
		     
		       return fileContent;
		    }
		    catch(FileNotFoundException e)
		    {
		      System.out.println("File not found" + e);
		    }
		    catch(IOException ioe)
		    {
		      System.out.println("Exception while reading the file " + ioe);
		    } finally {
		    	if (fin != null) {
		    		try {
						fin.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		    	}
		    		
		    }
		 	return null;
		 }
	

	@Ignore("Generator provides platform dependent images (fonts) see http://forums.activiti.org/en/viewtopic.php?f=6&t=4647&start=0")
	public void ignoreTestOneNodeCount() throws IOException {
	    String id = repositoryService.createProcessDefinitionQuery().processDefinitionKey( FINANCIALREPORT_PROCESS_KEY ).singleResult().getId();
	    DiagramLayerGenerator generator = new WriteNodeDescriptionDiagramLayer( (RepositoryServiceImpl) repositoryService );
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put( WriteNodeDescriptionDiagramLayer.PROCESS_DEFINITION_ID, id);
	    params.put( "writeReportTask", 5);
	    
	    InputStream expectedStream = new FileInputStream("src/test/resources/org/processmonitor/activiti/diagram/WriteCountLayerGeneratorTest.oneNumber.png" );   
	    assertTrue( isEqual(expectedStream, generator.generateLayer("png", params)));
	    
	}
	
	@Ignore("Generator provides platform dependent images (fonts) see http://forums.activiti.org/en/viewtopic.php?f=6&t=4647&start=0")
	public void ignoreTestProcessDiagramMerge() throws IOException {
		byte[][] isLayers = new byte[3][];
		InputStream producedStream = null;
		InputStream expectedStream = null;
		
		try {
			//generate process diagram
		    DiagramLayerGenerator generator = new BasicProcessDiagramGenerator( (RepositoryServiceImpl) repositoryService );
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put(AbstractProcessDiagramLayerGenerator.PROCESS_DEFINITION_ID, FINANCIALREPORT_PROCESS_KEY);
		    isLayers[0] = generator.generateLayer("png", params);
		    
		    // highlight one node layer
		    generator = new HighlightNodeDiagramLayer( (RepositoryServiceImpl) repositoryService );
		    params.clear();
		    params.put( HighlightNodeDiagramLayer.PROCESS_DEFINITION_ID, FINANCIALREPORT_PROCESS_KEY);
		    List<String> highlightedActivities = new ArrayList<String>();
		    highlightedActivities.add("writeReportTask");
		    params.put( HighlightNodeDiagramLayer.HIGHLIGHTED_ACTIVITIES, highlightedActivities);
		    isLayers[1] = generator.generateLayer("png",params);
		    
		    //write string into one node
		    generator = new WriteNodeDescriptionDiagramLayer( (RepositoryServiceImpl) repositoryService );
		    params.clear();
		    params.put( WriteNodeDescriptionDiagramLayer.PROCESS_DEFINITION_ID, FINANCIALREPORT_PROCESS_KEY);
		    params.put( "writeReportTask", 5);
		    isLayers[2] = generator.generateLayer("png", params);
		    
		    // merge layers into process diagram and compare to expected results
		    (new MergeImages()).merge("target/test-classes/tmpProcessDiagramMerge.png", "png", isLayers);
		    expectedStream = new FileInputStream("src/test/resources/org/processmonitor/activiti/diagram/mergedProcessDiagram.png" );		    
		    producedStream = new FileInputStream("target/test-classes/tmpProcessDiagramMerge.png" );
		    
		    //assertTrue( isEqual(expectedStream, producedStream ));
		} finally 
		{
			if (expectedStream != null)
				expectedStream.close();
			if ( producedStream != null)
				producedStream.close();
		}
	}

	  static boolean isEqual(InputStream stream1, byte[] byteArray)
	          throws IOException {

		  ByteArrayInputStream stream2 = new ByteArrayInputStream( byteArray);
		  
	      ReadableByteChannel channel1 = Channels.newChannel(stream1);
	      ReadableByteChannel channel2 = Channels.newChannel(stream2);

	      ByteBuffer buffer1 = ByteBuffer.allocateDirect(1024);
	      ByteBuffer buffer2 = ByteBuffer.allocateDirect(1024);

	      try {
	          while (true) {

	              int bytesReadFromStream1 = channel1.read(buffer1);
	              int bytesReadFromStream2 = channel2.read(buffer2);

	              if (bytesReadFromStream1 == -1 || bytesReadFromStream2 == -1) 
	            	  return bytesReadFromStream1 == bytesReadFromStream2;

	              buffer1.flip();
	              buffer2.flip();

	              for (int i = 0; i < Math.min(bytesReadFromStream1, bytesReadFromStream2); i++)
	                  if (buffer1.get() != buffer2.get())
	                      return false;

	              buffer1.compact();
	              buffer2.compact();
	          }
	      } catch (IOException e) {
	    	  System.err.println( e.getStackTrace());
	    	  throw e;
	      } finally {
	          if (stream1 != null) stream1.close();
	          if (stream2 != null) stream2.close();
	      }
	  }

		
	
/**	  public void testQueryByProcessDefinitions() throws IOException {
		    String id = repositoryService.createProcessDefinitionQuery().processDefinitionKey( FINANCIALREPORT_PROCESS_KEY ).singleResult().getId();
		    
		    ReadOnlyProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(id);
		    List<String> highlightedActivities = new ArrayList();
		    for ( PvmActivity activiti : processDefinition.getActivities() )
		    {
			    long count = runtimeService.createExecutionQuery().processDefinitionKey( FINANCIALREPORT_PROCESS_KEY ).activityId(activiti.getId()).count();
		    	if ( count >0 )
		    		highlightedActivities.add(activiti.getId());
			    List<Execution> executions = runtimeService.createExecutionQuery().processDefinitionKey( FINANCIALREPORT_PROCESS_KEY ).activityId(activiti.getId()).list();
			    for ( Execution execution : executions)
			    {
			    	ExecutionEntity eE = (ExecutionEntity) execution;
			    	
			    }
			    
		    }
		    
		    ProcessDefinitionEntity pde = (ProcessDefinitionEntity) ( ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition( id ));
		    InputStream in = null;
	        FileOutputStream out = null;
		    
		    try {
		    	//ProcessDiagramGenerator processDiagramGenerator = new ProcessDiagramGenerator();
	            in = new ProcessMonitorDiagramGenerator().generateDiagram( pde, "png", highlightedActivities);
	            //in = ProcessDiagramGenerator.generatePngDiagram( pde);
	            out = new FileOutputStream("c:/tools/example.png");
	            int c;

	            while ((c = in.read()) != -1) {
	                out.write((byte)c);
	            }
	        } finally {
	            if (in != null) {
	                in.close();
	            }
	            if (out != null) {
	                out.close();
	            }
	        }
	  }
*/
}
