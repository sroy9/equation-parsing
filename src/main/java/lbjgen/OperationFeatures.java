// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5B8BBA038050144F756A90CDB84425BF8236B9F60946C0BDC5567758090EFBBB84A2BA916ECC9798D359ECB0E1B0574799BC0C1D755A5ABF76980B890C8A91DD51F3C43B22941780A7CDAD88EE0C5F4F7FA639F235E0255593403613E9E2F6A729C860EAB2B5C68D67017C394A098000000

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


public class OperationFeatures extends Classifier
{
  public OperationFeatures()
  {
    containingPackage = "lbjgen";
    name = "OperationFeatures";
  }

  public String getInputType() { return "structure.Expression"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Expression))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'OperationFeatures(Expression)' defined on line 9 of OperationClassifier.lbj received '" + type + "' as input.");
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
      System.err.println("Classifier 'OperationFeatures(Expression)' defined on line 9 of OperationClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "OperationFeatures".hashCode(); }
  public boolean equals(Object o) { return o instanceof OperationFeatures; }
}

