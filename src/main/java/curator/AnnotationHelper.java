package curator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import edu.illinois.cs.cogcomp.core.algorithms.Sorters;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.AbstractEdisonSerializer;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.edison.sentences.TokenizerUtilities.SentenceViewGenerators;
/**
 * Fixing the broken default edison serializer that has token offset 
 * exceptions
 * Note: SentenceViewGenerators.LBJSentenceViewGenerator must be used
 * in the generation for the original annotation. You could change
 * it below for other needs
 * 
 * Use the bottom 3 static methods only
 * @author cheng88
 *
 */
public class AnnotationHelper extends AbstractEdisonSerializer {

    JsonObject writeTextAnnotation(TextAnnotation ta) {

        JsonObject json = new JsonObject();

        writeString("corpusId", ta.getCorpusId(), json);
        writeString("id", ta.getId(), json);
        writeString("text", ta.getText(), json);
        writeStringArray("tokens", ta.getTokens(), json);

        writeSentences(ta, json);

        JsonArray views = new JsonArray();
        for (String viewName : Sorters.sortSet(ta.getAvailableViews())) {
            if (viewName.equals(ViewNames.SENTENCE))
                continue;

            JsonObject view = new JsonObject();

            writeString("viewName", viewName, view);
            views.add(view);

            JsonArray viewData = new JsonArray();
            List<View> topKViews = ta.getTopKViews(viewName);

            for (int k = 0; k < topKViews.size(); k++) {
                JsonObject kView = new JsonObject();
                writeView(topKViews.get(k), kView);

                viewData.add(kView);
            }

            view.add("viewData", viewData);
        }

        json.add("views", views);

        return json;
    }

    TextAnnotation readTextAnnotation(String string) throws Exception {
        JsonObject json = (JsonObject) new JsonParser().parse(string);

        String corpusId = readString("corpusId", json);
        String id = readString("id", json);
        String text = readString("text", json);
        readStringArray("tokens", json);
        readSentences(json);

        TextAnnotation ta = new TextAnnotation(corpusId, id, text,
                SentenceViewGenerators.LBJSentenceViewGenerator);

        JsonArray views = json.getAsJsonArray("views");
        for (int i = 0; i < views.size(); i++) {
            JsonObject view = (JsonObject) views.get(i);
            String viewName = readString("viewName", view);

            JsonArray viewData = view.getAsJsonArray("viewData");
            List<View> topKViews = new ArrayList<View>();

            for (int k = 0; k < viewData.size(); k++) {
                JsonObject kView = (JsonObject) viewData.get(k);

                topKViews.add(readView(kView, ta));
            }

            ta.addView(viewName, topKViews);

        }

        return ta;
    }

    private static void writeView(View view, JsonObject json) {
        writeString("viewType", view.getClass().getCanonicalName(), json);

        writeString("viewName", view.getViewName(), json);

        writeString("generator", view.getViewGenerator(), json);

        if (view.getScore() != 0)
            writeDouble("score", view.getScore(), json);

        List<Constituent> constituents = view.getConstituents();

        if (constituents.size() > 0) {
            JsonArray cJson = new JsonArray();
            for (int i = 0; i < view.getNumberOfConstituents(); i++) {

                Constituent constituent = constituents.get(i);
                JsonObject c = new JsonObject();
                writeConstituent(constituent, c);

                cJson.add(c);
            }

            json.add("constituents", cJson);
        }

        List<Relation> relations = view.getRelations();

        if (relations.size() > 0) {

            JsonArray rJson = new JsonArray();

            for (int i = 0; i < relations.size(); i++) {
                Relation r = relations.get(i);

                Constituent src = r.getSource();
                Constituent tgt = r.getTarget();

                int srcId = constituents.indexOf(src);
                int tgtId = constituents.indexOf(tgt);

                JsonObject rJ = new JsonObject();

                writeString("relationName", r.getRelationName(), rJ);

                if (r.getScore() != 0)
                    writeDouble("score", r.getScore(), rJ);
                writeInt("srcConstituent", srcId, rJ);
                writeInt("targetConstituent", tgtId, rJ);

                rJson.add(rJ);
            }

            json.add("relations", rJson);
        }

    }

    private static View readView(JsonObject json, TextAnnotation ta) throws Exception {

        String viewClass = readString("viewType", json);

        String viewName = readString("viewName", json);

        String viewGenerator = readString("generator", json);

        double score = 0;
        if (json.has("score"))
            score = readDouble("score", json);

        View view = createEmptyView(ta, viewClass, viewName, viewGenerator, score);

        List<Constituent> constituents = new ArrayList<Constituent>();

        if (json.has("constituents")) {

            JsonArray cJson = json.getAsJsonArray("constituents");

            for (int i = 0; i < cJson.size(); i++) {
                JsonObject cJ = (JsonObject) cJson.get(i);
                Constituent c = readConstituent(cJ, ta, viewName);
                constituents.add(c);
                view.addConstituent(c);
            }
        }

        if (json.has("relations")) {
            JsonArray rJson = json.getAsJsonArray("relations");
            for (int i = 0; i < rJson.size(); i++) {
                JsonObject rJ = (JsonObject) rJson.get(i);

                String name = readString("relationName", rJ);

                double s = 0;
                if (rJ.has("score"))
                    s = readDouble("score", rJ);

                int src = readInt("srcConstituent", rJ);
                int tgt = readInt("targetConstituent", rJ);

                Relation rel = new Relation(name, constituents.get(src), constituents.get(tgt), s);

                view.addRelation(rel);
            }
        }
        return view;
    }

