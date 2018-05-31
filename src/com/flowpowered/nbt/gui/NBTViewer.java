/*
 * Decompiled with CFR 0_129.
 */
package com.flowpowered.nbt.gui;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntArrayTag;
import com.flowpowered.nbt.ListTag;
import com.flowpowered.nbt.ShortArrayTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.itemmap.StringMapReader;
import com.flowpowered.nbt.regionfile.SimpleRegionFileReader;
import com.flowpowered.nbt.stream.NBTInputStream;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public class NBTViewer
extends JFrame
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final int MAX_WIDTH = 32;
    private String format = "";
    private JTree tree;
    private DefaultMutableTreeNode top;

    public NBTViewer() {
        JMenuBar menu = new JMenuBar();
        this.setJMenuBar(menu);
        JMenu file = new JMenu("File");
        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(this);
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(this);
        file.add(open);
        file.addSeparator();
        file.add(exit);
        menu.add(file);
        this.top = new DefaultMutableTreeNode("NBT Contents");
        this.tree = new JTree(this.top);
        JScrollPane treeView = new JScrollPane(this.tree);
        this.add(treeView);
        this.setTitle("SimpleNBT Viewer");
        this.setSize(300, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(3);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch (ClassNotFoundException e) {
                }
                catch (InstantiationException e) {
                }
                catch (IllegalAccessException e) {
                }
                catch (UnsupportedLookAndFeelException e) {
                    // empty catch block
                }
                NBTViewer viewer = new NBTViewer();
                viewer.setVisible(true);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command == null) {
            return;
        }
        if (command.equals("Open")) {
            this.openFile();
        } else if (command.equals("Exit")) {
            System.exit(0);
        }
    }

    private void openFile() {
        FileDialog d = new FileDialog(this, "Open File", 0);
        d.setVisible(true);
        if (d.getDirectory() == null || d.getFile() == null) {
            return;
        }
        File dir = new File(d.getDirectory());
        File f = new File(dir, d.getFile());
        List tags = this.readFile(f);
        this.updateTree(tags);
        this.top.setUserObject("NBT Contents [" + this.format + "]");
        ((DefaultTreeModel)this.tree.getModel()).nodeChanged(this.top);
    }

    private List<Tag<?>> readFile(File f) {
        List tags = this.readRawNBT(f, true);
        if (tags != null) {
            this.format = "Compressed NBT";
            return tags;
        }
        tags = this.readRawNBT(f, false);
        if (tags != null) {
            this.format = "Uncompressed NBT";
            return tags;
        }
        tags = SimpleRegionFileReader.readFile(f);
        if (tags != null) {
            this.format = "SimpleRegionFile";
            return tags;
        }
        tags = StringMapReader.readFile(f);
        if (tags != null) {
            this.format = "StringMap";
            return tags;
        }
        this.format = "Unknown";
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private List<Tag<?>> readRawNBT(File f, boolean compressed) {
        ArrayList tags;
        tags = new ArrayList();
        try {
            FileInputStream is = new FileInputStream(f);
            NBTInputStream ns = new NBTInputStream(is, compressed);
            try {
                boolean eof = false;
                while (!eof) {
                    try {
                        tags.add(ns.readTag());
                    }
                    catch (EOFException e) {
                        eof = true;
                    }
                }
            }
            finally {
                try {
                    ns.close();
                }
                catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Unable to close file", "File Read Error", 0);
                }
            }
        }
        catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Unable to open file", "File Read Error", 0);
        }
        catch (IOException e) {
            return null;
        }
        return tags;
    }

    private void updateTree(List<Tag<?>> tags) {
        int i;
        DefaultTreeModel model = (DefaultTreeModel)this.tree.getModel();
        this.top.removeAllChildren();
        model.nodeStructureChanged(this.top);
        if (tags == null) {
            return;
        }
        if (tags.size() == 1) {
            model.insertNodeInto(NBTViewer.getNode(tags.get(0)), this.top, 0);
        } else {
            i = 0;
            for (Tag t : tags) {
                model.insertNodeInto(NBTViewer.getNode(t), this.top, i);
                ++i;
            }
        }
        for (i = 0; i < this.tree.getRowCount(); ++i) {
            this.tree.collapseRow(i);
        }
        this.tree.expandRow(0);
        if (tags.size() == 1) {
            this.tree.expandRow(1);
        }
    }

    private static DefaultMutableTreeNode getNode(Tag<?> tag) {
        return NBTViewer.getNode(tag, true);
    }

    private static DefaultMutableTreeNode getNode(Tag<?> tag, boolean includeName) {
        if (tag == null) {
            return new DefaultMutableTreeNode("Empty");
        }
        if (tag instanceof CompoundTag) {
            return NBTViewer.getNode((CompoundTag)tag);
        }
        if (tag instanceof ListTag) {
            try {
                return NBTViewer.getNode((ListTag)tag);
            }
            catch (ClassCastException e) {
            }
        } else {
            if (tag instanceof ByteArrayTag) {
                return NBTViewer.getNode((ByteArrayTag)tag);
            }
            if (tag instanceof ShortArrayTag) {
                return NBTViewer.getNode((ShortArrayTag)tag);
            }
            if (tag instanceof IntArrayTag) {
                return NBTViewer.getNode((IntArrayTag)tag);
            }
        }
        String message = includeName ? tag.getName() + ":" + tag.getValue() : tag.getValue().toString();
        return new DefaultMutableTreeNode(message);
    }

    private static DefaultMutableTreeNode getNode(CompoundTag tag) {
        CompoundMap map = tag.getValue();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tag.getName() + " [Map]");
        for (Tag t : map.values()) {
            DefaultMutableTreeNode child = NBTViewer.getNode(t);
            root.add(child);
        }
        return root;
    }

    private static DefaultMutableTreeNode getNode(ListTag<Tag<?>> tag) {
        Object values = tag.getValue();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tag.getName() + " [List]");
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            Tag t = (Tag)iterator.next();
            DefaultMutableTreeNode child = NBTViewer.getNode(t, false);
            root.add(child);
        }
        return root;
    }

    private static DefaultMutableTreeNode getNode(ByteArrayTag tag) {
        byte[] values = tag.getValue();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tag.getName() + " [byte[" + values.length + "]");
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (byte v : values) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            String s = Byte.toString(v);
            if (sb.length() + s.length() > 32) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(sb.toString());
                root.add(child);
                sb.setLength(0);
            }
            sb.append(Integer.toHexString(v & 255));
        }
        sb.append("}");
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(sb.toString());
        root.add(child);
        return root;
    }

    private static DefaultMutableTreeNode getNode(ShortArrayTag tag) {
        short[] values = tag.getValue();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tag.getName() + " [short[" + values.length + "]]");
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (short v : values) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            String s = Short.toString(v);
            if (sb.length() + s.length() > 32) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(sb.toString());
                root.add(child);
                sb.setLength(0);
            }
            sb.append(v);
        }
        sb.append("}");
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(sb.toString());
        root.add(child);
        return root;
    }

    private static DefaultMutableTreeNode getNode(IntArrayTag tag) {
        int[] values = tag.getValue();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tag.getName() + " [int[" + values.length + "]]");
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int v : values) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            String s = Integer.toString(v);
            if (sb.length() + s.length() > 32) {
                sb.append("<br>");
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(sb.toString());
                root.add(child);
                sb.setLength(0);
            }
            sb.append(v);
        }
        sb.append("}");
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(sb.toString());
        root.add(child);
        return root;
    }

}

