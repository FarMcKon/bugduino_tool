/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2008 Ben Fry and Casey Reas

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package net.buglabs.bugbase_yt;


import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import processing.app.Editor;
import processing.app.tools.Tool;
import processing.app.Sketch;
import processing.app.Preferences;
import processing.app.Base;
/* START OF IMPORTS FROM SendAVRProgram.java */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/* END OF IMPORTS FROM SendAVRProgram.java */


/**
 * Custom 'Tool' Menu entry for programming a bugduino 
 * unit from SeedStudios running on a BugBaseYT via a TCP/IP 
 * upload to the BugBase.
 */
public class ProgramBugduino implements Tool { 

	Editor editor;
	int slot;		/* target slot the bugduino is loaded into/on on the BugBase */ 
	String ipAddr; /* IP address of the BugBase on the local network */
    final int bugduinoPort= 8806; /* port used for the TCP/IP data transfer */
	final boolean verbose = true; //for debugging
	
	/* Standard Processing IDE tool init function. 
	 */
	public void init(Editor editor) {
		this.editor = editor;
		slot = -1;
		ipAddr = "";
	}

	/* Standard Processing IDE tool menu item function 
	*/
	public String getMenuTitle() {
		return "Program Bugduino";
	}


	/* Standard Processing IDE 'run' funcing. This function runs
	* when the item is selected from the 'Tool' menu
	*/
	 public void run() {
		//This is modeled after Sketch's 'Export Applet' function
		int keepSettings = 0;
	
		if(this.slot == -1){
			if(this.verbose) 	System.out.println("slot is -1!");
			keepSettings = updateSettings();
		}
	
		// update sketch info
		Sketch sk = editor.getSketch();
	
		//sk.ensureExistence();

		sk.getCurrentCode().setProgram(editor.getText());

		//TODO: if needed in the future, reload code from an external editor 
/*    if (Preferences.getBoolean("editor.external")) {
    	  sk.load();
     }*/

    // Create a fresh applet folder (needed before preproc is run below)
    File tempBuildFolder = Base.getBuildFolder();
    // Nuke the old applet folder because it can cause trouble
/*    if (Preferences.getBoolean("export.delete_target_folder")) {
      Base.removeDir(tmpBuildFolder);
    }
	
	tmpBuildFolder.mkdirs();
 */
	String foundName = sk.getPrimaryFile().getName();
	System.out.println("foundName a: "+foundName);
	if(foundName.endsWith(".pde") ){
		foundName = foundName.substring(0, foundName.length() - 4);
		foundName = foundName.concat(".cpp.hex");
	}
	System.out.println("foundName: "+foundName);
    // use to be able to build the sketch
	//but sk.build is off-limits now...
	/*
	try{
		if(this.verbose)  System.out.println( "buiding code." );
	    //`foundName = sk.build(this.verbose);
	    // (already reported) error during export, exit this function
	    if (foundName == null) {
			System.err.println("no valid foundname from sk.build");
			return ;
		}
	} catch (Exception e ) 	{
		e.printStackTrace();
		return;
	}
	*/
	if(keepSettings < 0) {
		System.err.println("Error in generating settings: " + keepSettings);
	}
	
	if(this.verbose)  System.out.println( "Uploading to BUGduino." );
	System.out.println(sk.getCodeFolder().getPath() + " <-cf\n");
	System.out.println(sk.getDataFolder().getPath()+ " <-df\n");
	System.out.println(tempBuildFolder.getPath()+ " <-bf\n");

	File avrFile = new File( tempBuildFolder.getPath() + "/" + foundName );
	
	System.out.println("avr filename: "+ avrFile);
	if(avrFile.exists()) {

		boolean status = sendAVRProgramToBUG( avrFile,
								this.ipAddr, this.bugduinoPort, this.slot, this.verbose );
 	
		if(this.verbose) System.out.println("sendAVRProgramToBUG status: " + status);
	 	if(this.verbose) System.out.println("keepSettings: " + keepSettings);
	}
	else {
		System.err.println("avr file does not exist. Please rebuild your sketch");
	}
	//user specified one-time settings, wipe out settings
 	if(keepSettings != 1) { 
		this.wipeSettings();
		return;
	}
  }


