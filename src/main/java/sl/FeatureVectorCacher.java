package sl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import structure.Blob;
import structure.Lattice;
import utils.Params;

public class FeatureVectorCacher {
	static final String cachepath = Params.featcachepath;

	public static List<String> getFeature(Blob blob, Lattice l) {
		
		String file=cachepath+"/"+blob.simulProb.index+"_"+l.hashCode();
		List<String> feats=null;
		if(IOUtils.exists(file))
		{
			try {
//				System.out.println("Found!");
				 feats = LineIO.read(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(feats!=null)
				return feats;
		}
		return null;
	}

	public static void cache(Blob blob, Lattice l, List<String> features) {
		String file=cachepath+"/"+blob.simulProb.index+"_"+l.hashCode();
		if(!IOUtils.exists(file))
		{
			try {
				System.out.println("caching ...");
				LineIO.write(file, features);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		else
//		{
//			System.out.println("Already in cache ...");
//		}
	}
}
