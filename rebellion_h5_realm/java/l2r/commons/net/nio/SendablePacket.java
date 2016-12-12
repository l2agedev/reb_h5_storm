package l2r.commons.net.nio;

public abstract class SendablePacket<T> extends AbstractPacket<T>
{
	/** byte 8 bits (−128 to 127) */
	protected void writeC(int data)
	{
		getByteBuffer().put((byte) data);
	}

	/** double 128 bits (−170,141,183,460,469,231,731,687,303,715,884,105,728 to 170,141,183,460,469,231,731,687,303,715,884,105,727) */
	protected void writeF(double value)
	{
		getByteBuffer().putDouble(value);
	}

	/** short 16 bits (−32,768 to 32,767) */
	protected void writeH(int value)
	{
		getByteBuffer().putShort((short) value);
	}

	/** int 32 bits (−2,147,483,648 to 2,147,483,647) */
	protected void writeD(int value)
	{
		getByteBuffer().putInt(value);
	}

	/** long 64 bits (−9,223,372,036,854,775,808 to 9,223,372,036,854,775,807) */
	protected void writeQ(long value)
	{
		getByteBuffer().putLong(value);
	}

	/** byte[] */
	protected void writeB(byte[] data)
	{
		getByteBuffer().put(data);
	}

	/** CharSequence (String, StringBuffer, etc) */
	protected void writeS(CharSequence charSequence)
	{
		if(charSequence != null)
		{
			int length = charSequence.length();
			for(int i = 0; i < length; i++)
				getByteBuffer().putChar(charSequence.charAt(i));
		}
		getByteBuffer().putChar('\000');
	}

	protected abstract boolean write();
}
