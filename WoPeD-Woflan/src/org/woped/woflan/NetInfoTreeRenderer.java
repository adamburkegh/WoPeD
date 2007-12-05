package org.woped.woflan;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.woped.editor.controller.vc.NetInfo;
import org.woped.language.Messages;

@SuppressWarnings("serial")
public class NetInfoTreeRenderer extends DefaultTreeCellRenderer
{
	Icon iconInfo = null;
	Icon iconOK = null;
	Icon iconERROR = null;
	
	public NetInfoTreeRenderer()
	{
	    iconOK = Messages.getImageIcon("Analysis.Tree.Okay");
	    iconInfo = Messages.getImageIcon("Analysis.Tree.Info");
	    iconERROR = Messages.getImageIcon("Analysis.Tree.Warning");		
	}
	
	public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {
		
		super.getTreeCellRendererComponent(
				tree, value, sel,
				expanded, leaf, row,
				hasFocus);
		Icon newIcon = GetIconForItem(value);
		if (newIcon!=null)		
			setIcon(newIcon);		 	
		return this;
	}	
	private Icon GetIconForItem(Object value)
	{
		Icon result = null;
		try 
		{
			// Cast to NetInfo
			// If this doesn't work we have
			// a problem.
			// We will return the default icon in this case
			NetInfo myNetInfo = (NetInfo)value;
			int infoType = myNetInfo.GetInfoState();
			switch (infoType)
			{
			case NetInfo.InfoStateERROR:
				result = iconERROR;
				break;
			case NetInfo.InfoStateOK:
				result = iconOK;
				break;
			case NetInfo.InfoStateInfo:
				result = iconInfo;
				break;
			};
		}
		catch (Exception e)
		{
		}
		return result;
	}
}
