package zasz.me.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TermsResponse
{
    public ResponseHeader responseHeader;
    public Map<String, List<String>> terms;

    public Map<String, Integer> toWeightedMap()
    {
        List<String> termsWithfrequency = terms.values().iterator().next();
        HashMap<String,Integer> weightedTerms = new HashMap<String, Integer>(termsWithfrequency.size());
        Iterator<String> i = termsWithfrequency.iterator();
        // This wont throw up because the list size will always be even.
        while (i.hasNext())
            weightedTerms.put(i.next(), Integer.parseInt(i.next()));
        return weightedTerms;
    }
}
