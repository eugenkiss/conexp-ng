package fcatools.conexpng;

import com.alee.laf.StyleConstants;
import com.alee.laf.WebLookAndFeel;
import com.google.common.collect.Sets;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcalib.FullObject;
import fcatools.conexpng.gui.MainFrame;
import fcatools.conexpng.model.FormalContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

/**
 *
 * The code for (re)storing the window location is from: <href
 * a="http://stackoverflow.com/a/7778332/283607">What is the best practice for
 * setting JFrame locations in Java?</a>
 *
 */
public class Main {

    public static final String settingsDirName = ".conexp-ng";
    public static final String optionsFileName = new File(
            getSettingsDirectory(), "options.prop").getPath();

    public static void main(String... args) {
//        WebLookAndFeel.install();
        try
        {
            // Setting up WebLookAndFeel style
            StyleConstants.smallRound = 2;
            StyleConstants.mediumRound = 2;
            StyleConstants.largeRound = 2;
            UIManager.setLookAndFeel ( WebLookAndFeel.class.getCanonicalName () );
        }
        catch ( Throwable e )
        {
            // Something went wrong
        }

        // Disable border around focused cells as it does not fit into the
        // context editor concept
        UIManager.put("Table.focusCellHighlightBorder", new EmptyBorder(0, 0,
                0, 0));
        // Disable changing foreground color of cells as it does not fit into
        // the context editor concept
        UIManager.put("Table.focusCellForeground", Color.black);

        final ProgramState state = new ProgramState();
        state.filePath = "untitled.cex";
        state.context = new FormalContext();
        state.context.addAttribute("female");
        state.context.addAttribute("juvenile");
        state.context.addAttribute("adult");
        state.context.addAttribute("male");
        try {
            state.context.addObject(new FullObject<>("girl", Sets
                    .newHashSet("female", "juvenile")));
            state.context.addObject(new FullObject<>("woman", Sets
                    .newHashSet("female", "adult")));
            state.context.addObject(new FullObject<>("boy", Sets
                    .newHashSet("male", "juvenile")));
            state.context.addObject(new FullObject<>("man", Sets
                    .newHashSet("male", "adult")));
        } catch (IllegalObjectException e1) {
            e1.printStackTrace();
        }

        // Create main window and take care of correctly saving and restoring
        // the last window location
        final MainFrame f = new MainFrame(state);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    storeOptions(f, state.lastOpened);
                    f.new CloseAction().actionPerformed(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        File optionsFile = new File(optionsFileName);
        if (optionsFile.exists()) {
            try {
                restoreOptions(f, state);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            f.setLocationByPlatform(true);
        }
        f.setVisible(true);

        // Force various GUI components to update
        state.contextChanged();
    }

    // Store location & size of UI & dir that was last opened from
    private static void storeOptions(Frame f, String lastOpened) throws Exception {
        File file = new File(optionsFileName);
        Properties p = new Properties();
        // restore the frame from 'full screen' first!
        f.setExtendedState(Frame.NORMAL);
        Rectangle r = f.getBounds();
        int x = (int) r.getX();
        int y = (int) r.getY();
        int w = (int) r.getWidth();
        int h = (int) r.getHeight();

        p.setProperty("x", "" + x);
        p.setProperty("y", "" + y);
        p.setProperty("w", "" + w);
        p.setProperty("h", "" + h);
        p.setProperty("lastOpened", lastOpened);

        BufferedWriter br = new BufferedWriter(new FileWriter(file));
        p.store(br, "Properties of the user frame");
    }

    // Restore location & size of UI & dir that was last opened from
    private static void restoreOptions(Frame f, ProgramState state) throws IOException {
        File file = new File(optionsFileName);
        Properties p = new Properties();
        BufferedReader br = new BufferedReader(new FileReader(file));
        p.load(br);

        int x = Integer.parseInt(p.getProperty("x"));
        int y = Integer.parseInt(p.getProperty("y"));
        int w = Integer.parseInt(p.getProperty("w"));
        int h = Integer.parseInt(p.getProperty("h"));
        String lastOpened = p.getProperty("lastOpened");

        Rectangle r = new Rectangle(x, y, w, h);
        f.setBounds(r);
        state.lastOpened = lastOpened;
    }

    // http://stackoverflow.com/a/193987/283607
    private static File getSettingsDirectory() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new IllegalStateException("user.home==null");
        }
        File home = new File(userHome);
        File settingsDirectory = new File(home, settingsDirName);
        if (!settingsDirectory.exists()) {
            if (!settingsDirectory.mkdir()) {
                throw new IllegalStateException(settingsDirectory.toString());
            }
        }
        return settingsDirectory;
    }

}
