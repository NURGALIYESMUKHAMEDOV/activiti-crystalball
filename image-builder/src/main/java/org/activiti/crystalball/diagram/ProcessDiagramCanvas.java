package org.activiti.crystalball.diagram;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;



public class ProcessDiagramCanvas extends org.activiti.engine.impl.bpmn.diagram.ProcessDiagramCanvas {
	
	  public ProcessDiagramCanvas(int width, int height) {
		  super( width, height);
	  }


	  public ProcessDiagramCanvas(int width, int height, int minX, int minY) {
		  super(width, height, minX, minY);
	  }


	public void drawStringToNode(String count, int x, int y, int width, int height) {
	    // count
	    if (count != null) {
	      String text = fitTextToWidth(count, width);
	      int textX = x + ((width - fontMetrics.stringWidth(text)) / 2);
	      int textY = y + fontMetrics.getHeight();
	      g.drawString(text, textX, textY);
	    }		
	}
	  
	public void drawHighLight(int x, int y, int width, int height, Color color) {
		Paint originalPaint = g.getPaint();
		Stroke originalStroke = g.getStroke();

		g.setPaint( color);
		g.setStroke(THICK_TASK_BORDER_STROKE);

		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width,
				height, 20, 20);
		g.draw(rect);

		g.setPaint(originalPaint);
		g.setStroke(originalStroke);
	}
}
