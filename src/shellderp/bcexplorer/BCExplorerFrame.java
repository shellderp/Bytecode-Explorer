package shellderp.bcexplorer;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.*;
import shellderp.bcexplorer.ui.ClassTabPane;
import shellderp.bcexplorer.ui.MiddleClickCloseTabListener;
import shellderp.bcexplorer.ui.SwingUtils;
import shellderp.bcexplorer.ui.TabContextMenuListener;

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
 * Main application frame.
 * <p/>
 * Created by: Mike
 */

public class BCExplorerFrame extends JFrame {

    ClassHierarchy classHierarchy;

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
                        if (file.isDirectory()) {
                            classHierarchy.loadDirectory(file);
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
            }
        }).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        fileMenu.add(new AbstractAction("Unload all classes") {
            public void actionPerformed(ActionEvent e) {
                int button = JOptionPane.showConfirmDialog(BCExplorerFrame.this, "Unload all classes?", "Confirm unload", JOptionPane.OK_CANCEL_OPTION);
                if (button != JOptionPane.OK_OPTION)
                    return;

                closeAll();
                classHierarchy.unloadClasses();
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("New window") {
            public void actionPerformed(ActionEvent e) {
                new BCExplorerFrame().setVisible(true);
            }
        });

        JMenu toolsMenu = menubar.add(new JMenu("Tools"));
        toolsMenu.add(new AbstractAction("Search") {
            public void actionPerformed(ActionEvent e) {
                System.out.println("search not implemented");
            }
        }).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        toolsMenu.add(new AbstractAction("Request GC") {
            public void actionPerformed(ActionEvent e) {
                System.gc();
            }
        });

        setJMenuBar(menubar);

        resultTabPane = new JTabbedPane();
        resultTabPane.addMouseListener(new MiddleClickCloseTabListener(resultTabPane));
        resultTabPane.setPreferredSize(new Dimension(0, getHeight() / 4));
        resultTabPane.addMouseListener(new TabContextMenuListener(resultTabPane));

        try {
            classHierarchy = new ClassHierarchy(Object.class.getCanonicalName());
        } catch (ClassNotFoundException e) {
            // won't happen
        }

        classTabPane = new ClassTabPane(classHierarchy, resultTabPane);

        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplitPane.setContinuousLayout(true);
        verticalSplitPane.setTopComponent(classTabPane);
        verticalSplitPane.setBottomComponent(resultTabPane);
        verticalSplitPane.setResizeWeight(1);

        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitPane.setContinuousLayout(true);

        horizontalSplitPane.setLeftComponent(new JScrollPane(classHierarchy.getJTree(classTabPane)));
        horizontalSplitPane.setRightComponent(verticalSplitPane);

        add(horizontalSplitPane, BorderLayout.CENTER);

        validate();
    }

    public void closeAll() {
        classTabPane.removeAll();
        resultTabPane.removeAll();
    }

}

