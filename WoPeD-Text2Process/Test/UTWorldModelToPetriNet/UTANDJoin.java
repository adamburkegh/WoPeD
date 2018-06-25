package UTWorldModelToPetriNet;

import WorldModelToPetrinet.ANDJoin;
import WorldModelToPetrinet.ANDSplit;
import WorldModelToPetrinet.PetriNet;
import WorldModelToPetrinet.Place;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class UTANDJoin {

    /*Unit test for Class WorldModelToPetrinet.ANDJoin*/

    private String exspectedPNML="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!--PLEASE DO NOT EDIT THIS FILE\n" +
            "Created with Workflow PetriNet Designer Version 3.2.0 (woped.org)-->\n" +
            "<pnml xmlns=\"pnml.woped.org\">\n" +
            "  <net type=\"http://www.informatik.hu-berlin.de/top/pntd/ptNetb\" id=\"noID\"><place id=\"p1\"><name><text>p1</text><graphics><offset x=\"0\" y=\"0\"/></graphics></name><graphics><position x=\"0\" y=\"0\"/><dimension x=\"40\" y=\"40\"/></graphics></place>\n" +
            "<place id=\"p2\"><name><text>p2</text><graphics><offset x=\"0\" y=\"0\"/></graphics></name><graphics><position x=\"0\" y=\"0\"/><dimension x=\"40\" y=\"40\"/></graphics></place>\n" +
            "<place id=\"p3\"><name><text>p3</text><graphics><offset x=\"0\" y=\"0\"/></graphics></name><graphics><position x=\"0\" y=\"0\"/><dimension x=\"40\" y=\"40\"/></graphics></place>\n" +
            "<place id=\"p4\"><name><text>p4</text><graphics><offset x=\"0\" y=\"0\"/></graphics></name><graphics><position x=\"0\" y=\"0\"/><dimension x=\"40\" y=\"40\"/></graphics></place>\n" +
            "<transition id=\"t1_op_1\"><name><text/><graphics><offset x=\"0\" y=\"0\"/></graphics></name><graphics><position x=\"0\" y=\"0\"/><dimension x=\"40\" y=\"40\"/></graphics><toolspecific tool=\"WoPeD\" version=\"1.0\"><operator id=\"t1\" type=\"102\"/><time>0</time><timeUnit>1</timeUnit><orientation>3</orientation></toolspecific></transition>\n" +
            "<arc id=\"a1\" source=\"t1_op_1\" target=\"p1\"><inscription><text>1</text><graphics><offset x=\"500.0\" y=\"-12.0\"/></graphics></inscription><toolspecific tool=\"WoPeD\" version=\"1.0\"><probability>1.0</probability><displayProbabilityOn>false</displayProbabilityOn><displayProbabilityPosition x=\"500.0\" y=\"12.0\"/></toolspecific></arc>\n" +
            "<arc id=\"a2\" source=\"p2\" target=\"t1_op_1\"><inscription><text>1</text><graphics><offset x=\"500.0\" y=\"-12.0\"/></graphics></inscription><toolspecific tool=\"WoPeD\" version=\"1.0\"><probability>1.0</probability><displayProbabilityOn>false</displayProbabilityOn><displayProbabilityPosition x=\"500.0\" y=\"12.0\"/></toolspecific></arc>\n" +
            "<arc id=\"a3\" source=\"p3\" target=\"t1_op_1\"><inscription><text>1</text><graphics><offset x=\"500.0\" y=\"-12.0\"/></graphics></inscription><toolspecific tool=\"WoPeD\" version=\"1.0\"><probability>1.0</probability><displayProbabilityOn>false</displayProbabilityOn><displayProbabilityPosition x=\"500.0\" y=\"12.0\"/></toolspecific></arc>\n" +
            "<arc id=\"a4\" source=\"p4\" target=\"t1_op_1\"><inscription><text>1</text><graphics><offset x=\"500.0\" y=\"-12.0\"/></graphics></inscription><toolspecific tool=\"WoPeD\" version=\"1.0\"><probability>1.0</probability><displayProbabilityOn>false</displayProbabilityOn><displayProbabilityPosition x=\"500.0\" y=\"12.0\"/></toolspecific></arc>\n" +
            "<toolspecific tool=\"WoPeD\" version=\"1.0\">\n" +
            "      <bounds>\n" +
            "        <position x=\"2\" y=\"25\"/>\n" +
            "        <dimension x=\"763\" y=\"574\"/>\n" +
            "      </bounds>\n" +
            "      <scale>100</scale>\n" +
            "      <treeWidthRight>549</treeWidthRight>\n" +
            "      <overviewPanelVisible>true</overviewPanelVisible>\n" +
            "      <treeHeightOverview>100</treeHeightOverview>\n" +
            "      <treePanelVisible>true</treePanelVisible>\n" +
            "      <verticalLayout>false</verticalLayout>\n" +
            "      <resources>\n" +
            "\n" +
            "\n" +
            "      </resources>\n" +
            "    </toolspecific>  </net>\n" +
            "</pnml>";

    @Test
    public void evaluateANDJoin() {
        PetriNet pn = new PetriNet();

        Place target =pn.getElementBuilder().createPlace(false,"");
        ArrayList<Place> sources = new ArrayList<Place>();
        sources.add(pn.getElementBuilder().createPlace(false,""));
        sources.add(pn.getElementBuilder().createPlace(false,""));
        sources.add(pn.getElementBuilder().createPlace(false,""));

        pn.add(target);
        pn.add(sources.get(0));
        pn.add(sources.get(1));
        pn.add(sources.get(2));

        ANDJoin aj = new ANDJoin("",false,"",pn.getElementBuilder());
        aj.addANDJoinToPetriNet(pn,sources,target);
        assertEquals("AND Join did not create exspected PNML.", true,pn.getPNML().equals(exspectedPNML));
    }
}
