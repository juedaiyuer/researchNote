/*
    GNU LESSER GENERAL PUBLIC LICENSE
    Copyright (C) 2006 The Lobo Project

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
package org.lobobrowser.html.style;

import org.lobobrowser.html.domimpl.HTMLElementImpl;
import org.lobobrowser.util.gui.*;

public class BodyRenderState extends BlockRenderState {
	public BodyRenderState(RenderState prevRenderState, HTMLElementImpl element) {
		super(prevRenderState, element);
	}

	private BackgroundInfo backgroundInfo = INVALID_BACKGROUND_INFO;

	public void invalidate() {
		super.invalidate();
		this.backgroundInfo = INVALID_BACKGROUND_INFO;
	}
	
	public BackgroundInfo getBackgroundInfo() {
		BackgroundInfo binfo = this.backgroundInfo;
		if(binfo != INVALID_BACKGROUND_INFO) {
			return binfo;
		}
		binfo = super.getBackgroundInfo();
		if(binfo == null || binfo.backgroundColor == null) {
			String bgcolor = this.element.getAttribute("bgcolor");
			if(bgcolor != null && bgcolor.length() != 0) {
				if(binfo == null) {
					binfo = new BackgroundInfo();
				}
				binfo.backgroundColor = ColorFactory.getInstance().getColor(bgcolor);
			}
		}
		this.backgroundInfo = binfo;
		return binfo;
	}
}