    private static void writeConstituent(Constituent c, JsonObject cJ) {
        writeString("label", c.getLabel(), cJ);

        if (c.getConstituentScore() != 0)
            writeDouble("score", c.getConstituentScore(), cJ);
        writeInt("start", c.getStartSpan(), cJ);
        writeInt("end", c.getEndSpan(), cJ);

        if (c.getAttributeKeys().size() > 0) {
            JsonObject properties = new JsonObject();

            for (String key : Sorters.sortSet(c.getAttributeKeys())) {
                writeString(key, c.getAttribute(key), properties);
            }

            cJ.add("properties", properties);
        }
    }

    private static Constituent readConstituent(JsonObject cJ, TextAnnotation ta, String viewName) {
        String label = readString("label", cJ);
        double score = 0;
        if (cJ.has("score"))
            score = readDouble("score", cJ);
        int start = readInt("start", cJ);
        int end = readInt("end", cJ);
        Constituent c = new Constituent(label, score, viewName, ta, start, end);

        if (cJ.has("properties")) {
            JsonObject properties = cJ.getAsJsonObject("properties");

            for (Entry<String, JsonElement> entry : properties.entrySet()) {
                c.addAttribute(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return c;
    }

    private static void writeSentences(TextAnnotation ta, JsonObject json) {

        JsonObject object = new JsonObject();

        SpanLabelView sentenceView = (SpanLabelView) ta.getView(ViewNames.SENTENCE);
        writeString("generator", sentenceView.getViewGenerator(), object);

        writeDouble("score", sentenceView.getScore(), object);
        int numSentences = sentenceView.getNumberOfConstituents();
        int[] sentenceEndPositions = new int[numSentences];

        int id = 0;
        for (Sentence sentence : ta.sentences()) {
            sentenceEndPositions[id++] = sentence.getEndSpan();
        }
        writeIntArray("sentenceEndPositions", sentenceEndPositions, object);

        json.add("sentences", object);

    }

    private static Pair<Pair<String, Double>, int[]> readSentences(JsonObject json) {
        JsonObject object = json.getAsJsonObject("sentences");

        String generator = readString("generator", object);
        double score = readDouble("score", object);
        int[] endPositions = readIntArray("sentenceEndPositions", object);

        return new Pair<Pair<String, Double>, int[]>(new Pair<String, Double>(generator, score),
                endPositions);

    }

    private static void writeIntArray(String name, int[] is, JsonObject object) {

        JsonArray array = new JsonArray();

        for (int i : is) {
            array.add(new JsonPrimitive(i));
        }

        object.add(name, array);
    }

    private static int[] readIntArray(String name, JsonObject object) {

        JsonArray array = object.get(name).getAsJsonArray();
        int[] s = new int[array.size()];

        for (int i = 0; i < array.size(); i++)
            s[i] = array.get(i).getAsInt();

        return s;
    }

    private static void writeStringArray(String name, String[] strings, JsonObject object) {

        JsonArray array = new JsonArray();

        for (String s : strings) {
            array.add(new JsonPrimitive(s));
        }

        object.add(name, array);
    }

    private static String[] readStringArray(String name, JsonObject object) {

        JsonArray array = object.get(name).getAsJsonArray();
        String[] s = new String[array.size()];

        for (int i = 0; i < array.size(); i++)
            s[i] = array.get(i).getAsString();
        return s;
    }

    private static void writeString(String name, String value, JsonObject out) {
        out.add(name, new JsonPrimitive(value));
    }

    private static String readString(String name, JsonObject obj) {
        return obj.getAsJsonPrimitive(name).getAsString();
        // return obj.get(name).getAsString();
    }

    private static void writeInt(String name, int value, JsonObject out) {
        out.add(name, new JsonPrimitive(value));
    }

    private static int readInt(String name, JsonObject obj) {
        return obj.get(name).getAsInt();
    }

    private static void writeDouble(String name, double value, JsonObject out) {
        out.add(name, new JsonPrimitive(value));
    }

    private static double readDouble(String name, JsonObject obj) {
        return obj.get(name).getAsDouble();
    }
    
    public static TextAnnotation deserialize(String json) throws Exception {
//    	return EdisonSerializationHelper.deserializeFromJson(json);
        AnnotationHelper deserializer = new AnnotationHelper();
        return deserializer.readTextAnnotation(json);
    }

    public static String serialize(TextAnnotation ta) {
        AnnotationHelper deserializer = new AnnotationHelper();
        JsonObject o = deserializer.writeTextAnnotation(ta);
//        return o.isJsonNull()?"{}":o.getAsString();
        return o.toString();
    }

    public static TextAnnotation newTextAnnotation(String id, String text) {
        return new TextAnnotation("semeval", id == null ? "" : id, text,
                SentenceViewGenerators.LBJSentenceViewGenerator);
    }
    
}
