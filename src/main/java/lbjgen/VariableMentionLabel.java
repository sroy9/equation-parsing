// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945580B4C2ACC4C4AC945FD4DCB29CCCFC3F94C4A4DC1D08274127124B6A28D8EA245B2005D79611C505F2704A4DA51A610028984A81B4000000

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


public class VariableMentionLabel extends Classifier
{
  public VariableMentionLabel()
  {
    containingPackage = "lbjgen";
    name = "VariableMentionLabel";
  }

  public String getInputType() { return "structure.Mention"; }
  public String getOutputType() { return "discrete"; }


  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public Feature featureValue(Object __example)
  {
    String result = discreteValue(__example);
    return new DiscretePrimitiveStringFeature(containingPackage, name, "", result, valueIndexOf(result), (short) allowableValues().length);
  }

  public String discreteValue(Object __example)
  {
    if (!(__example instanceof Mention))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'VariableMentionLabel(Mention)' defined on line 14 of VariableMentionClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Mention mention = (Mention) __example;

    return "" + (mention.label);
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Mention[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'VariableMentionLabel(Mention)' defined on line 14 of VariableMentionClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "VariableMentionLabel".hashCode(); }
  public boolean equals(Object o) { return o instanceof VariableMentionLabel; }
}

