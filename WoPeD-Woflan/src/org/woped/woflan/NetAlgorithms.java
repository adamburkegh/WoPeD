package org.woped.woflan;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.Edge;
import org.jgraph.graph.Port;
import org.woped.core.model.AbstractElementModel;
import org.woped.core.utilities.LoggerManager;

//! This class provides a number of algorithms
//! for dealing with graphs and especially petri-nets
public class NetAlgorithms {
	public static class RouteInfo
	{
		//! Store a reference to the predecessor
		//! on the route back to the source
		//! null if no connection to the source exists
		public RouteInfo m_predecessor = null;
		//! stores the number of arcs between the
		//! source and this element
		//! or -1 if no connection exists
		public int m_nDistanceToSource = -1;
		//! Stores a reference to the actual petri-net
		//! element this entry has been created for
		public AbstractElementModel m_thisElement = null;  
	}
	//! This method calculates all shortest connections between net elements
	//! according to Moore's algorithm. Arc weights are ignored.
	//! @param netElements specifies a collection of net elements that should be 
	//!                    taken into account (we typically don't want all of them)
	//! @param ignoreArcDirection specifies whether the direction of arcs should be
	//!                           taken into account
	//! @return Returns a two-dimensional array of RouteInfo
	//!         containing one row for each element in netElements,
	//!         specifying for each element in netElements what is the distance
	//!         to that element. For each element in a row,
	//!         the shortest route to the element described by the row
	//!         can be reconstructed by following the m_predecessor back-references
	public static RouteInfo[][] GetAllConnections(Collection netElements, boolean ignoreArcDirection)
	{
		int nNumNetElements = netElements.size();
		RouteInfo routeInfo[][] = new RouteInfo[nNumNetElements][nNumNetElements];
		HashMap nodeToIndex=new HashMap();
		
		// Build a map from node to index
		// We will need this when traversing the graph
		// (the graph doesn't know about our collection of input
		// net elements)
		Iterator nodeIndexIterator = netElements.iterator();
		int nNodeIndex = 0;
		while (nodeIndexIterator.hasNext())
		{
			nodeToIndex.put(nodeIndexIterator.next(),new Integer(nNodeIndex));
			++nNodeIndex;
		}
		
		try
		{
			// Iterate through outer index
			for (int i=0;i<nNumNetElements;++i)
			{							
				int j=0;
				Iterator innerIterator=netElements.iterator();
				while (innerIterator.hasNext())
				{
					routeInfo[i][j]= new RouteInfo();
					routeInfo[i][j].m_thisElement = 
						(AbstractElementModel)innerIterator.next();
					++j;
				}				
				// Apply Moore's algorithm
				// We have a distance of zero to ourselves
				routeInfo[i][i].m_nDistanceToSource = 0;
				// Define the starting point
				LinkedList currentList =
					new LinkedList();
				currentList.add(routeInfo[i][i]);
				// Keep going until no more elements need to be processed
				while (currentList.size()>0)
				{
					// Create the follow-up list
					LinkedList newList = new LinkedList();
					for (Iterator listContent=currentList.iterator();listContent.hasNext();)
					{
						RouteInfo currentRouteInfo = (RouteInfo)listContent.next();
						// Look up all connections (to and from, only from if directed
						// and iterate them)
						Set connectedNodes = GetDirectlyConnectedNodes(currentRouteInfo.m_thisElement,
								connectionTypeOUTBOUND);
						if (ignoreArcDirection==true)
						{
							// Depending on our configuration we care about the
							// direction of the edge or not
							Set wrongDirection = GetDirectlyConnectedNodes(currentRouteInfo.m_thisElement,
									connectionTypeINBOUND);
							connectedNodes.addAll(wrongDirection);
							
						}
						Iterator nodeIterator = connectedNodes.iterator();
						while (nodeIterator.hasNext())
						{
							AbstractElementModel target = (AbstractElementModel)nodeIterator.next();
							// Use our node to index lookup table to
							// find the RouteInfo object corresponding to the
							// target
							Integer targetIndex = (Integer)nodeToIndex.get(target);
							if (targetIndex!=null)
							{
								RouteInfo newRouteInfo =
									routeInfo[i][targetIndex.intValue()];
								// See whether this node has already been visited
								if (newRouteInfo.m_nDistanceToSource==-1)
								{
									// Update the information on this node
									newRouteInfo.m_predecessor = currentRouteInfo;
									newRouteInfo.m_nDistanceToSource = currentRouteInfo.m_nDistanceToSource + 1;
									// Add it to the new node list
									newList.add(newRouteInfo);
								}
							}
						}
					}
					// Iterate through the new list and
					// see what's left
					currentList = newList;
				}
			}
		}
		catch(Exception e)
		{
			LoggerManager.info(Constants.WOFLAN_LOGGER, "Illegal object type found!");
			// Can't calculate if non petri-net element has been 
			// passed as input
			routeInfo = null;
		}
		return routeInfo;
	}
	//! Extract unconnected nodes from the given RouteInfo array and store
	//! it in the set
	//! An unconnected node is a node that has no connection to or from
	//! the specified node
	//! @param centralNode specifies the node all nodes need to be connected to
	//! @param connectionGraph specifies the RouteInfo array
	//! @param unconnectedNodes set that receives the unconnected nodes
	public static void GetUnconnectedNodes(
			AbstractElementModel centralNode,
			RouteInfo[][] connectionGraph, Set unconnectedNodes)
	{
		int nNumElements = connectionGraph.length;
		int nCentralNodeIndex = -1;
		for (int i=0;i<nNumElements;++i)
			if (connectionGraph[i][i].m_thisElement==centralNode)
				nCentralNodeIndex = i;
		if (nCentralNodeIndex!=-1)
		{
			// Add all elements that do not have a connection to or from the
			// specified "centralNode"
			for (int i=0;i<nNumElements;++i)
			{
				if (connectionGraph[nCentralNodeIndex][i].m_nDistanceToSource==-1)
					unconnectedNodes.add(connectionGraph[nCentralNodeIndex][i].m_thisElement);
				if (connectionGraph[i][nCentralNodeIndex].m_nDistanceToSource==-1)
					// If item i does not have a connection to nCentralNodeIndex,
					// item i (whose reference is always stored in [i][i]) must be added 
					// to the unconnected list
					unconnectedNodes.add(connectionGraph[i][i].m_thisElement);
			}
		}
	}				
	public static class ArcConfiguration
	{
		int m_numIncoming = 0;
		int m_numOutgoing = 0;
	}
	//! Get the configuration of the arcs connected to the
	//! specified element
	//! @param element specifies the element to be analysed
	//! @param config specifies the object that will receive the
	//!        number of incoming and outgoing arcs for the
	//!        specified element
	public static void GetArcConfiguration(AbstractElementModel element,			
			ArcConfiguration config)
	{
		config.m_numIncoming = GetDirectlyConnectedNodes(element,connectionTypeINBOUND).size();
		config.m_numOutgoing = GetDirectlyConnectedNodes(element,connectionTypeOUTBOUND).size();
	}
	//! These constants define mask entries specifying
	//! the type of connections to be retrieved in the
	//! method below
	public static int connectionTypeINBOUND = 1;
	public static int connectionTypeOUTBOUND = 2;
	public static int connectionTypeALL = Integer.MAX_VALUE;
	
