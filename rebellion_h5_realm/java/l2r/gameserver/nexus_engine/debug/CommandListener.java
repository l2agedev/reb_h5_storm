package l2r.gameserver.nexus_engine.debug;

import l2r.gameserver.nexus_interface.NexusEvents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

/**
 * @author hNoke
 *
 */
public class CommandListener implements ActionListener
{
    private final JTextField textField;
    
    public CommandListener(DebugConsole servergui, JTextField jtextfield)
    {
        textField = jtextfield;
    }

    @Override
    public void actionPerformed(ActionEvent actionevent)
    {
        String s = textField.getText().trim();
        if(s.length() > 0)
        {
        	NexusEvents.consoleCommand(s);
        	DebugConsole.userCmd("[COMMAND] " + s + "\n");
        }
        textField.setText("");
    }
}
