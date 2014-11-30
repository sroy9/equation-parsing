// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B8800000000000000056B81BA038040144F756A11EE09885743D6AB4B9EF433AC24272CEEAD428FFE9388659AE13CCB7F01B159ECA07F4A296872F6CCE2B4EB239FAA4B0BF6CBE7C88EF48F06A541142B340714BD514F781DC4B7AD89CB912693BEA3696C89D8FF6DC4F0211768BEACE0B16BF20FB66837A59000000

package lbjgen;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import java.util.List;
import lbj.*;
import parser.*;
import structure.*;


public class VariableMentionFeatures extends Classifier
{
  public VariableMentionFeatures()
  {
    containingPackage = "lbjgen";
    name = "VariableMentionFeatures";
  }

  public String getInputType() { return "structure.Mention"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Mention))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'VariableMentionFeatures(Mention)' defined on line 9 of VariableMentionClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Mention mention = (Mention) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    for (int i = 0; i < mention.features.size(); i++)
    {
      __id = "" + (mention.features.get(i));
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Mention[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'VariableMentionFeatures(Mention)' defined on line 9 of VariableMentionClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "VariableMentionFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof VariableMentionFeatures; }
}

