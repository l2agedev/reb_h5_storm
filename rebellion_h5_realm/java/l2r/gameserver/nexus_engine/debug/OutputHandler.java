// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package l2r.gameserver.nexus_engine.debug;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.JTextArea;

/**
 * @author hNoke
 *
 */
public class OutputHandler extends Handler
{
    public OutputHandler(JTextArea jtextarea)
    {
        a = new int[1024];
        i = 0;
        logFormatter = new LogFormatter(this);
        setFormatter(logFormatter);
        textArea = jtextarea;
    }

    public void close()
    {
    }

    public void flush()
    {
    }

    public void publish(LogRecord logrecord)
    {
    	try
		{
    		int length = textArea.getDocument().getLength();
            textArea.append(logFormatter.format(logrecord));
            textArea.setCaretPosition(textArea.getDocument().getLength());
            int j = textArea.getDocument().getLength() - length;
            if(a[i] != 0)
            {
                textArea.replaceRange("", 0, a[i]);
            }
            a[i] = j;
            i = (i + 1) % 1024;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }

    private int a[];
    private int i;
    Formatter logFormatter;
    private JTextArea textArea;
}
