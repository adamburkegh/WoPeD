package org.woped.woflan;

import org.woped.core.controller.*;
import org.woped.core.model.*;
import org.woped.core.model.petrinet.*;
import org.woped.core.utilities.LoggerManager;
import org.jgraph.graph.*;


import java.util.*;

public class StructuralAnalysis {

	//! Construct static analysis object from
	//! a petri-net editor
	//! Note that this object will not perform all
	//! its calculations at construction time
	//! Rather, each getter method knows which calculations
	//! need to be done and will trigger them
	public StructuralAnalysis(IEditor currentEditor) {
		m_currentEditor = currentEditor;
	}
	
	public int GetNumPlaces()
	{
		Calculate_BasicNetInfo();
		return m_places.size();
	}
	//! Returns an iterator over all places of the net
	public Iterator GetPlacesIterator()
	{
		Calculate_BasicNetInfo();
		return m_places.iterator();
	}
	
	public int GetNumTransitions()
	{
		Calculate_BasicNetInfo();
		return m_transitions.size();
	}
	public Iterator GetTransitionsIterator()
	{
		Calculate_BasicNetInfo();
		return m_transitions.iterator();
	}
	public int GetNumArcs()
	{
		return m_nNumArcs;		
	}

	public int GetNumSourcePlaces()
	{
		Calculate_BasicNetInfo();
		return m_sourcePlaces.size();
	}
	public Iterator GetSourcePlacesIterator()
	{
		Calculate_BasicNetInfo();
		return m_sourcePlaces.iterator();		
	}
	public int GetNumSourceTransitions()
	{
		Calculate_BasicNetInfo();
		return m_sourceTransitions.size();
	}
	public Iterator GetSourceTransitionsIterator()
	{
		Calculate_BasicNetInfo();
		return m_sourceTransitions.iterator();		
	}

	public int GetNumSinkPlaces()
	{
		Calculate_BasicNetInfo();
		return m_sinkPlaces.size();
	}
	public Iterator GetSinkPlacesIterator()
	{
		Calculate_BasicNetInfo();
		return m_sinkPlaces.iterator();		
	}
	public int GetNumSinkTransitions()
	{
		Calculate_BasicNetInfo();
		return m_sinkTransitions.size();
	}
	public Iterator GetSinkTransitionsIterator()
	{
		Calculate_BasicNetInfo();
		return m_sinkTransitions.iterator();		
	}

	public int GetNumNotConnectedNodes()
	{
		Calculate_Connections();
		return m_notConnectedNodes.size();
	}
	//! Return all nodes of the current net that
	//! are not connected	
	public Iterator GetNotConnectedNodes()
	{
		Calculate_Connections();
		return m_notConnectedNodes.iterator();		
	}
	public int GetNumNotStronglyConnectedNodes()
	{
		Calculate_Connections();
		return m_notStronglyConnectedNodes.size();		
	}
	//! Return all nodes of the current net that 
	//! are not strongly connected
	public Iterator GetNotStronglyConnectedNodes()
	{
		Calculate_Connections();
		return m_notStronglyConnectedNodes.iterator();		
	}
	
	public int GetNumFreeChoiceViolations()
	{
		Calculate_FreeChoice();
		return m_freeChoiceViolations.size();		
	}
	//! Return a list of free-choice violations
	//! Each free-choice violation is represented 
	//! by a Set of nodes defining the violation
	//! @return Iterator through a list of sets 
	//!         of nodes violating the free-choice property
	public Iterator GetFreeChoiceViolations()
	{
		Calculate_FreeChoice();
		return m_freeChoiceViolations.iterator();		
	}
	
	//! Remember a reference to the current editor
	//! as we need it to access the net
	private IEditor m_currentEditor;
	
	//! Will become true once
	//! the basic net info has been calculated
	boolean m_bBasicNetInfoAvailable = false;
	
	//! Stores a linked list of all the places of
	//! the processed net
	LinkedList m_places = new LinkedList();
	//! Stores a linked list of all the transitions of
	//! the processed net
	LinkedList m_transitions = new LinkedList();
	//! Stores the number of arcs contained in this net
	int m_nNumArcs=0;
	