	/* Clears this classes  ipAddress and slot */
	protected void wipeSettings() {
 		if(this.verbose) System.out.println("wiping out ip addr and slot setting");
		this.ipAddr = "";
		this.slot = -1;
	}



/* START OF STUFF COPIED OVER FROM SendAVRProgram.java */

	/* This function takes a biary blog, an IP Address, a port number,
	 * and a bugbase slot number. Sends a write command,the slot number, and the 
	 * binary blob to IPAddress:port, assuming that IP/Port is a BugBase waiting for code
	 * returns true on sucess, false otherwise 
	*/  
  protected boolean sendAVRProgramToBUG( File avrBinaryData, String bugAddress,
		 int port, int targetSlot, boolean verbose ) {

        if(verbose) System.out.println("Sending " + avrBinaryData.toURI() + " to " + bugAddress);

        int avrBinaryDataLength = 0;
        Socket socket = null;
        DataOutputStream socketOutput = null;
        OutputStream rawSocketOutput = null;
        DataInputStream socketInput = null;
        InputStream rawSocketInput = null;
        FileInputStream avrBinaryDataReader = null;
        //File avrBinaryData = null;
        byte[] buffer = null;

        /* open a connection */
        try {
			//old way: socket = new Socket( bugAddress.substring( 0, ( bugAddress.length() - 2 ) ), bugPort );
            socket = new Socket(bugAddress, port );
            rawSocketOutput = socket.getOutputStream();
            rawSocketInput = socket.getInputStream();
            socketOutput = new DataOutputStream( rawSocketOutput );
            socketInput = new DataInputStream( rawSocketInput );
        } catch (UnknownHostException e) {
            System.err.println( "ERROR: Could not contact host." );
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println( "ERROR: Could not create connection to host." );
            e.printStackTrace();
        return false;
        }
      /* only block for 5 seconds */
        try {
            socket.setSoTimeout( 5000 );
        } catch (SocketException e3) {
            System.err.println( "ERROR: Could not set socket timeout time... for some reason." );
            e3.printStackTrace();
        return false;
        }

        try {
            /* Initiate a write operation */
            socketOutput.writeUTF( "WRIT" );
            //socketOutput.write( new char[] { 'W', 'R', 'I', 'T' } );

            /* Send the slot of the BUGduino */
			socketOutput.writeInt(targetSlot);
//            socketOutput.writeInt( Integer.parseInt( bugAddress.substring( bugAddress.length() - 1 ) ) );
        } catch (IOException e2) {
            System.err.println( "ERROR: Could not write to the socket." );
            e2.printStackTrace();
            return false;
        }

        /* Send the file size */
        //avrBinaryData = new File( avrBinary.getLocationURI() );
        avrBinaryDataLength = (int)avrBinaryData.length();
        if(verbose) System.out.println( "File length: " + avrBinaryData.length() + ", "
					 + avrBinaryDataLength );
        try {
            socketOutput.writeInt( avrBinaryDataLength );
        } catch (IOException e2) {
            System.err.println( "ERROR: Could not send the file size." );
            e2.printStackTrace();
            return false;
        }

        /* let's hope nobody has any really big binary files (they'd better
         * not - the address space of the ATmega328 is only so big!)
         */
        buffer = new byte[avrBinaryDataLength];

        try {
            avrBinaryDataReader = new FileInputStream( avrBinaryData );
        } catch (FileNotFoundException e1) {
            System.err.println( "ERROR: Could not open binary file." );
            e1.printStackTrace();
            return false;
        }

        /* read the data from the file */
        try {
            avrBinaryDataReader.read( buffer, 0, avrBinaryDataLength );
        } catch (IOException e1) {
            System.err.println( "ERROR: Could not read binary file." );
            e1.printStackTrace();
            return false;
        }

        /* send the data out */
        try {
            socketOutput.write( buffer, 0, avrBinaryDataLength );
        } catch (IOException e1) {
            System.err.println( "ERROR: Could not send binary file to host." );
            e1.printStackTrace();
            return false;
        }
      /* get the return code */
        /*
        try {
            int returnCode;
            returnCode = socketInput.readInt();
            System.out.println( "Return code: " + returnCode + ( returnCode == 0 ? " (success)" : " (fail+ure)" ) );
        } catch (IOException e1) {
            System.err.println( "ERROR: Did not receive return code from host." );
            e1.printStackTrace();
        }
        */

        /* close the connection */
        try {
            avrBinaryDataReader.close();
            socketInput.close();
            socketOutput.close();
            socket.close();
        } catch (IOException e) {
            System.err.println( "ERROR: Could not close connection." );
            e.printStackTrace();
            return false;
        }

        if(verbose) System.out.println( "Data sent." );

        return true;
    }
/* END OF STUFF COPIED OVER FROM SendAVRProgram.java */
	
