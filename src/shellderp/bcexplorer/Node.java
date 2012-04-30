package shellderp.bcexplorer;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;

public class Node<T> implements TreeNode, Comparable<Node<T>>, Iterable<Node<T>> {
    private T value;
    private Node<T> parent;
    private List<Node<T>> children = new ArrayList<Node<T>>();
    private String displayText;

    public Node(T value) {
        set(value);
    }

    // Value-related methods
    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        if (value == null)
            throw new RuntimeException("Node value cannot be null");
    }

    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node comp = (Node) o;
            if (!value.getClass().equals(comp.value.getClass()))
                return false;
            if (value.equals(comp.value))
                return true;
        }
        return false;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String toString() {
        if (displayText != null)
            return displayText;
        return value == null ? null : value.toString();
    }
    
    // Child-related methods
    public Node<T> addChild(Node<T> child) {
        child.changeParent(this);
        return child;
    }

    public Node<T> addChild(T value) {
        return addChild(new Node<T>(value));
    }

    public void removeAllChildren() {
        for (Node<T> child : children) {
            child.removeAllChildren();
        }
        children.clear();
    }

    public void changeParent(Node<T> newParent) {
        if (parent != null) {
            parent.children.remove(this);
        }
        parent = newParent;
        if (newParent != null) {
            newParent.children.add(this);
        }
    }


    // Utility methods
    public Node<T> findChild(T value) {
        for (Node<T> child : children) {
            if (child.get().equals(value))
                return child;
        }
        return null;
    }
    
    public TreePath getPath() {
        if (parent != null)
            return parent.getPath().pathByAddingChild(this);
        return new TreePath(this);
    }

    public String treeString() {
        return treeString(0);
    }

    public String treeString(int indent) {
        String newline = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }

        sb.append(toString());

        indent++;
        for (Node<T> child : children) {
            sb.append(newline).append(child.treeString(indent));
        }

        return sb.toString();
    }

    public void sortAll() {
        for (Node<T> child : children) {
            child.sortAll();
        }
        Collections.sort(children);
    }


    // TreeNode implemented methods
    public int getIndex(TreeNode treeNode) {
        return children.indexOf(treeNode);
    }

    public Node<T> getChildAt(int i) {
        return children.get(i);
    }

    public int getChildCount() {
        return children.size();
    }

    public Node<T> getParent() {
        return parent;
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }

    // Comparable implemented methods
    public int compareTo(Node<T> other) {
        if (value instanceof Comparable) {
            return ((Comparable) value).compareTo(other.value);
        }
        return this.toString().compareTo(other.toString());
    }

    // Iterable implemented methods
    public Iterator<Node<T>> iterator() {
        return children.iterator();
    }

}
