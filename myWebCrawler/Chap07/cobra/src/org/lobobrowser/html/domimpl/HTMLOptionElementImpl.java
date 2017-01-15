package org.lobobrowser.html.domimpl;

import org.w3c.dom.html2.*;

public class HTMLOptionElementImpl extends HTMLElementImpl implements HTMLOptionElement {
	public HTMLOptionElementImpl(String name) {
		super(name, true);
	}

	public boolean getDefaultSelected() {
		return this.getAttributeAsBoolean("selected");
	}

	public boolean getDisabled() {
		return false;
	}

	public HTMLFormElement getForm() {
		return this.getForm();
	}

	public int getIndex() {
		Object parent = this.getParentNode();
		if(parent instanceof HTMLSelectElement) {
			HTMLOptionsCollectionImpl options = (HTMLOptionsCollectionImpl) ((HTMLSelectElement) parent).getOptions();
			return options.indexOf(this);
		}
		else {
			return -1;
		}
	}

	public String getLabel() {
		return this.getAttribute("label");
	}

	public boolean getSelected() {
		Object parent = this.getParentNode();
		if(parent instanceof HTMLSelectElement) {
			return ((HTMLSelectElement) parent).getSelectedIndex() == this.getIndex();
		}
		else {
			return false;
		}		
	}

	public String getText() {
		return this.getRawInnerText(false);
	}

	public String getValue() {
		return this.getAttribute("value");
	}

	public void setDefaultSelected(boolean defaultSelected) {
		this.setAttribute("selected", defaultSelected ? "selected" : null);
	}

	public void setDisabled(boolean disabled) {
		//TODO Unsupported
	}

	public void setLabel(String label) {
		this.setAttribute("label", label);
	}

	public void setSelected(boolean selected) {
		Object parent = this.getParentNode();
		if(parent instanceof HTMLSelectElement) {
			if(selected) {	
				((HTMLSelectElement) parent).setSelectedIndex(this.getIndex());
			}
			else {
				((HTMLSelectElement) parent).setSelectedIndex(-1);
			}
		}
	}

	public void setValue(String value) {
		this.setAttribute("value", value);
	}
	
	public String toString() {
		return "HTMLOptionElementImpl[text=" + this.getText() + ",selected=" + this.getSelected() + "]";
	}
}
