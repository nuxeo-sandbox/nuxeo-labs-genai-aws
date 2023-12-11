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
@Operation(id = BedrockRunOp.ID, category = Constants.CAT_SERVICES, label = "Bedrock: Run", description = ""
        + "Run a model, return the response as Blob (text/plain)."
        + " The prompt is required and is sent as is to the model."
        + " modelId is required. Default is anthropic.claude-instant-v1. IMPORTANT: We support only Anthropic Claude and AWS Titan for now."
        + " awsRegion is required. Default is us-)east-1."
        + " If input is passed and xpath is passed and contains a blob, it is converted to text and added to the prompt,"
        + " with 2 lines before and after. Also, if a blob is used, it can be instertedn in the prompt by replacing the"
        + " text set in insertInPromptReplaceTag (which is optional)"
        + " If input is void/null, the operationwill just send the prompt and return the result."
        + " modelId and awsRegion are required."
        + " If modelParams is passed, it is a JSON string containing values for tuning the model: temperature (0-1), topP (0-1),"
        + " responseMaxTokenCount (integer), stopSequences (list of strings).")
public class BedrockRunOp {

    public static final String ID = "Bedrock.Run";

    @Context
    protected CoreSession session;

    @Param(name = "modelId", required = true, values = { InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1 })
    protected String modelId = InvokeBedrock.MODEL_ANTHROPIC_CLAUDE_INSTANT_V1;

    @Param(name = "awsRegion", required = true, values = { "us-east-1" })
    protected String awsRegion = "us-east-1";

    @Param(name = "prompt", required = true)
    protected String prompt;

    @Param(name = "xpath", required = false)
    protected String xpath;

    @Param(name = "insertInPromptReplaceTag", required = false)
    protected String insertInPromptReplaceTag;

    @Param(name = "modelParams", required = false)
    protected String modelParams;

    protected Blob runIt(Blob blob) {

        InvokeBedrock ibr = new InvokeBedrock(Region.of(awsRegion), modelId);
        if (modelParams != null) {
            RequestParameters params = new RequestParameters(modelParams);
            ibr.setParameters(params);
        }

        String result = ibr.run(prompt, blob, null);

        return new StringBlob(result, "text/plain");
    }

    @OperationMethod
    public Blob run() {

        return runIt(null);
    }

    @OperationMethod
    public Blob run(DocumentModel doc) {

        Blob blob = null;
        if (StringUtils.isNotBlank(xpath)) {
            blob = (Blob) doc.getPropertyValue(xpath);
        }

        return runIt(blob);
    }
}
