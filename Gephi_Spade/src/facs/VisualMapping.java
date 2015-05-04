package facs;

import java.awt.Color;
import gephi.spade.panel.SpadeContext.NormalizationKind;
import gephi.spade.panel.SpadeContext.SymmetryType;
import gephi.spade.panel.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JOptionPane;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;


/**
 *
 * @author mlinderm
 */
public class VisualMapping {

    private static Set<String> nodeList;
    private HashMap globalRanges;
    //Markers used for size and color "mapping"
    private SpadeContext spadeCxt;
    private String sizeMarker;
    private String colorMarker;
    private NormalizationKind rangeKind;
    private SymmetryType symmetryType;
    
    public VisualMapping(SpadeContext context) {
        globalRanges = null;
        this.nodeList = new HashSet();
        this.spadeCxt = context;
        
    }

    public VisualMapping(File globalBoundaryFile, SpadeContext context) {
        globalRanges = new HashMap();
        this.nodeList = new HashSet();
        this.spadeCxt = context;
        readBoundaries(globalBoundaryFile);
    }

    private static class Range {
        private double min;
        private double max;

        public Range(double min_a, double max_a) {
            min = min_a;
            max = max_a;
        }

        public void setMax(double Max) {
            max = Max;
        }

        public void setMin(double Min) {
            min = Min;
        }

        public double getMax() {
            if (max < min) {
                return 100.0; //sane default if no valid nodes
            } else {
                return max;
            }
        }

        public double getMin() {
            if (max < min) {
                return 0.0; //sane default if no valid nodes
            } else {
                return min;
            }
        }

    }
    
    public boolean globalRangeAvailable() { 
        return (globalRanges != null) && (globalRanges.size() > 0);
    }

    /**
     * Set markers used in VisualMapping
     * @param sizeMarker Marker to use for size calculator
     * @param colorMarker Marker to use for color calculator
     * @param rangeKind global or local
     * @param symmetryType Asymmetric or symmetric
     * @throws IllegalArgumentException If markers are non-numeric
     */
    public void setCurrentMarkersAndRangeKind(String sizeMarker, String colorMarker, NormalizationKind rangeKind, SymmetryType symmetryType) throws IllegalArgumentException {
        //if (!isNumericAttribute(sizeMarker)) {
        if (sizeMarker == null) {
          throw new IllegalArgumentException("sizeMarker is non-numeric");
        }
        this.sizeMarker = sizeMarker;

        //if (!isNumericAttribute(colorMarker)) {
         if (colorMarker == null) {
            throw new IllegalArgumentException("colorMarker is non-numeric");
        }
        this.colorMarker = colorMarker;

        this.rangeKind = rangeKind;

        this.symmetryType = symmetryType;
    }
    
    public String getCurrentSizeMarker() { return sizeMarker; }
    public String getCurrentColorMarker() { return colorMarker; }

    /**
     * Create a size calculator based on current sizeMarker
     * @return "SPADE Size Calculator" with size continuous mapper
     */
    /* currently out
    public Calculator createSizeCalculator() {
    
        Range rng = getAttributeRange(sizeMarker);
        double rmin = rng.getMin();
        double rmax = rng.getMax();

        VisualPropertyType type = VisualPropertyType.NODE_SIZE;
        final Object defaultObj = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

        ContinuousMapping cm = new ContinuousMapping(defaultObj.getClass(), sizeMarker);
        Interpolator numToSize = new LinearNumberToNumberInterpolator();
        cm.setInterpolator(numToSize);

        BoundaryRangeValues bv0 = new BoundaryRangeValues(28, 28, 28);
        BoundaryRangeValues bv1 = new BoundaryRangeValues(72, 72, 72);

        cm.addPoint(rmin, bv0);
        cm.addPoint(rmax, bv1);

        return new BasicCalculator("SPADE Size Calculator", cm, VisualPropertyType.NODE_SIZE);
    }

    /**
     * Create a color calculator based on current colorMarker
     * @return "SPADE Color Calculator" with color continuous mapper
     */
    

    /**
     * Checks if attribute is numeric, i.e. integer or floating point
     * @param attrID Attribute to be checked
     * @return true if numeric
     */
    // attrubutes not used the same way
    /*
    public static boolean isNumericAttribute(String attrID) {
        //CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        
        switch(cyNodeAttrs.getType(attrID)) {
            default: return false;
            case CyAttributes.TYPE_FLOATING:
            case CyAttributes.TYPE_INTEGER:
                return true;
      
        }
    }
    */
    /**
     * Populates a JComboBox will all numeric attributes of current network
     * @param csBox - JComboBox to populate
     */
    
