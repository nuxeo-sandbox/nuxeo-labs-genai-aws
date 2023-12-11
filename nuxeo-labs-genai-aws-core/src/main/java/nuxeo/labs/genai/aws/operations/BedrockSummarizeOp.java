package nuxeo.labs.genai.aws.operations;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

import nuxeo.labs.genai.aws.InvokeBedrock;
import nuxeo.labs.genai.aws.RequestParameters;
import software.amazon.awssdk.regions.Region;

/**
 *
 */
@Operation(id = BedrockSummarizeOp.ID, category = Constants.CAT_SERVICES, label = "Bedrock: Summarize", description = ""
        + "Summarize a blob or text, return a Blob (text/plain) with the summary."
        + " input can be void/null or a Document. If void/null, the text parameter is required and is the content to summarize."
        + " If input is a Document, then text is ignored and xpath is required and the blob found is the content to summarize."
        + " modelId is required. Default is anthropic.claude-instant-v1"
        + " IMPORTANT: We support only Anthropic Claude and AWS Titan for now."
        + " awsRegion is required. Default is us-east-1."
        + " language also is required, because it changes the prompt. We support only English ('en') and French ('fr')."
        + " For any other language, use Bedrock.Run"
        + " numberOfSentences, if passed, changes the prompt and asks for a summary in numberOfSentences sentences."
        + " If modelParams is passed, it is a JSON string containing an object with values for tuning the model: temperature (0-1), topP (0-1),"
        + " responseMaxTokenCount (integer), stopSequences (list of strings)."
        + " Notice this is the same a using the Bedrock.Run operation, we just provide the prompt, like 'Summarize the following text:',"
        + " or 'Merci de resumer ce texte en 3 phrases :'.")
public class BedrockSummarizeOp {

    public static final String ID = "Bedrock.Summarize";

    @Context
    protected CoreSession session;

    @Param(name = "modelId", required = true, values = { InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1 })
    protected String modelId = InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1;

    @Param(name = "awsRegion", required = true, values = { "us-east-1" })
    protected String awsRegion = "us-east-1";

    @Param(name = "text", required = false)
    protected String text;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath = "file:content";

    @Param(name = "numberOfSentences", required = false)
    protected Integer numberOfSentences;

    @Param(name = "language", required = true, values = { "en" })
    protected String language = "en";

    @Param(name = "modelParams", required = false)
    protected String modelParams;
    
    protected Blob runIt(Blob blob) {
        
        String prompt = "";
        int countSentences = 0;
        if (numberOfSentences != null && numberOfSentences.intValue() > 0) {
            countSentences = numberOfSentences.intValue();
        }
        
        switch (language.toLowerCase()) {
        case "fr":
            prompt = "Merci de résumer ce texte";
            if (countSentences > 0) {
                prompt += " en " + countSentences + " phrase";
                if (countSentences > 1) {
                    prompt += "s";
                }
            }
            break;

        default:
            prompt = "Please summarize this text";
            if (countSentences > 0) {
                prompt += " in " + countSentences + " sentence";
                if (countSentences > 1) {
                    prompt += "s";
                }
            }
            break;
        }
        prompt += " :\n\n";

        InvokeBedrock ibr = new InvokeBedrock(Region.of(awsRegion), modelId);

        if (modelParams != null) {
            RequestParameters params = new RequestParameters(modelParams);
            ibr.setParameters(params);
        }

        String summary = ibr.run(prompt, blob, null);

        return new StringBlob(summary, "text/plain");
    }

    @OperationMethod
    public Blob run() {

        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("When calling the " + ID + " operation with no input document, the 'text' parameter is required.");
        }
        
        Blob toSummarize = new StringBlob(text, "text/plain");
        return runIt(toSummarize);
        
    }
    
    @OperationMethod
    public Blob run(DocumentModel doc) {
        
        if (StringUtils.isBlank(xpath)) {
            throw new IllegalArgumentException("When calling the " + ID + " operation with an input document, the 'xpath' parameter is required.");
        }
        
        Blob toSummarize = (Blob) doc.getPropertyValue(xpath);
        return runIt(toSummarize);
        
    }
}
