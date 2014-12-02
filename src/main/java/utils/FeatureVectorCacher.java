package utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import curator.NewCachingCurator;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import structure.Blob;
import structure.LabelSet;
import structure.Lattice;
import structure.VarSet;

public class FeatureVectorCacher {
	static final String cachepath = Params.featcachepath;



	public static List<String> getMentionDetectionFeatures(
			VarSet varSet, LabelSet labelSet) throws Exception {
		String file=cachepath+"/"+NewCachingCurator.getMD5Checksum(
				varSet.simulProb.index+"_"+Arrays.asList(labelSet.labels));
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
	
	public static List<String> getMentionDetectionFeatures(
			VarSet varSet, LabelSet labelSet, int index) throws Exception {
		
		String file=cachepath+"/"+NewCachingCurator.getMD5Checksum(
				varSet.simulProb.index+"_"+Arrays.asList(labelSet.labels)+"_"+index);
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

	public static void cache(VarSet varSet, LabelSet labelSet, List<String> features) 
			throws Exception {
		String file=cachepath+"/"+NewCachingCurator.getMD5Checksum(
				varSet.simulProb.index+"_"+Arrays.asList(labelSet.labels));
		if(!IOUtils.exists(file))
		{
			try {
				LineIO.write(file, features);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void cache(VarSet varSet, LabelSet labelSet, int index, 
			List<String> features) throws Exception {
		String file=cachepath+"/"+NewCachingCurator.getMD5Checksum(
				varSet.simulProb.index+"_"+Arrays.asList(labelSet.labels)+"_"+index);
		if(!IOUtils.exists(file))
		{
			try {
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
//				System.out.println("caching ...");
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
