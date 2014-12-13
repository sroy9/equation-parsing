package curator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.ws.handler.Handler;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
/**
 * Caching Curator MakeShift Version 2.0
 * Full functionality of old caching curator is not provided. 
 * You may need to modify code to suit your needs.
 * To add views, modify the code in getTextAnnotation
 * @author upadhya3
 *
 */
public class NewCachingCurator {

	public CuratorClient curator = null;
	public String pathToSaveCachedFiles = null;
	public List<String>views;
	
	public NewCachingCurator(String curatorHost, 
			int port, 
			String pathToSaveCachedFiles, 
			List<String>views) throws Exception {
		if(curatorHost != null) {
			this.curator = new CuratorClient(curatorHost, port);
		}
		this.pathToSaveCachedFiles = pathToSaveCachedFiles;
		this.views= views;
	}

	public TextAnnotation getTextAnnotationWithSingleView(
			String text, String view, boolean forceUpdate) throws Exception {
		String md5sum = getMD5Checksum(view+":"+text+":"+view);
		String savePath = pathToSaveCachedFiles + "/" + md5sum + ".cached";
		TextAnnotation ta;
		if (new File(savePath).exists()) {
			ta = AnnotationHelper.deserialize(readJson(savePath));
		} else {
			System.out.println("Called curator with " + text);
			ta = curator.getTextAnnotation("", "", text, forceUpdate);
			if(view.equals(ViewNames.SHALLOW_PARSE)) {
				curator.addChunkView(ta, forceUpdate);
			} else if(view.equals(ViewNames.POS)) {
				curator.addPOSView(ta, forceUpdate);
			} else if(view.equals(ViewNames.PARSE_BERKELEY)) {
				curator.addBerkeleyParse(ta, forceUpdate);
			} else if(view.equals(ViewNames.PARSE_STANFORD)) {
				curator.addStanfordParse(ta, forceUpdate);
			} else if(view.equals(ViewNames.SRL_VERB)) {
				curator.addSRLVerbView(ta, forceUpdate);
			} else if(view.equals(ViewNames.DEPENDENCY_STANFORD)) {
				curator.addStanfordDependencyView(ta, forceUpdate);
			} else if(view.equals(ViewNames.DEPENDENCY)) {
				curator.addDependencyTreeView(ta, forceUpdate, ViewNames.DEPENDENCY, ViewNames.DEPENDENCY);
			} else if(view.equals(ViewNames.LEMMA)) {
				curator.addLemmaView(ta, forceUpdate);
			} else {
				// Does not cache annotation if no view added
				return ta;
			}
			writeJson(savePath, AnnotationHelper.serialize(ta));
		}
		return ta;
	}

	public static void writeJson(String fileName, String json) 
			throws FileNotFoundException, IOException{
		ObjectOutputStream writer= new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(fileName)));
		writer.writeObject(json);
		writer.close();
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        //add owners permission
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        //add group permissions
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        //add others permissions
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
         
        Files.setPosixFilePermissions(Paths.get(fileName), perms);
	}
	
	public static String readJson(String fileName) 
			throws ClassNotFoundException, IOException{
		ObjectInputStream reader = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(fileName)));
		String json= (String) reader.readObject();
		reader.close();
		return json;
	}

	public static String getMD5Checksum(String text) throws Exception {
		MessageDigest complete = MessageDigest.getInstance("MD5");
		complete.update(text.getBytes(), 0, text.getBytes().length);
		byte[] b = complete.digest();
		String result = "";
		for (int i = 0; i < b.length; i++)
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		return result;
	}

	public static void main(String[] args) throws Exception {
//		String curatorServer = "trollope.cs.illinois.edu";
//		int curatorPort = 9010;
//		String path= "/shared/experiments/upadhya3/curatorCache";
//		ArrayList<String> myviews = new ArrayList<String>();
//		myviews.add("SHALLOW_PARSE");
//		myviews.add("POS");	// there should be a better way of doing this
//		NewCachingCurator curator = new NewCachingCurator(curatorServer, curatorPort,path, myviews);
//		boolean forceUpdate = false;
//		
//		TextAnnotation ta = curator.getTextAnnotation("The pt is a 60 yo F with cancer.", forceUpdate);
//		
//		
//		SpanLabelView slv = (SpanLabelView) ta.getView(ViewNames.SHALLOW_PARSE);
//		for(Constituent chunks:slv.getConstituents())
//			System.out.println(chunks.getSurfaceString());
//		Timer timer = new Timer();
//		for(int time=0;time<10;time++)
//		{
//			timer.schedule(new SayHello(time), 0, 1000);
//		}
	}
}