	//! Return a set of elements preceding a given element
	//! All other methods of NetAlgorithms must use this method
	//! to navigate the graph!!
	//! 
	//! @param element specifies the element which is to be examined
	//! @param specifies the type of connection to be taken into account
	//! @return Returns a set of predecessors, successors or both
	public static Set GetDirectlyConnectedNodes(AbstractElementModel element,
			int connectionType)
	{
		HashSet result = new HashSet();
		Port port = element.getPort();
		for (Iterator edgeIterator=port.edges();edgeIterator.hasNext();)
		{
			Edge current = (Edge)edgeIterator.next();
			DefaultPort connectedPort = null;
			// If the current port is the target
			// the edge is incoming and we have a predecessor
			if ((current.getTarget()==port)&&((connectionType&connectionTypeINBOUND)>0))
				connectedPort = (DefaultPort)current.getSource();			
			// If the current port is the source
			// the edge is outgoing and we have a successor
			if ((current.getSource()==port)&&((connectionType&connectionTypeOUTBOUND)>0))
				connectedPort = (DefaultPort)current.getTarget();
			if (connectedPort!=null)
			{
				// Found a port, get it's parent
				Object connectedNode = connectedPort.getParent();
				result.add(connectedNode);
			}
		}				
		return result;
	}
}
