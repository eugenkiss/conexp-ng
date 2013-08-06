package fcatools.conexpng.gui.lattice;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import fcatools.conexpng.Conf;

/*The JSVGCanvas provides a set of build-in interactors that
 * let the users manipulate the displayed document, including ones for zooming,
 * panning and rotating. Interactors catch user input to the JSVGCanvas component
 * and translate them into behaviour.
 */
public class LatticeGraphView extends JSVGCanvas {

    private static final long serialVersionUID = -8623872314193862285L;
    private LatticeGraph graph;
    private Conf state;
    private ArrayList<Node> lastIdeal;
    private boolean idealHighlighting;
    private boolean move;
    private static Font font = new Font("Monospaced", Font.PLAIN, 12);

    public LatticeGraphView(LatticeGraph graph, Conf state) {
        this.graph = graph;
        this.state = state;
        this.lastIdeal = new ArrayList<Node>();
        this.init();
    }

    private void init() {
        this.removeAll();
        for (Node n : graph.getNodes()) {
            this.add(n);
            n.addMouseMotionListener(new LatticeGraphNodeMouseMotionListener(n));
            n.addMouseListener(new NodeMouseClickListener(n));

            this.add(n.getAttributesLabel());
            n.getAttributesLabel().addMouseMotionListener(
                    new LatticeGraphNodeMouseMotionListener(n.getAttributesLabel()));

            this.add(n.getObjectsLabel());
            n.getObjectsLabel().addMouseMotionListener(new LatticeGraphNodeMouseMotionListener(n.getObjectsLabel()));
        }

    }

    @Override
    public void paint(Graphics g0) {
        super.paint(g0);

        Graphics2D g = (Graphics2D) g0;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        int radius = LatticeView.radius;

        if (!graph.getEdges().isEmpty() && state.showEdges) {
            for (Edge e : graph.getEdges()) {
                g.setColor(Color.BLACK);
                if (e.getU().isPartOfAnIdeal() && e.getV().isPartOfAnIdeal() && idealHighlighting) {
                    g.setColor(Color.BLUE);
                } else if (!(e.getU().isPartOfAnIdeal() && e.getV().isPartOfAnIdeal()) && idealHighlighting) {
                    g.setColor(Color.LIGHT_GRAY);
                }
                g.drawLine(e.getU().getX() + radius, e.getU().getY() + radius, e.getV().getX() + radius, e.getV()
                        .getY() + radius);
            }
        }
        for (Node n : graph.getNodes()) {
            int x = n.getX();
            int y = n.getY();

            // draw a normal node
            g.setColor(Color.WHITE);
            g.fillOval(x, y, radius * 2, radius * 2);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, radius * 2, radius * 2);

            // highlight an ideal if it selected
            if (n.isPartOfAnIdeal() && idealHighlighting) {
                lastIdeal.add(n);
                g.setColor(Color.BLUE);
                g.drawOval(x, y, radius * 2, radius * 2);
            } else if ((!n.isPartOfAnIdeal()) && idealHighlighting) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillOval(x, y, radius * 2, radius * 2);
            }

