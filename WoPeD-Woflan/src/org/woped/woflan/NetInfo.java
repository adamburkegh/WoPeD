package org.woped.woflan;

import javax.swing.tree.DefaultMutableTreeNode;


public class NetInfo extends DefaultMutableTreeNode {

	public NetInfo(String myNetInfo) {
		super(myNetInfo);
	}
	
	//! This method must be implemented
	//! by all objects derived from NetInfo
	//! It is called when the selection changes
	//! to select the corresponding items in the
	//! graph view
	//! The returned object's toString() method will
	//! be used to derive the identifier of the
	//! graph model element to be selected
	Object[] getReferencedElements() { return new Object[0]; };
}
