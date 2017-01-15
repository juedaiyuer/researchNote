package org.lobobrowser.html.domimpl;

import org.lobobrowser.html.FormInput;
import org.w3c.dom.DOMException;
import org.w3c.dom.html2.HTMLElement;
import org.w3c.dom.html2.HTMLOptionsCollection;
import org.w3c.dom.html2.HTMLSelectElement;

public class HTMLSelectElementImpl extends HTMLBaseInputElement implements
		HTMLSelectElement {
	public HTMLSelectElementImpl(String name) {
		super(name);
	}

	public void add(HTMLElement element, HTMLElement before)
			throws DOMException {
		this.insertBefore(element, before);
	}

	public int getLength() {
		return this.getOptions().getLength();
	}

	public boolean getMultiple() {
		InputContext ic = this.inputContext;
		if(ic != null) {
			return ic.getMultiple();
		}
		else {
			return false;
		}
	}

	private HTMLOptionsCollection options;
	
	public HTMLOptionsCollection getOptions() {
		synchronized(this) {
			if(this.options == null) {
				this.options = new HTMLOptionsCollectionImpl(this);
			}
			return this.options;
		}
	}

	public int getSelectedIndex() {
		InputContext ic = this.inputContext;
		if(ic != null) {
			return ic.getSelectedIndex();
		}
		else {
			return -1;
		}
	}

	public int getSize() {
		InputContext ic = this.inputContext;
		if(ic != null) {
			return ic.getVisibleSize();
		}
		else {
			return 0;
		}
	}

	public String getType() {
		return this.getMultiple() ? "select-multiple" : "select-one";
	}

	public void remove(int index) {
		try {
			this.removeChild(this.getOptions().item(index));
		} catch(DOMException de) {
			this.warn("remove(): Unable to remove option at index " + index + ".", de);
		}
	}

	public void setLength(int length) throws DOMException {
		this.getOptions().setLength(length);
	}

	public void setMultiple(boolean multiple) {
		InputContext ic = this.inputContext;
		if(ic != null) {
			ic.setMultiple(multiple);
		}
	}

	private int deferredSelectedIndex = -1;
	
	public void setSelectedIndex(int selectedIndex) {
		InputContext ic = this.inputContext;
		if(ic != null) {
			ic.setSelectedIndex(selectedIndex);
		}
		else {
			this.deferredSelectedIndex = selectedIndex;
		}
	}

	public void setSize(int size) {
		InputContext ic = this.inputContext;
		if(ic != null) {
			ic.setVisibleSize(size);
		}
	}

	protected FormInput[] getFormInputs() {
		// Needs to be overriden for forms to submit.
		String name = this.getName();
		if(name == null) {
			return null;
		}
		return new FormInput[] { new FormInput(name, this.getValue()) };
	}	
	
	public void resetInput() {
		InputContext ic = this.inputContext;
		if(ic != null) {
			ic.resetInput();
		}
	}

	public void setInputContext(InputContext ic) {
		ic.setSelectedIndex(this.deferredSelectedIndex);
	}
}
