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
			VarSet varSet, LabelSet labelSet) {
		String file = null;
		try {
			file = cachepath+"/"+NewCachingCurator.getMD5Checksum(
					varSet.simulProb.index+"_"+varSet.sentId+"_"+
							Arrays.asList(labelSet.labels));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<String> feats=null;
		if(IOUtils.exists(file)) {
			try {
				 feats = LineIO.read(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if(feats!=null) {
				return feats;
			}
		}
		return null;
	}
	
	public static List<String> getMentionDetectionFeatures(
			VarSet varSet, LabelSet labelSet, int index) {		
		String file = null;
		try {
			file = cachepath+"/"+NewCachingCurator.getMD5Checksum(
					varSet.simulProb.index+"_"+varSet.sentId+"_"+
							Arrays.asList(labelSet.labels)+"_"+index);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<String> feats=null;
		if(IOUtils.exists(file)) {
			try {
				 feats = LineIO.read(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if(feats!=null) {
				return feats;
			}
		}
		return null;
	}

	public static void cache(VarSet varSet, LabelSet labelSet, List<String> features) {
		String file = null;
		try {
			file = cachepath+"/"+NewCachingCurator.getMD5Checksum(
					varSet.simulProb.index+"_"+varSet.sentId+"_"+
							Arrays.asList(labelSet.labels));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(!IOUtils.exists(file)) {
			try {
				LineIO.write(file, features);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void cache(
			VarSet varSet, LabelSet labelSet, int index, List<String> features) {
		String file = null;
		try {
			file = cachepath+"/"+NewCachingCurator.getMD5Checksum(
					varSet.simulProb.index+"_"+varSet.sentId+"_"+
							Arrays.asList(labelSet.labels)+"_"+index);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(!IOUtils.exists(file)) {
			try {
				LineIO.write(file, features);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static List<String> getFeature(Blob blob, Lattice l) {
		String file=cachepath+"/"+blob.simulProb.index+"_"+l.hashCode();
		List<String> feats=null;
		if(IOUtils.exists(file)) {
			try {
				 feats = LineIO.read(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if(feats!=null) {
				return feats;
			}
		}
		return null;
	}

	public static void cache(Blob blob, Lattice l, List<String> features) {
		String file=cachepath+"/"+blob.simulProb.index+"_"+l.hashCode();
		if(!IOUtils.exists(file)) {
			try {
				LineIO.write(file, features);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