	//! Stores a linked list of source places
	LinkedList m_sourcePlaces = new LinkedList();
	//! Stores a linked list of sink places
	LinkedList m_sinkPlaces = new LinkedList();
	
	//! Stores a linked list of source transitions
	LinkedList m_sourceTransitions = new LinkedList();
	//! Stores a linked list of sink transitions
	LinkedList m_sinkTransitions = new LinkedList();
	
	boolean m_bConnectionInfoAvailable = false;
	
	//! Stores a list of all nodes (transitions 
	//! and places that are not connected)
	HashSet m_notConnectedNodes = new HashSet();
	//! Stores a list of all nodes (transitions 
	//! and places that are not strongly connected)
	HashSet m_notStronglyConnectedNodes = new HashSet();
	
	boolean m_bFreeChoiceInfoAvailable = false;
	//! Stores a list of free-choice violations
	//! consisting of node sets
	HashSet m_freeChoiceViolations = new HashSet();
	
	//! Trigger the calculation of basic net information
	private void Calculate_BasicNetInfo()
	{
		// We cache all calculated information
		// Check if we already know what we need to know
		if (m_bBasicNetInfoAvailable)
			return;
		m_bBasicNetInfoAvailable = true;
		
		// Get the element container containing all our elements
		ModelElementContainer elements
		= m_currentEditor.getModelProcessor().getElementContainer();
		// Iterate through all elements and 
		// take notes
		Iterator i=elements.getRootElements().iterator();
		NetAlgorithms.ArcConfiguration arcConfig = new NetAlgorithms.ArcConfiguration();
		while (i.hasNext())
		{
			try
			{
				AbstractElementModel currentNode =				
					(AbstractElementModel)i.next();
				NetAlgorithms.GetArcConfiguration(currentNode, arcConfig);
				switch (currentNode.getType())
				{
				case AbstractPetriNetModelElement.PLACE_TYPE:
					m_places.add(currentNode);
					if (arcConfig.m_numIncoming == 0)
						m_sourcePlaces.add(currentNode);
					if (arcConfig.m_numOutgoing==0)
						m_sinkPlaces.add(currentNode);
					break;
				case AbstractPetriNetModelElement.TRANS_SIMPLE_TYPE:					
					// Treat simple and complex (operator) transitions
					// equally
				case AbstractPetriNetModelElement.TRANS_OPERATOR_TYPE:
					m_transitions.add(currentNode);
					if (arcConfig.m_numIncoming == 0)
						m_sourceTransitions.add(currentNode);
					if (arcConfig.m_numOutgoing==0)
						m_sinkTransitions.add(currentNode);						
					break;												
				default:
					// Ignore all the rest
				}
				
			}
			catch(Exception e)
			{
				LoggerManager.info(Constants.WOFLAN_LOGGER, "Illegal object type found!");					
			}
		}
		// Just ask the arc map for its size...
		m_nNumArcs = elements.getArcMap().size();
	}
	
