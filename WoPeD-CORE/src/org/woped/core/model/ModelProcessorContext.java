package org.woped.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public class ModelProcessorContext implements Serializable
{
    private HashMap contextMap = null;

    public ModelProcessorContext()
    {
        contextMap = new HashMap();
    }

    public void addContext(Object key, Object value)
    {
        contextMap.put(key, value);
    }

    public void removeContext(Object key)
    {
        contextMap.remove(key);
    }

    public Object getContext(Object key)
    {
        return contextMap.get(key);
    }

    public boolean containsContext(Object key)
    {
        return contextMap.containsKey(key);
    }

    public Iterator getKeyIterator()
    {
        return contextMap.keySet().iterator();
    }
}
