package l2r.gameserver.cache;

import l2r.gameserver.Config;
import l2r.gameserver.idfactory.IdFactory;

import gov.nasa.worldwind.formats.dds.DDSConverter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImagesChache
{
	private static final Logger _log = LoggerFactory.getLogger(ImagesChache.class);
	
	private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final static Lock readLock = lock.readLock();
	
	private static final int[] SIZES = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024 };
	private static final int MAX_SIZE = SIZES[(SIZES.length - 1)];
	
	public static final Pattern HTML_PATTERN = Pattern.compile("%image:(.*?)%", 32);
	
	private static final ImagesChache _instance = new ImagesChache();

	private final  Map<String, Integer> _imagesId = new HashMap<String, Integer>();
	 
	private final FastMap<Integer, byte[]> _images = new FastMap<Integer, byte[]>();
	
	public static final ImagesChache getInstance()
	{
		return _instance;
	}
	
	private ImagesChache()
	{
		load();
	}
	
	public void reload()
	{
		try
		{
			if (_imagesId != null && _images != null)
			{
				_imagesId.clear();
				_images.clear();
				load();
			}
		}
		catch (Exception e)
		{
			_log.error("ImagesChache: Error while Reloading image cache.", e);
		}
		
	}
	
	public void load()
	{
		try
		{
			_log.info("ImagesChache: Loading images...");
			
			File folder = new File(Config.DATAPACK_ROOT, "data/images");
			
			if (!folder.exists() || !folder.isDirectory())
			{
				_log.info("ImagesChache: Files missing, loading aborted.");
				return;
			}
			
			int count = 0;
			for (File file : folder.listFiles())
			{
				if (!file.isDirectory())
				{
					if (checkImageFormat(file))
					{
						count++;
						
						String fileName = file.getName();
						try
						{
							ByteBuffer bf = DDSConverter.convertToDDS(file);
							byte[] image = bf.array();
							int imageId = IdFactory.getInstance().getNextId();
							
							_imagesId.put(fileName.toLowerCase(), Integer.valueOf(imageId));
							_images.put(imageId, image);
							
							//_log.info("ImagesChache: Loaded " + fileName + " image.");
						}
						catch (IOException ioe)
						{
							_log.error("ImagesChache: Error while loading " + fileName + " image.");
						}
					}
				}
			}
			
			_log.info("ImagesChache: Loaded " + count + " images");
		}
		catch(Exception e)
		{
			_log.warn("Error while loading custom images:", e);
		}
	}

	public int getImageId(String val)
	{
		int imageId = 0;
		
		readLock.lock();
		try
		{
			if (_imagesId.get(val.toLowerCase()) != null)
				imageId = _imagesId.get(val.toLowerCase()).intValue();
		}
		finally
		{
			readLock.unlock();
		}
		
		return imageId;
	}
	
	public byte[] getImage(int imageId)
	{
		byte[] image = null;
		
		readLock.lock();
		try
		{
			image = _images.get(imageId);
		}
		finally
		{
			readLock.unlock();
		}
		
		return image;
	}
	
	@SuppressWarnings("unused")
	private static File resizeImage(File file)
	{
		BufferedImage image;
		try
		{
			image = ImageIO.read(file);
		}
		catch (IOException ioe)
		{
			_log.error("ImagesChache: Error while resizing " + file.getName() + " image.");
			return null;
		}
		
		if (image == null)
		{
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		
		boolean resizeWidth = true;
		if (width > MAX_SIZE)
		{
			image = image.getSubimage(0, 0, MAX_SIZE, height);
			resizeWidth = false;
		}
		
		boolean resizeHeight = true;
		if (height > MAX_SIZE)
		{
			image = image.getSubimage(0, 0, width, MAX_SIZE);
			resizeHeight = false;
		}
		
		int resizedWidth = width;
		if (resizeWidth)
		{
			for (int size : SIZES)
			{
				if (size >= width)
				{
					resizedWidth = size;
					break;
				}
			}
		}
		int resizedHeight = height;
		if (resizeHeight)
		{
			for (int size : SIZES)
			{
				if (size >= height)
				{
					resizedHeight = size;
					break;
				}
			}
		}
		if (resizedWidth != width || resizedHeight != height)
		{
			for (int x = 0; x < resizedWidth; x++)
			{
				for (int y = 0; y < resizedHeight; y++)
				{
					image.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
			String filename = file.getName();
			String format = filename.substring(filename.lastIndexOf("."));
			try
			{
				ImageIO.write(image, format, file);
			}
			catch (IOException ioe)
			{
				_log.error("ImagesChache: Error while resizing " + file.getName() + " image.");
				return null;
			}
		}
		return file;
	}
	
	public FastMap<Integer, byte[]> getChachedImages()
	{
		return _images;
	}
	
	private static boolean checkImageFormat(File file)
	{
		String filename = file.getName();
		int dotPos = filename.lastIndexOf(".");
		String format = filename.substring(dotPos);
		if (format.equalsIgnoreCase(".jpg") || format.equalsIgnoreCase(".png") || format.equalsIgnoreCase(".bmp"))
			return true;
		return false;
	}
}