	//Returned some combo of IP and port in the past, no longer valid
	// Looks like it was part of a Eclipse plugin for programming bugduinos
	/*
	  protected String getBUGAddress() {
        //Michael, I can help with the dragonfly UI integration later.
        return "172.16.0.20"; //HACK temp hack to get this running
        String data = null;
        BufferedReader conf = null;
		// read from eclipse config file for eclipse bugduino?
        try {
            conf = new BufferedReader( new FileReader( new File( "/etc/bugduinoEclipse.conf" ) ) );
        } catch (FileNotFoundException e) {
            System.err.println( "ERROR: File \"/etc/bugduinoEclipse.conf\" not found." );
            e.printStackTrace();
        }

        try {
            data = conf.readLine();
        } catch (IOException e) {
            System.err.println( "ERROR: Could not read file \"/etc/bugduinoEclipse.conf\"." );
            e.printStackTrace();
        }

        
        System.out.println( "Please enter the IP (or, address) and the slot of the BUG that your 
		BUGduino+ will go to as follows: [IP]-[SLOT]. Note that slots are indexed starting at 0.");
        
        try {
            ip = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        } catch (IOException e) {
            System.out.println( "ERROR: Problem reading data.\n" );
            e.printStackTrace();
        }
        

        return data;

    }
	*/

	
	/* Gets updated IP and BugSlot settings from the user via dialog boxes
	 * returns 1 if user as selected 'keep these settings this session
	 * returns 0 if settings to be used only once
	 * a negative value otherwise
	 */
	protected int updateSettings() {
		int keepChanges = 0;
		// grab the target IP addr from the user
		String s = (String)JOptionPane.showInputDialog(
			editor, "Please select your BugBase's IP address:",
			"Set BugBase IP ", 	JOptionPane.PLAIN_MESSAGE,
			null, null, "172.16.0.20");

		//If a string was returned, say so.
		if ((s != null) && (s.length() > 0)) {
			this.ipAddr = s;
			keepChanges= -1; //flag that settings changed 
		}
		else 
			return -1; //failed to get an IP addr
		s = null;
	
		//grab the slot target the user specifies
		s  = (String)JOptionPane.showInputDialog(
			editor, 
			"Please select your BugBase's slot (2 or 3):",
			//"Please select your BugBase's slot (0 - 3):",//disabled, 0/1 not programmable 
			"Set Bugbase slot", 	JOptionPane.PLAIN_MESSAGE,
			null, null, "2");

		//If a string was returned, say so.
		if ((s != null) && (s.length() > 0)) {
			this.slot = Integer.parseInt(s); 
			keepChanges= -1; //flag that settings changed 
			if(this.slot < 2 || this.slot > 3) { 
				System.out.println("BETA: Only slots 0 - 3 are valid currently" );
				return -2 ;//no not keep these settings
			}
		} else 
			return -3; //failed to get an IP addr, do not keep settings
		
		//find out if we want to use this once, or keep these settings	
		Object[] options = { "Use Once", "Use this entire session" };
		int result =  JOptionPane.showOptionDialog(null, "Use these settings once, or for "+
			"this whole session?", "Warning",
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
            null, options, options[0]);
		
 		if (result == JOptionPane.YES_OPTION)
			keepChanges = 0;
		else
			keepChanges = 1;
		
		System.out.println("user would like to keep changes? " + keepChanges);
		return keepChanges;
	}

}
