package shellderp.bcexplorer;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.*;
import shellderp.bcexplorer.ui.MiddleClickCloseTabListener;
import shellderp.bcexplorer.ui.SwingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Frame that holds the application together.
 * <p/>
 * Created by: Mike
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
        fileMenu.add(new AbstractAction("Load classes") {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                fc.setDialogTitle("Choose directories and/or jars to load");
                fc.setFileFilter(new FileNameExtensionFilter("Java class files", "class", "jar", "zip"));
                fc.setMultiSelectionEnabled(true);
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                fc.showOpenDialog(BCExplorerFrame.this);

                File[] files = fc.getSelectedFiles();

                if (files == null)
                    return;

                for (File file : files) {
                    try {
                        System.out.println("loading " + file);
                        if (file.isDirectory()) {
                            classHierarchy.loadDirectory(new File[]{file});
                        } else if (file.getName().endsWith(".jar")) {
                            JarFile jf = new JarFile(file);
                            classHierarchy.loadJarFile(jf);
                        } else if (file.getName().endsWith(".class")) {
                            ClassParser cp = new ClassParser(new FileInputStream(file), file.getName());
                            classHierarchy.loadClasses(Collections.singletonList(new ClassGen(cp.parse())));
                        }
                    } catch (IOException ex) {
                        SwingUtils.showErrorDialog(BCExplorerFrame.this, "Error loading", "Error loading '" + file.getName() + "'", "Message: " + ex);
                    }
                }

                // display the new class hierarchy
                classTree = classHierarchy.buildJTree(classTabPane);
                horizontalSplitPane.setLeftComponent(new JScrollPane(classTree));
            }
        });
        fileMenu.add(new AbstractAction("Unload all classes") {
            public void actionPerformed(ActionEvent e) {
                int button = JOptionPane.showConfirmDialog(BCExplorerFrame.this, "Unload all classes?", "Confirm unload", JOptionPane.OK_CANCEL_OPTION);
                if (button != JOptionPane.OK_OPTION)
                    return;

                closeAll();
                classHierarchy.unloadClasses();
                classTree = classHierarchy.buildJTree(classTabPane);
                horizontalSplitPane.setLeftComponent(new JScrollPane(classTree));
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

        try {
            classHierarchy = new ClassHierarchy(Object.class.getCanonicalName());
        } catch (ClassNotFoundException e) {
            // won't happen
            e.printStackTrace();
            System.exit(1);
        }

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
            classHierarchy.loadDirectory(new File[]{new File("out")});
            classTabPane = new ClassTabPane(classHierarchy, resultTabPane);
            classTree = classHierarchy.buildJTree(classTabPane);
            verticalSplitPane.setTopComponent(classTabPane);
            horizontalSplitPane.setLeftComponent(new JScrollPane(classTree));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //// remove after testing ////
        //////////////////////////////

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

