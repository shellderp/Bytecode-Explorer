package shellderp.bcexplorer;

import org.apache.bcel.generic.*;
import shellderp.bcexplorer.ui.MiddleClickCloseTabListener;
import shellderp.bcexplorer.ui.SwingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;

/**
 * TODO:
 *
 * MAIN FUNCTIONALITY:
 * [ ] go to declaration button in context menu when right clicking instructions such as a method call, field reference, etc
 * [ ] when showing a context menu for an instruction, list all fields and methods references in a submenu i.e. Args -> A, B
 * [ ] replace ref searching with a function that calls back to each reference, or minimum returns a list rather than nodes
 * [ ] go to super declaration
 * [ ] find all references to a class, not to a specific member. either from class hierarchy or a class tab
 * [ ] search classnames and constants
 * [ ] search in super classes and subclasses because of virtual calls
 *    - 3 scenarios:
 *     - target/baseclass has a method m which a subclass s does not override, in this case show all references to m invoked on s
 *     - target/baseclass has a method m which a subclass s overrides, in this case do not show references to m invoked on s
 *     - target has a method m which is an overriden method from baseclass b; in this case, try to determine if a reference to b.m == a.m
 * [ ] decompiled view
 * [ ] save modified classes to jar
 *     - can keep track of where each file came from, then "save to respective jars" or "save all to single jar"
 *      - make a list of all files that didn't come from jars and prompt for jar name
 *     - also have option to save all files to wherever they came from, and possibly for only modified files
 *      - "save all classes to original files" "save only modified classes to original files"
 * [ ] field/method name refactoring
 * [ ] annotation - highlighting, making notes in classes, allow hotlinking to members
 *
 * CONVENIENCE:
 * [ ] merge loading jars & directories. select any files and auto load. if directory, load all jars in directory as well as classes
 * [ ] add option to load additional classes ontop of the existing tree
 * [ ] give option to hide certain constant pool entries (instruction.e. class index)
 *     - maybe just group constant pool entries by type?
 * [ ] option to display tree by package instead of inheritance (and other layouts?)
 * [ ] session storing and loading
 * [ ] button to sync open tab with class hierarchy tree, similar to eclipse
 * [ ] back/forward to navigate between class areas
 *
 * timeless:
 * [ ] code cleanup
 *
  */

public class BCExplorerFrame extends JFrame {
    private JSplitPane horizontalSplitPane;
    private JSplitPane verticalSplitPane;

    private ClassHierarchy classHierarchy;
    private JTree classTree;

    private ClassTabPane classTabPane;
    private JTabbedPane resultTabPane;

