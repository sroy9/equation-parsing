// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000B49CC2E4E2A4D2945507ECFCD28C1023C72139253743C5B2A0A825B8B8333F3F412518C4D450B1D558A6500A2C2D228888E52345DB5B24D2007717EEE544000000

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


public class CompleteLabel extends Classifier
{
  public CompleteLabel()
  {
    containingPackage = "lbjgen";
    name = "CompleteLabel";
  }

  public String getInputType() { return "structure.Expression"; }
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
    if (!(__example instanceof Expression))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'CompleteLabel(Expression)' defined on line 14 of CompleteClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Expression expr = (Expression) __example;

    return "" + (expr.complete);
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Expression[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'CompleteLabel(Expression)' defined on line 14 of CompleteClassifier.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "CompleteLabel".hashCode(); }
  public boolean equals(Object o) { return o instanceof CompleteLabel; }
}

