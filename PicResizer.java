//Imports are listed in full to show what's being used
//could just import javax.swing.* and java.awt.* etc..
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FileDialog;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PicResizer { // {{{

	//! This is the progress bar that will show the progress of image conversion.
	private JProgressBar progressBar = new JProgressBar( 0, 100 );

	private ConfigOptions configOptions = new ConfigOptions();

	//Note: Typically the main method will be in a
	//separate class. As this is a simple one class
	//example it's all in the one class.
	public static void main(String[] args) {
		new PicResizer();
	}

	public PicResizer() { // {{{
		JFrame guiFrame = new JFrame();

		//make sure the program exits when the frame closes
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiFrame.setTitle("Смаляване на Снимки");
		guiFrame.setSize(300,250);

		//This will center the JFrame in the middle of the screen
		guiFrame.setLocationRelativeTo(null);

		final JPanel progressPanel = new JPanel();
		JLabel progressLbl = new JLabel( "Свършена работа:" );
		progressPanel.setVisible( true );
		progressPanel.add( progressLbl );
		progressPanel.add( progressBar );

		JButton choosePics = new JButton( "Изберете Снимки");

		// Before starting the operation lets load our properties
		if( !loadSettings() ) {
			System.out.println( "Error in loading the settings!" );
			return;
		}

		//The ActionListener class is used to handle the
		//event that happens when the user clicks the button.
		//As there is not a lot that needs to happen we can 
		//define an anonymous inner class to make the code simpler.
		choosePics.addActionListener(new ActionListener()
				{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					// use the native file dialog on the mac
					FileDialog dialog = new FileDialog(guiFrame, "Изберете Снимки", FileDialog.LOAD );
					dialog.setMultipleMode( true );
					dialog.show();
					File[] listOfFiles = dialog.getFiles();
					if( listOfFiles == null ) { 
						// Added condition check
						System.out.println( "Empty list of files selected. Returning ..." );
						return; 
					}
					convertFiles( listOfFiles );

				}
				});

		//The JFrame uses the BorderLayout layout manager.
		//Put the two JPanels and JButton in different areas.
		guiFrame.add(progressPanel, BorderLayout.CENTER);

		guiFrame.add(choosePics, BorderLayout.SOUTH);

		//make sure the JFrame is visible
		guiFrame.setVisible(true);
	} // }}}

	private void convertFiles( File[] files ) { // {{{
		// I need a new thread in order to update the progress bar :/
		new Thread( new Runnable() {
			public void run() {
				// Initialize the progress bar
				progressBar.setValue( 0 );
				progressBar.setMaximum( files.length );
				progressBar.setStringPainted( true );

				// Create a thread pool
				ExecutorService executor = Executors.newFixedThreadPool( configOptions.numberOfThreads );

				// Open and convert all of the images
				for ( File file : files ) {
					WorkerThread worker = new WorkerThread( file );
					worker.setConfigOptions( configOptions );
					worker.setProgressBar( progressBar );
					executor.execute( worker );
				}
			}
		}).start();
	} // }}}
	
	private boolean loadSettings() { // {{{
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			this.configOptions.setConvertPath( prop.getProperty( "convertPath" ) );
			this.configOptions.setQuality( prop.getProperty( "quality" ) );
			this.configOptions.setSize( prop.getProperty( "size" ) );
			this.configOptions.setDestinationPath( prop.getProperty( "destinationPath" ) );
			this.configOptions.setNumberOfThreads( Integer.parseInt( prop.getProperty( "numberOfThreads" ) ) );
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	} // }}}

} // }}}

// This is a worker thread that will do a single job
class WorkerThread implements Runnable { // {{{
	private File file;
	private ConfigOptions configOptions = new ConfigOptions();
	private JProgressBar progressBar = new JProgressBar();

	public WorkerThread( File f ){
		this.file=f;
	}

	@Override
		public void run() {
			// Craft the destination path and the file name
			String path = this.configOptions.destinationPath + this.file.getName();
			// Add the JPEG file extension
			path = path.substring( 0, path.lastIndexOf( '.' ) ) + ".jpg";

			System.out.println( "Working on file " + this.file.getPath() );
			System.out.println( "Saving file to " + path );

			try {

				String[] cmd = { this.configOptions.convertPath, this.file.getPath(), "-resize", this.configOptions.size, "-quality", this.configOptions.quality, path };

				String finalCommand = "";
				for( String s : cmd ) {
					finalCommand += s + " ";
				}
				System.out.println( "Executing the command: " + finalCommand );

				ProcessBuilder pb = new ProcessBuilder( cmd );
				pb.redirectErrorStream(true);

				Process p = pb.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while((line=br.readLine())!=null){
					// Print the output of the convert command.
					System.out.println(line);
				}
				// Wait for the process to finish.
				p.waitFor();

				// Update the progress bar
				this.progressBar.setValue( this.progressBar.getValue() + 1 );

			} catch( IOException e ) {
				System.out.println( "Exception: " + e.getMessage() );
			} catch( InterruptedException e ) {
				System.out.println( "Exception: " + e.getMessage() );
			}

		}

	public void setConfigOptions( ConfigOptions o ) {
		this.configOptions = o;
	}

	public void setProgressBar( JProgressBar pb ) {
		this.progressBar = pb;
	}

} // }}}

// The properties structure.
class ConfigOptions { // {{{
	//! Path to the ImageMagick 'convert' utility
	public String convertPath = "";
	//! The quality of the output JPEG images
	public String quality = "";
	//! The size of the output JPEG images
	public String size = "";
	//! Where to save the converted images
	public String destinationPath = "";
	//! Number of threads to execute
	public int numberOfThreads = 1;

	public void setConvertPath( String s ) {
		this.convertPath = s;
	}

	public void setQuality( String s ) {
		this.quality = s;
	}

	public void setSize( String s ) {
		this.size = s;
	}

	public void setDestinationPath( String s ) {
		this.destinationPath = s;
	}

	public void setNumberOfThreads( int n ) {
		this.numberOfThreads = n;
	}
} // }}}

