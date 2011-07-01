package org.uncertweb.austalwps.util;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
//import de.ifgi.Austal.GUI;

public class AustalParser {
	
	public static void main(String[] args) {
		AustalOutputReader austal = new AustalOutputReader();
		String destination = "C:/PhD/software/Austal2000/Ly/results_250";//"C:/SOS/AUSTAL/values";
		String folder = "C:/PhD/software/Austal2000/Ly/new";
	//	int start = 1;
	//	int end = 10;
		int count = 127;
		
		// loop through folders
		for(int i = 101; i<=count; i++){
			String POfolder = folder + "/" + "PO" + i;
			austal.readAustalFiles(POfolder, false, destination);

		}
		//loop through point files
		/*for (int i=0;i<7;i++){
			austal.readFilesInFolders(folder, false, start, end);
			start = start+10;
			end = end+10;
			if(end>61){
				start=61;
				end=61;
			}
		}*/
	
		//loop through point files for uncertainty
	/*	for (int i=0;i<7;i++){
			austal.readFilesInFolders(folder, true, start, end);
			start = start+10;
			end = end+10;
			if(end>61){
				start=61;
				end=61;
			}
		}*/
	}
	
	private static String getCSVFile(String[] args) throws FileNotFoundException {
		String file;
		if (args.length == 0){
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open DMNA file");
			chooser.setFileFilter( new FileFilter(){
	            public boolean accept( File f ) {
	                return f.isDirectory() || f.getPath().endsWith("dmna") || f.getPath().endsWith("DMNA");
	            }
	            public String getDescription() {
	                return "Comma Seperated Value";
	            }
			});
			int returnVal = chooser.showOpenDialog( null );
			
			if(returnVal != JFileChooser.APPROVE_OPTION) {
				System.exit(0);
			}
			file = chooser.getSelectedFile().getPath();
			
			System.out.println("Opening CVS file: " + file);
		}
		else {
			file = new File( args[0] ).getPath();
		}
		return file;
	}

}
