/*
    GNU LESSER GENERAL PUBLIC LICENSE
    Copyright (C) 2006 The XAMJ Project

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Contact info: xamjadmin@users.sourceforge.net
*/
package org.lobobrowser.html.renderer;

import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.domimpl.*;
import java.awt.*;

public class RImgControl extends RUIControl {
	public RImgControl(ModelNode me, UIControl widget, RenderableContainer container, FrameContext frameContext, UserAgentContext ucontext) {
		super(me, widget, container, frameContext, ucontext);
	}

	protected void applyStyle() {
		super.applyStyle();
		//TODO: Should borderInsets be moved to RenderState?
		if(this.borderInsets == null) {
			HTMLElementImpl imageElement = (HTMLElementImpl) this.modelNode;
			String borderText = imageElement.getAttribute("border");
			if(borderText != null && borderText.length() != 0) {
				int border;
				try {
					//TODO: Percentages supported?
					border = Integer.parseInt(borderText);
				} catch(NumberFormatException nfe) {
					// Ignore?
					border = 0;
				}
				this.borderInsets = new Insets(border, border, border, border);
			}
		}
	}

	
}
