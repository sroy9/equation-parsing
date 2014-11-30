// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5B814A038040140FB2D711671984EC15F4129F7484AD030ABB2B332881CFB7609C927AA28AEE798E3B0D851EA97A9747BB3F16B41A68BDA3B3452720D532AB31E38127140946014F837BE8EE8CDC0FBF53A2B1344F257D1DF0AC4ACFB9CB961422E20B2B0B5CE8DFBDB39272588000000

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


public class CompleteFeatures extends Classifier
{
  public CompleteFeatures()
  {
    containingPackage = "lbjgen";
    name = "CompleteFeatures";
  }

  public String getInputType() { return "structure.Expression"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Expression))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'CompleteFeatures(Expression)' defined on line 9 of CompleteClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Expression expr = (Expression) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    for (int i = 0; i < expr.features.size(); i++)
    {
      __id = "" + (expr.features.get(i));
      __value = "true";
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Expression[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'CompleteFeatures(Expression)' defined on line 9 of CompleteClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "CompleteFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof CompleteFeatures; }
}

