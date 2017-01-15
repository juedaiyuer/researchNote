

import java.io.*;
import java.awt.*;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import java.awt.image.*;
import com.sun.media.codec.video.jpeg.NativeEncoder;
import com.sun.image.codec.jpeg.*;

// If you prefer to amend the sun example FrameAccess.java then to get every
// frame include
// out.setFlags(in.getFlags() | Buffer.FLAG_NO_SYNC); when copying the input
// attributes
// in process(Buffer in, Buffer out). This avoids dropping frames whilst not
// removing likes
// of end of media events.

public class vid2jpg extends Frame implements ControllerListener
{
	Processor p;
	Object waitObj = new Object();
	boolean stateOK = true;
	DataSourceHandler handler;
	imgPanel currPanel;int imgWidth;int imgHeight;
	DirectColorModel dcm = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF);
	MemoryImageSource sourceImage;Image outputImage;
	String sep = System.getProperty("file.separator");
	NativeEncoder e;
	int[] outvid;
	int startFr = 1;int endFr = 1000;int countFr = 0;
	boolean sunjava=true;

	/**
	 * Static main method
	 */
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			System.out.println("No media address.");
			new vid2jpg("file:testcam04.avi");	// or alternative "vfw://0" if
												// webcam
		}
		else
		{
			String path = args[0].trim();
			System.out.println(path);
			new vid2jpg(path);
		}
	}
	
	/**
	 * Constructor
	 */
	public vid2jpg(String path)
	{
		MediaLocator ml;String args = path;

		if((ml = new MediaLocator(args)) == null)
		{
			System.out.println("Cannot build media locator from: " + args);
		}

		if(!open(ml))
		{
			System.out.println("Failed to open media source");
		}
    }

	/**
	 * Given a MediaLocator, create a processor and start
	 */
	private boolean open(MediaLocator ml)
	{
		System.out.println("Create processor for: " + ml);
		
		try
		{
			p = Manager.createProcessor(ml);
		}
		catch (Exception e)
		{
			System.out.println("Failed to create a processor from the given media source: " + e);
			return false;
		}
		
		p.addControllerListener(this);
		
		// Put the Processor into configured state.
		p.configure();
		if(!waitForState(p.Configured))
		{
			System.out.println("Failed to configure the processor.");
			return false;
		}
		
		// Get the raw output from the Processor.
		p.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW));
		
		TrackControl tc[] = p.getTrackControls();
		if(tc == null)
		{
			System.out.println("Failed to obtain track controls from the processor.");
			return false;
		}
		
		TrackControl videoTrack = null;
		for(int i = 0; i < tc.length; i++)
		{
			if(tc[i].getFormat() instanceof VideoFormat)
			{
				tc[i].setFormat(new RGBFormat(null, -1, Format.byteArray, -1.0F, 24, 3, 2, 1));
				videoTrack = tc[i];
			}
			else
			tc[i].setEnabled(false);
		}		
		if(videoTrack == null)
		{
			System.out.println("The input media does not contain a video track.");
			return false;
		}		
		System.out.println("Video format: " + videoTrack.getFormat());

		p.realize();
		if(!waitForState(p.Realized))
		{
			System.out.println("Failed to realize the processor.");
			return false;
		}
		
		// Get the output DataSource from the processor and set it to the
		// DataSourceHandler.
		DataSource ods = p.getDataOutput();
		handler = new DataSourceHandler();
		try
		{
			handler.setSource(ods);	// also determines image size
		}
		catch(IncompatibleSourceException e)
		{
			System.out.println("Cannot handle the output DataSource from the processor: " + ods);
			return false;
		}
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		currPanel = new imgPanel(new Dimension(imgWidth,imgHeight));
		add(currPanel);
		pack();
		// setLocation(100,100);
		setVisible(true);
		
		handler.start();
		
		// Prefetch the processor.
		p.prefetch();
		
		if(!waitForState(p.Prefetched))
		{
			System.out.println("Failed to prefetch the processor.");
			return false;
		}

		// Start the processor
		// p.setStopTime(new Time(20.00));
		p.start();
		
		return true;
	}

	/**
	 * Sets image size
	 */
	private void imageProfile(VideoFormat vidFormat)
	{
		System.out.println("Push Format "+vidFormat);
		Dimension d = (vidFormat).getSize();
		System.out.println("Video frame size: "+ d.width+"x"+d.height);
		imgWidth=d.width;
		imgHeight=d.height;
	}

	/**
	 * Called on each new frame buffer
	 */
	private void useFrameData(Buffer inBuffer)
	{
		countFr++;
		if(countFr<startFr || countFr>endFr)return;

		try
		{
			printDataInfo(inBuffer);

			if(inBuffer.getData()!=null)	// vfw://0 can deliver nulls
			{
				if(outvid==null)outvid = new int[imgWidth*imgHeight];
				outdataBuffer(outvid,(byte[])inBuffer.getData());
				setImage(outvid);

				// name so OS more likely to sort correctly if doing jpg2vid
				String paddedname = "00000000000000000000"+inBuffer.getTimeStamp();
				String sizedname = paddedname.substring(paddedname.length()-20);

				if(sunjava)
				{
					saveJpeg(outputImage,"image_"+sizedname+".jpg");		
				}
				else
				{
					if(e==null)initJpeg((RGBFormat)inBuffer.getFormat());
					byte[] b = fetchJpeg(inBuffer);
					String filename = "image_"+sizedname+".jpg";
					makeFile(filename, b);
				}
			}
		}
		catch(Exception e){System.out.println(e);}	
	}

	/**
	 * Tidy on finish
	 */
	public void tidyClose()
	{
		handler.close();
		p.close();
		if(e!=null)e.close();
		// dispose(); // frame
		System.out.println("Sources closed");
	}

	/**
	 * Draw image to AWT frame
	 */
	private void setImage(int[] outpix)
	{
		if(sourceImage==null)sourceImage = new MemoryImageSource(imgWidth, imgHeight, dcm, outpix, 0, imgWidth);
		outputImage = createImage(sourceImage);
		currPanel.setImage(outputImage);	
	}

	/**
	 * Block until the processor has transitioned to the given state
	 */
	private boolean waitForState(int state)
	{
		synchronized(waitObj)
		{
			try
			{
				while(p.getState() < state && stateOK)
				waitObj.wait();
			}
			catch (Exception e)
			{
			}
		}
		return stateOK;
	}

	/**
	 * Controller Listener.
	 */
	public void controllerUpdate(ControllerEvent evt)
	{
		if(evt instanceof ConfigureCompleteEvent ||	evt instanceof RealizeCompleteEvent || evt instanceof PrefetchCompleteEvent)
		{
			synchronized(waitObj)
			{
				stateOK = true;
				waitObj.notifyAll();
			}
		}
		else
		if(evt instanceof ResourceUnavailableEvent)
		{
			synchronized(waitObj)
			{
				stateOK = false;
				waitObj.notifyAll();
			}
		}
		else
		if(evt instanceof EndOfMediaEvent || evt instanceof StopAtTimeEvent)
		{
			tidyClose();
		}
	}

	/**
	 * Prints frame info
	 */
	private void printDataInfo(Buffer buffer)
    {
		System.out.println(" Time stamp: " + buffer.getTimeStamp());
		System.out.println(" Time: " + (buffer.getTimeStamp()/10000000)/100f+"secs");
		System.out.println(" Sequence #: " + buffer.getSequenceNumber());
		System.out.println(" Data length: " + buffer.getLength());
		System.out.println(" Key Frame: " + (buffer.getFlags()==Buffer.FLAG_KEY_FRAME)+" "+buffer.getFlags());
    }

	/**
	 * Converts buffer data to pixel data for display
	 */
	public void outdataBuffer(int[] outpix, byte[] inData)	// could use
															// JavaRGBConverter
	{
		boolean flip=false;
		{
			int srcPtr = 0;
			int dstPtr = 0;
			int dstInc = 0;
			if(flip)
			{
				dstPtr = imgWidth * (imgHeight - 1);
				dstInc = -2 * imgWidth;
			}
			
			for(int y = 0; y < imgHeight; y++)
			{
				for(int x = 0; x < imgWidth; x++)
				{
					byte red = inData[srcPtr + 2];
					byte green = inData[srcPtr + 1];
					byte blue = inData[srcPtr];
					
					int pixel = (red & 0xff) << 16 | (green & 0xff) << 8 | (blue & 0xff) << 0;
					outpix[dstPtr] = pixel;
					srcPtr += 3;
					dstPtr += 1;
				}
				dstPtr += dstInc;
			}
		}
		Thread.yield();
	}

	/**
	 * Jpeg encoder and file writer
	 */
	public void saveJpeg(Image img, String filename)
	{
		BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		g.drawImage(img, 0, 0, this);
		
		BufferedOutputStream fw=null;
		try
		{
			fw = new BufferedOutputStream(new FileOutputStream("images"+sep+filename));
		}
		catch(IOException e ){System.out.println("makeFile "+e);}

		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fw);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
		param.setQuality(0.6f,false);
		encoder.setJPEGEncodeParam(param);
		
		try 
		{ 
			encoder.encode(bi); 
			fw.close(); 

			System.out.println(" *** Created file "+filename);
		}
		catch (java.io.IOException io) 
		{
			System.out.println("IOException"); 
		}
	}

	/**
	 * Inits a jpeg encoder - if using MS JVM - but frame sizes have to be
	 * multiples of 8
	 */
	private void initJpeg(RGBFormat vfin) throws Exception
	{
		float val=0.6F;

		int widpx=imgWidth;
		int hgtpx=imgHeight;

		// This encoder need multiples of 8 - use another if a problem
		if(widpx % 8 != 0 || hgtpx % 8 != 0)
		{
			System.out.println("Width = "+imgWidth+" "+"Height = "+imgHeight);
			throw new Exception("Image sizes not /8");
		}

		VideoFormat vfout = new VideoFormat("jpeg", new Dimension(widpx,hgtpx), widpx * hgtpx * 3, Format.byteArray, -1F);
		
		e = new NativeEncoder();
		e.setInputFormat(vfin);
		e.setOutputFormat(vfout);
		e.open();

		Control cs[] = (Control[])e.getControls();
		for (int i = 0; i < cs.length; i++)
		{
			if (cs[i] instanceof QualityControl)
			{			
				QualityControl qc = (QualityControl)cs[i];
				qc.setQuality(val);
				break;			
			}
		}
	}

	/**
	 * Fetches a jpeg from buffer data - if using MS JVM
	 */
	private byte[] fetchJpeg(Buffer inBuffer) throws Exception
	{
		Buffer outBuffer=new Buffer();	// may need new to keep threadsafe if
										// extended
		int result = e.process(inBuffer, outBuffer);
		int lengthF = outBuffer.getLength();
		byte[] b = new byte[lengthF];
		System.arraycopy(outBuffer.getData(), 0, b, 0, lengthF);
		return b;
	}

	/**
	 * Saves jpeg to file
	 */
	public void makeFile(String filename, byte[] b)
	{
		BufferedOutputStream fw=null;
		try
		{
			fw = new BufferedOutputStream(new FileOutputStream("images"+sep+filename));
			fw.write(b, 0, b.length);fw.close();
			
			System.out.println(" *** Created file "+filename);
		}
		catch(IOException e ){System.out.println("makeFile "+e);}
	}
	


	/***************************************************************************
	 * Inner classes
	 **************************************************************************/

	/**
	 * A DataSourceHandler class to read from a DataSource and displays
	 * information of each frame of data received.
	 */
	class DataSourceHandler implements BufferTransferHandler
	{
		DataSource source;
		PullBufferStream pullStrms[] = null;
		PushBufferStream pushStrms[] = null;
		Buffer readBuffer;

		/**
		 * Sets the media source this MediaHandler should use to obtain content.
		 */
		private void setSource(DataSource source) throws IncompatibleSourceException
		{
			// Different types of DataSources need to handled differently.
			if(source instanceof PushBufferDataSource) 
			{
				pushStrms = ((PushBufferDataSource) source).getStreams();
				
				// Set the transfer handler to receive pushed data from the push
				// DataSource.
				pushStrms[0].setTransferHandler(this);

				// Set image size
				imageProfile((VideoFormat)pushStrms[0].getFormat());
			}
			else
			if(source instanceof PullBufferDataSource)
			{
				System.out.println("PullBufferDataSource!");
			
				// This handler only handles push buffer datasource.
				throw new IncompatibleSourceException();
			}
			
			this.source = source;
			readBuffer = new Buffer();
		}
		
		/**
		 * This will get called when there's data pushed from the
		 * PushBufferDataSource.
		 */
		public void transferData(PushBufferStream stream)
		{
			try
			{
				stream.read(readBuffer);
			}
			catch(Exception e)
			{
				System.out.println(e);
				return;
			}

			// Just in case contents of data object changed by some other thread
			Buffer inBuffer = (Buffer)(readBuffer.clone());

			// Check for end of stream
			if(readBuffer.isEOM())
			{
				System.out.println("End of stream");
				return;
			}

			// Do useful stuff or wait
			useFrameData(inBuffer);
		}

		public void start()
		{
			try{source.start();}catch(Exception e){System.out.println(e);}
		}
		
		public void stop()
		{
			try{source.stop();}catch(Exception e){System.out.println(e);}
		}	
		
		public void close(){stop();}
		
		public Object[] getControls()
		{
			return new Object[0];
		}
		
		public Object getControl(String name)
		{
			return null;
		}
	}
	
	/**
	 * Panel extension
	 */
	class imgPanel extends Panel
	{
		Dimension size;
		public Image myimg = null;

		public imgPanel(Dimension size)
		{
			super();
			this.size = size;
		}

		public Dimension getPreferredSize()
		{
			return size;
		}

		public void update(Graphics g)
		{
			paint(g);
		}

		public void paint(Graphics g)
		{
			if (myimg != null)
			{
				g.drawImage(myimg, 0, 0, this);
			}
		}

		public void setImage(Image img)
		{
			if(img!=null)
			{
				this.myimg = img;
				update(getGraphics());
			}
		}
	}

}


