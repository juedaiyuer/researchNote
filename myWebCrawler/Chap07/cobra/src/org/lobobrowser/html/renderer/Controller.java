package org.lobobrowser.html.renderer;

import org.lobobrowser.html.domimpl.*;
import java.awt.event.*;

/**
 * A controller that takes care of renderer events.
 */
interface Controller {	
	public boolean onPressed(ModelNode node, InputEvent event, int x, int y);
	public boolean onEnterPressed(ModelNode node, InputEvent event);
	public boolean onMouseClick(ModelNode node, MouseEvent event, int x, int y);
	public boolean onMouseDown(ModelNode node, MouseEvent event, int x, int y);
	public boolean onMouseUp(ModelNode node, MouseEvent event, int x, int y);
	public boolean onMouseDisarmed(ModelNode node, MouseEvent event);	
}