            // label drawing
            if ((!n.getVisibleObjects().isEmpty()) && state.showObjectLabel) {
                g.setColor(Color.BLACK);
                g.drawLine(n.getObjectsLabel().getX() + n.getObjectsLabel().getBounds().width / 2, n.getObjectsLabel()
                        .getY(), x + radius, y + radius);

                String content = n.getObjectsLabel().elementsToString();
                g.setFont(font);
                FontMetrics fm = g.getFontMetrics();
                Rectangle r = fm.getStringBounds(content, g).getBounds();

                g.setColor(Color.WHITE);
                g.fillRect(r.x + n.getObjectsLabel().getX(), r.y + n.getObjectsLabel().getY(), r.width, r.height);

                g.setColor(Color.MAGENTA);

                g.drawString(content, n.getObjectsLabel().getX(), n.getObjectsLabel().getY());

                g.setColor(Color.BLACK);
                g.drawRect(r.x + n.getObjectsLabel().getX(), r.y + n.getObjectsLabel().getY(), r.width, r.height);

                n.getObjectsLabel().setBounds(r.x + n.getObjectsLabel().getX(), r.y + n.getObjectsLabel().getY(),
                        r.width, r.height);

                // draw filled node
                g.setColor(Color.BLACK);
                g.fillOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
                g.setColor(Color.WHITE);
                g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 0, 180);
                g.setColor(Color.BLACK);
                g.drawOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
            }

            if ((!n.getVisibleAttributes().isEmpty()) && state.showAttributLabel) {
                g.setColor(Color.BLACK);
                g.drawLine(n.getAttributesLabel().getX() + n.getAttributesLabel().getBounds().width / 2, n
                        .getAttributesLabel().getY(), x + radius, y + radius);

                String content = n.getAttributesLabel().elementsToString();
                g.setFont(font);
                FontMetrics fm = g.getFontMetrics();
                Rectangle r = fm.getStringBounds(content, g).getBounds();

                g.setColor(Color.WHITE);
                g.fillRect(r.x + n.getAttributesLabel().getX(), r.y + n.getAttributesLabel().getY(), r.width, r.height);

                g.setColor(Color.GREEN);

                g.drawString(content, n.getAttributesLabel().getX(), n.getAttributesLabel().getY());

                g.setColor(Color.BLACK);
                g.drawRect(r.x + n.getAttributesLabel().getX(), r.y + n.getAttributesLabel().getY(), r.width, r.height);

                n.getAttributesLabel().setBounds(r.x + n.getAttributesLabel().getX(),
                        r.y + n.getAttributesLabel().getY(), r.width, r.height);

                // draw filled node
                g.setColor(Color.BLUE);
                g.fillOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
                g.setColor(Color.WHITE);
                g.fillArc(x, y, LatticeView.radius * 2, LatticeView.radius * 2, 180, 180);
                g.setColor(Color.BLACK);
                g.drawOval(x, y, LatticeView.radius * 2, LatticeView.radius * 2);
            }
        }
        resetHighlighting();
        lastIdeal.clear();
    }

    /**
     * This method creates an pdf file of the lattice.
     *
     * @param path
     */
    public void exportLatticeAsPDF(String path) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document document = domImpl.createDocument(svgNS, "svg", null);

        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        this.paint(svgGenerator);

        Writer out;
        try {
            out = new FileWriter(new File("test_batik.svg"));
            svgGenerator.stream(out, true);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }

        String svg_URI_input;
        try {
            svg_URI_input = Paths.get("test_batik.svg").toUri().toURL().toString();
            TranscoderInput input_svg_image = new TranscoderInput(svg_URI_input);
            OutputStream pdf_ostream;
            if (path.contains(".pdf")) {
                pdf_ostream = new FileOutputStream(path);
            } else {
                pdf_ostream = new FileOutputStream(path + ".pdf");
            }

            // calculating the pdf file size
            int maxWidth = 0;
            int maxHeight = 0;
            for (Node n : graph.getNodes()) {
                if (maxWidth < n.getX()) {
                    maxWidth = n.getX();
                }
                if (maxHeight < n.getY()) {
                    maxHeight = n.getY();
                }
                if (state.showAttributLabel) {
                    if (maxWidth < n.getAttributesLabel().getX()) {
                        maxWidth = n.getAttributesLabel().getX();
                    }
                    if (maxHeight < n.getAttributesLabel().getY()) {
                        maxHeight = n.getAttributesLabel().getY();
                    }
                }
                if (state.showObjectLabel) {
                    if (maxWidth < n.getObjectsLabel().getX()) {
                        maxWidth = n.getObjectsLabel().getX();
                    }
                    if (maxHeight < n.getObjectsLabel().getY()) {
                        maxHeight = n.getObjectsLabel().getY();
                    }
                }

            }
            Rectangle r = new Rectangle(maxWidth + 30, maxHeight + 30);
            TranscoderOutput output_pdf_file = new TranscoderOutput(pdf_ostream);
            Transcoder transcoder = new PDFTranscoder();
            transcoder.addTranscodingHint(PDFTranscoder.KEY_WIDTH, new Float(r.width));
            transcoder.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, new Float(r.height));
            transcoder.addTranscodingHint(PDFTranscoder.KEY_AOI, r);

            transcoder.transcode(input_svg_image, output_pdf_file);
            pdf_ostream.flush();
            pdf_ostream.close();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TranscoderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setLatticeGraph(LatticeGraph g) {
        graph = g;
        init();
        setMove(move);
    }

    private void resetHighlighting() {
        for (Node n : lastIdeal) {
            n.setPartOfAnIdeal(false);
        }
    }

    public void setMove(boolean change) {
        move = change;
        for (Node n : graph.getNodes()) {
            n.moveSubgraph(change);
        }
    }

    public void idealHighlighting(boolean change) {
        this.idealHighlighting = change;
    }

}