    @SuppressWarnings("empty-statement")
    public static void populateNumericAttributeComboBox(javax.swing.JComboBox csBox) {
        csBox.removeAllItems();
        String name = "";
        //String[] names = null;
        String[] names = new String[100];
        int i = 0;
        //CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        try{ 
            
          //String str = network.toString();
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            pc.newProject();
            org.gephi.project.api.Workspace workspace = pc.getCurrentWorkspace();
            //nodeList = CyTableUtil.getColumnNames(cam.getCurrentTable());
            AttributeColumnsController acc = Lookup.getDefault().lookup(AttributeColumnsController.class);
            AttributeController atCon = Lookup.getDefault().lookup(AttributeController.class);
            AttributeModel model = atCon.getModel(workspace);
            AttributeTable at = model.getNodeTable();
            AttributeColumn[] columns = at.getColumns();
        
            for (AttributeColumn col : columns){
                nodeList.add(col.getTitle());
                names[i] = col.getTitle();
            }
            //CyTable nodeTable = spadeCxt.adapter.getCyTableManager().getTable(0);
            //CyTable nodeTable = network.getDefaultNodeTable().get; // BF check that these are the right values
            //network.getNodeList()
            //CyTable nodeTable = cam.getCurrentTable();
            //nodeList = CyTableUtil.getColumnNames(nodeTable);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getClass().getName());
        }
         
        //nodeList = CyTableUtil.getColumnNames(spadeCxt.adapter.getCyTableManager().getTable(0));
        /*
        for (CyColumn column : network.getDefaultNodeTable().getColumns()){
            if (column.getName() != null){
                names[i] = column.getName();
                i++;
            } 
        }
        */
        /*
        for (String colName : nodeList){
            if (colName != null) {
                names[i] = colName;
                i++;
            }
        }
        */
        //String[] names = spadeCxt.adapter.getCyApplicationManager().getCurrentNetwork().getRow(node).get("name",String.class);
        //String[] names = cyNodeAttrs.getAttributeNames();
        //Arrays.sort(names);
        for (String nameIterator : names) {
            //if (isNumericAttribute(name) && cyNodeAttrs.getUserVisible(name)) {
                csBox.addItem(nameIterator);
            //}
        }
         
    }
    
    private Range getAttributeRange(String attrID) {
        Range range;
        if ((rangeKind == NormalizationKind.GLOBAL) && ((range = (Range)globalRanges.get(attrID)) != null)) {
            return range;
        }

        // Either local, or could not find attribute in global list
        //double[] values = new double[.getNodeCount()];
        //double[] values = new double[cam.getCurrentNetwork().getNodeCount()];
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        org.gephi.project.api.Workspace workspace = pc.getCurrentWorkspace();
        
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel gm = gc.getModel(workspace);
        Graph graph = gm.getGraph();
        
        double[] values = new double[graph.getNodeCount()];
        int value_idx = 0;

        //CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
       // byte attrType = cyNodeAttrs.getType(attrID);
       //byte attrType = cam.getCurrentTable().getAllRows().get(attrID);
        //String attrType = cam.getCurrentNetwork().getDefaultNetworkTable().attrID);
        //Iterator<CyNode> it = Cytoscape.getCurrentNetwork().nodesIterator();
        //Iterator<CyNode> it = cam.getCurrentNetwork().getNodeList().iterator();
        //Iterator<Node> it = cam.getCurrentNetwork().getNodeList().iterator();
        NodeIterable it = graph.getNodes();
        Node[] graphNodes = it.toArray();
        //while (it.hasNext()) {
        for (int i = 0; i < graphNodes.length; i++){
            //giny.model.Node node = (giny.model.Node) it.next();
            //CyNode node = it.next();
            // Ignore non-numeric nodes
            //String nodeID = cam.getCurrentNetwork().getRow(node).get("node_id", String.class);
            String nodeID = Integer.toString(graphNodes[i].getId());
            //if (cyNodeAttrs.hasAttribute(nodeID, attrID)) {
            /*
            if (cam.getCurrentTable().rowExists(nodeID) && cam.getCurrentTable().rowExists(nodeID)) {
                Double value;
                if (attrType == CyAttributes.TYPE_INTEGER) {
                    value = cyNodeAttrs.getIntegerAttribute(nodeID, attrID).doubleValue();
                } else if (attrType == CyAttributes.TYPE_FLOATING) {
                    value = cyNodeAttrs.getDoubleAttribute(nodeID, attrID);
                } else {
                    continue;
                }
                    */
                values[value_idx] = Integer.parseInt(nodeID);
                value_idx++;
            }
        //}

        values = Arrays.copyOf(values, value_idx);
        Percentile pctile = new Percentile();
        
        return new Range(
            pctile.evaluate(values, 2.0),  // TODO: Make these values controllable
            pctile.evaluate(values, 98.0)
            );
    }

     private void readBoundaries(File boundaryFile) {
        try {

            BufferedReader br = new BufferedReader(new FileReader(boundaryFile.getAbsolutePath()));
            String   read;

            while ((read = br.readLine()) != null) {
                // Line: attribute 0% min% max% 100%
                String[] vals = read.split(" ");
                globalRanges.put(vals[0].replaceAll("\"",""), new Range(Double.parseDouble(vals[2]),Double.parseDouble(vals[3])));
            }

        } catch (FileNotFoundException ex) {
            //CyLogger.getLogger(CytoSpade.class.getName()).error(null, ex);
            globalRanges = null;
        } catch (IOException ex) {
            //CyLogger.getLogger(CytoSpade.class.getName()).error(null, ex);
            globalRanges = null;
        }
        //CyLogger.getLogger(CytoSpade.class.getName()).info("Loaded ranges from global_boundaries.table");
    }

}