    public BCExplorerFrame() {
        super("Bytecode Explorer");
        this.setSize(1000, 800);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JMenuBar menubar = new JMenuBar();

        JMenu fileMenu = menubar.add(new JMenu("File"));
        fileMenu.add(new AbstractAction("Load jar") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                fc.setFileFilter(new FileNameExtensionFilter("JAR file", "jar", "zip"));
                fc.showOpenDialog(BCExplorerFrame.this);
                File file = fc.getSelectedFile();
                if (file != null) {
                    try {
                        JarFile jf = new JarFile(file);
                        classHierarchy = ClassHierarchy.fromJarFile(jf);
                        jf.close();

                        // the jar loaded successfully, close all active tabs and display the new class hierarchy
                        BCExplorerFrame.this.closeAll();

                        classTabPane = new ClassTabPane(classHierarchy, resultTabPane);
                        classTree = classHierarchy.buildJTree(classTabPane);
                        verticalSplitPane.setTopComponent(classTabPane);
                        horizontalSplitPane.setLeftComponent(new JScrollPane(classTree));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtils.showErrorDialog(BCExplorerFrame.this, "Error loading", "Error loading '" + file.getName() + "'", "Message: " + ex);
                    }
                }
            }
        });
        fileMenu.add(new AbstractAction("Load directories") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                fc.setDialogTitle("Choose directories");
                fc.setMultiSelectionEnabled(true);
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.showOpenDialog(BCExplorerFrame.this);
                File[] files = fc.getSelectedFiles();
                if (files != null) {
                    try {
                        classHierarchy = ClassHierarchy.fromDirectories(files);

                        // the jar loaded successfully, close all active tabs and display the new class hierarchy
                        BCExplorerFrame.this.closeAll();

                        classTabPane = new ClassTabPane(classHierarchy, resultTabPane);
                        classTree = classHierarchy.buildJTree(classTabPane);
                        verticalSplitPane.setTopComponent(classTabPane);
                        horizontalSplitPane.setLeftComponent(new JScrollPane(classTree));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtils.showErrorDialog(BCExplorerFrame.this, "Error loading", "Error loading from directory", "Message: " + ex);
                    }
                }
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("New window") {
            public void actionPerformed(ActionEvent e) {
                new BCExplorerFrame().setVisible(true);
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("Load session") {
            public void actionPerformed(ActionEvent e) {
                // TODO minor
            }
        }).setEnabled(false);
        fileMenu.add(new AbstractAction("Save session") {
            public void actionPerformed(ActionEvent e) {
                // TODO minor
            }
        }).setEnabled(false);

        JMenu toolsMenu = menubar.add(new JMenu("Tools"));
        toolsMenu.add(new AbstractAction("Search classes by name") {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        }).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        toolsMenu.add(new AbstractAction("Search constants") {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });

        setJMenuBar(menubar);

        resultTabPane = new JTabbedPane();
        resultTabPane.addMouseListener(new MiddleClickCloseTabListener(resultTabPane));
        resultTabPane.setPreferredSize(new Dimension(0, getHeight() / 4));

        classTabPane = new ClassTabPane(classHierarchy, resultTabPane);

        verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplitPane.setContinuousLayout(true);
        verticalSplitPane.setTopComponent(classTabPane);
        verticalSplitPane.setBottomComponent(resultTabPane);
        verticalSplitPane.setResizeWeight(1);

        horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitPane.setContinuousLayout(true);

        JLabel label = new JLabel("No classes loaded");
        label.setBorder(new EmptyBorder(3, 3, 3, 3));
        label.setVerticalAlignment(SwingConstants.TOP);

        horizontalSplitPane.setLeftComponent(label);
        horizontalSplitPane.setRightComponent(verticalSplitPane);

        add(horizontalSplitPane, BorderLayout.CENTER);

        // TODO remove after testing
        try {
            classHierarchy = ClassHierarchy.fromDirectories(new File[]{new File("out")});
            classTabPane = new ClassTabPane(classHierarchy, resultTabPane);
            classTree = classHierarchy.buildJTree(classTabPane);
            verticalSplitPane.setTopComponent(classTabPane);
            horizontalSplitPane.setLeftComponent(new JScrollPane(classTree));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ///////////////////////////

        validate();
    }

    public void closeAll() {
        classTabPane.removeAll();
        resultTabPane.removeAll();
    }

    /*public void actionPerformed(ActionEvent e) {
        } else if (cmd.equals(".Tools.Search.Classes")) {
            String input = JOptionPane.showInputDialog("Enter class name (regex):");
            if (input == null)
                return;
            input = ".*?" + input + ".*?";
            Vector<ClassGen> matches = new Vector<ClassGen>();
            for (String name : loadedClasses.keySet()) {
                if (name.matches(input)) {
                    matches.add(loadedClasses.get(name));
                }
            }
            showMatches(matches);
        } else if (cmd.equals(".Tools.Search.Constants")) {
            String input = JOptionPane.showInputDialog("Enter search string (regex):");
            if (input == null)
                return;
            input = ".*?" + input + ".*?";
            Vector<ClassGen> matches = new Vector<ClassGen>();
            for (ClassGen cg : loadedClasses.values()) {
                ConstantPoolGen cpgen = cg.getConstantPool();
                for (int instruction = 0; instruction < cpgen.getSize(); instruction++) {
                    Constant constant = cpgen.getConstant(instruction);
                    if (constant instanceof ConstantString) {
                        ConstantString cs = (ConstantString) constant;
                        if (cs.getBytes(cpgen.getConstantPool()).matches(input)) {
                            matches.add(cg);
                            break;
                        }
                    } else if (constant instanceof ConstantUtf8) {
                        ConstantUtf8 cs = (ConstantUtf8) constant;
                        if (cs.getBytes().matches(input)) {
                            matches.add(cg);
                            break;
                        }
                    } else {
                        if (constant != null && constant.toString().matches(input)) {
                            matches.add(cg);
                            break;
                        }
                    }
                }
            }
            showMatches(matches);
        }
    }*/

    public void showMatches(Vector<ClassGen> matches) {
        if (matches.size() == 0)
            return;
        JList list = new JList(matches);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setSelectedIndex(0);
        Object[] message = {"The following results matched your search,  please select which you would like to take action on: ", new JScrollPane(list)};
        /* TODO
         * will make this display in same way as reference tree
         * no need for Find in Tree, instead a better idea is to be able to find in tree any open class
         * also provide context menu to do so, in case the user doesn't want to open a class every time
         * we could also display all results in their tree form, so find in tree isn't needed
        */
        Object[] options = {"Open", "Find in Tree", "Cancel"};
        int result = JOptionPane.showOptionDialog(this, message, "Search Results", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
        int[] values = list.getSelectedIndices();
        if (values.length > 0) {
            switch (result) {
                case 0:
                    for (int idx : values) {
                        ClassGen cg = matches.get(idx);
                        classTabPane.openClassTab(cg);
                    }
                    break;
                case 1:
                    for (int idx : values) {
                        ClassGen cg = matches.get(idx);
                        goToClassNode(classTree, cg);
                    }
                    break;
            }
        }
    }

    public void goToClassNode(JTree t, Object o) {
        SwingUtils.goToNode(t, SwingUtils.buildTreePath(classHierarchy.rootClass, o));
    }

}