	private void Calculate_Connections()
	{
		if (m_bConnectionInfoAvailable==true)
			return;
		m_bConnectionInfoAvailable=true;
		
		// First, calculate basic net information
		Calculate_BasicNetInfo();
		LinkedList netElements = new LinkedList();
		// A WoPeD graph contains more than just places
		// and transitions. We are only interested in those
		// however
		netElements.addAll(m_places);
		netElements.addAll(m_transitions);
		
		if (m_transitions.size()==0)
			return;

		// Create transition 't*'
        CreationMap tempMap = ((AbstractElementModel)m_transitions.getFirst()).getCreationMap();
        tempMap.setType(AbstractPetriNetModelElement.TRANS_SIMPLE_TYPE);
        String tempID = "t*";
        tempMap.setName(tempID);
        tempMap.setId(tempID);
        tempMap.setEditOnCreation(false);
        AbstractElementModel ttemp = m_currentEditor.getModelProcessor().createElement(tempMap);
        netElements.add(ttemp);
        
        
        // Now connect the new transition 't*' to
        // the source and the target
        // For this to be possible, we will need
        // a unique source and a unique sink
        if ((m_sourcePlaces.size()==1)&&(m_sinkPlaces.size()==1))        	
        {                
        	AbstractElementModel source = (AbstractElementModel)m_sourcePlaces.getFirst(); 
        	String sourceID =source.getId();
        	AbstractElementModel target = (AbstractElementModel)m_sinkPlaces.getFirst(); 
        	String targetID = target.getId();        		
        	Object newEdge = m_currentEditor.getModelProcessor().createArc(tempID,sourceID);   
        	ttemp.getPort().addEdge(newEdge);
        	source.getPort().addEdge(newEdge);        	
        	newEdge = m_currentEditor.getModelProcessor().createArc(targetID,tempID);
        	ttemp.getPort().addEdge(newEdge);
        	target.getPort().addEdge(newEdge);
        }        
        		
		// First check for connectedness:
		// Return connection map presuming that all arcs may be
		// used in both directions
		NetAlgorithms.RouteInfo[][] connectionGraph = NetAlgorithms.GetAllConnections(netElements, true);
		if (connectionGraph!=null)
			NetAlgorithms.GetUnconnectedNodes(ttemp, connectionGraph, m_notConnectedNodes);	
		
		// Now get the graph for strong connectedness
		// This will also give us all shortest distances
		// according to Moore's algorithm (no arc weights) 
		NetAlgorithms.RouteInfo[][] strongConnectionGraph = NetAlgorithms.GetAllConnections(netElements, false);
		if (strongConnectionGraph!=null)
			NetAlgorithms.GetUnconnectedNodes(ttemp, strongConnectionGraph, m_notStronglyConnectedNodes);
		
		// Remove the element from the graph
		m_currentEditor.getModelProcessor().removeElement(tempID);		
	}
	
	void Calculate_FreeChoice()
	{
		if (m_bFreeChoiceInfoAvailable)
			return;
		m_bFreeChoiceInfoAvailable = true;
		
		// First, calculate basic net information
		Calculate_BasicNetInfo();

		// The first thing we look for are forward-branched places (conflicts)
		// and their follow-up transitions
		Set placeResults = GetNonFreeChoiceGroups(m_places.iterator(),false);
		// Now look for backward-branched transitions (synchronization)
		// and their preceeding places
		Set transitionResults = GetNonFreeChoiceGroups(m_transitions.iterator(),true);
		
		m_freeChoiceViolations.addAll(placeResults);
		m_freeChoiceViolations.addAll(transitionResults);
	}
	Set GetNonFreeChoiceGroups(Iterator i, boolean swapArcDirection)
	{
		Set result = new HashSet();
		// Look for forward-branched places (conflicts)
		// and their follow-up transitions
		while (i.hasNext()){
			// Determine the arc configuration of the current place
			AbstractElementModel currentPlace = (AbstractElementModel)i.next();
			
			// Have a closer look at the follow-up transitions			
			// Collect all affected nodes a priori just in case
			HashSet violationGroup = new HashSet();
			boolean violation = false;
			Set compareSet = null;
			Set successors = NetAlgorithms.GetDirectlyConnectedNodes(currentPlace,
					swapArcDirection?NetAlgorithms.connectionTypeINBOUND
							:NetAlgorithms.connectionTypeOUTBOUND);
			for (Iterator s=successors.iterator();s.hasNext();)
			{
				AbstractElementModel successor = (AbstractElementModel)s.next();
				Set predecessors = NetAlgorithms.GetDirectlyConnectedNodes(successor,
						swapArcDirection?NetAlgorithms.connectionTypeOUTBOUND
								:NetAlgorithms.connectionTypeINBOUND);
				if (compareSet==null)
					compareSet = predecessors;
				else
				{
					// All predecessors of all successors of our
					// original place must be the same
					violation = violation || (!compareSet.equals(predecessors));
				}
				// Add the element and all its predecessors
				violationGroup.addAll(predecessors);							
				violationGroup.add(successor);
			}
			if (violation)
			{
				// We have a violation, store the group in the list
				result.add(violationGroup);	
			}
		}
		return result;
	}	
}
