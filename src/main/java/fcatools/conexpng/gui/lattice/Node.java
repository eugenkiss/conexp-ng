package fcatools.conexpng.gui.lattice;

import javax.swing.*;

import de.tudresden.inf.tcs.fcalib.utils.ListSet;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Node extends JPanel implements LatticeGraphElement {

    /**
     *
     */
    private static final long serialVersionUID = 4253192979583459657L;
    private Set<String> objects;
    private Set<String> attributes;
    private int x;
    private int y;
    private List<Node> below;
    private ListSet<Node> ideal;
    private boolean isIdealVisibile;
    private Label visibleObjects;
    private Label visibleAttributes;
    private boolean moveSubgraph;
    private int level;
    private boolean hitBorder;

    /**
     *
     * @param extent
     * @param intent
     * @param x
     * @param y
     */
    public Node(Set<String> extent, Set<String> intent, int x, int y) {
        this.objects = extent;
        this.attributes = intent;
        this.x = x;
        this.y = y;
        this.setBounds(x, y, 15, 15);
        this.below = new ArrayList<>();

    }

    /**
     *
     */
    public Node() {
        this.objects = new TreeSet<>();
        this.attributes = new TreeSet<>();
        this.visibleObjects = new Label(new TreeSet<String>(), this, true);
        this.visibleAttributes = new Label(new TreeSet<String>(), this, false);
        this.ideal = new ListSet<>();
        this.x = 0;
        this.y = 0;
        this.setBounds(x, y, 15, 15);
        this.setBackground(Color.white);
        this.below = new ArrayList<>();
    }

    @Override
    public void paint(Graphics g) {
    }

    /**
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    public void setX(int x) {
        this.x = x;
        this.setBounds(x, y, 15, 15);
    }

    /**
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    public void setY(int y) {
        this.y = y;
        this.setBounds(x, y, 15, 15);
    }

    /**
     *
     * @param extent
     */
    public void addObject(String extent) {
        objects.add(extent);
    }

    /**
     *
     * @param set
     */
    public void addAttribut(String set) {
        attributes.add(set);
    }

    public void addBelowNode(Node n) {
        below.add(n);
    }

    public List<Node> getBelow() {
        return below;
    }

    public void update(int x, int y, boolean first) {
        int updateX;
        int updateY;
        if (this.x + x >= 2) {
            updateX = this.x + x;
        } else {
            hitBorder = true;
            updateX = 1;
        }
        if (this.y + y >= 2)
            updateY = this.y + y;
        else {
            updateY = 1;
            hitBorder = true;
        }

        for (Node n : ideal) {
            if (!n.isUpdateXPosible(x) || !n.isUpdateYPosible(y)) {
                hitBorder = true;
            }
        }

        if (!hitBorder) {
            if (moveSubgraph && first) {
                for (Node n : ideal) {
                    n.update(x, y, false);
                }
            }

            this.setBounds(updateX, updateY, 15, 15);
            this.x = updateX;
            this.y = updateY;
            visibleAttributes.update(x, y, first);
            visibleObjects.update(x, y, first);

            if (getParent() != null) {
                getParent().repaint();
            }
        }

        hitBorder = false;
    }

    public boolean isUpdateXPosible(int x) {
        if (this.x + x >= 2 && this.x + x < getParent().getWidth()) {
            return true;
        }
        return false;
    }

    public boolean isUpdateYPosible(int y) {
        if (this.y + y >= 2 && this.y + y < getParent().getHeight()) {
            return true;
        }
        return false;
    }

    public ListSet<Node> getIdeal() {
        return ideal;
    }

    public void addObjects(Set<String> extent) {
        objects.addAll(extent);

    }

    public void addAttributs(Set<String> intent) {
        attributes.addAll(intent);

    }

    public Set<String> getObjects() {
        return objects;
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public void setLevel(int level) {
        this.level = level;

    }

    public int getLevel() {
        return level;
    }

    public void setVisibleObject(String object) {
        visibleObjects.getSet().add(object);
    }

    public void setVisibleObjects(Set<String> objects) {
        visibleObjects.getSet().clear();
        visibleObjects.getSet().addAll(objects);
    }

    public Set<String> getVisibleObjects() {
        return this.visibleObjects.getSet();
    }

    public void setVisibleAttribute(String attribute) {
        this.visibleAttributes.getSet().add(attribute);
    }

    public void setVisibleAttributes(Set<String> attributes) {
        visibleAttributes.getSet().clear();
        visibleAttributes.getSet().addAll(attributes);
    }

    public Set<String> getVisibleAttributes() {
        return this.visibleAttributes.getSet();

    }

    public Label getObjectsLabel() {
        return this.visibleObjects;
    }

    public Label getAttributesLabel() {
        return this.visibleAttributes;
    }

    public void moveSubgraph(boolean b) {
        this.moveSubgraph = b;
    }

    public void toggleIdealVisibility() {
        this.isIdealVisibile = !this.isIdealVisibile;
        for (Node n : ideal) {
            n.setPartOfAnIdeal(isIdealVisibile);
        }
        if (getParent() != null) {
            getParent().repaint();
        }
    }

    public boolean isPartOfAnIdeal() {
        return this.isIdealVisibile;
    }

    public void setPartOfAnIdeal(boolean b) {
        this.isIdealVisibile = b;
    }

}
