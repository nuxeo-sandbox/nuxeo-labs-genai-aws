package nuxeo.labs.genai.aws;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nuxeo.ecm.core.api.Blob;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

public class InvokeBedrock {

    private static final Logger log = LogManager.getLogger(InvokeBedrock.class);

    public static final Region DEFAULT_REGION = Region.US_EAST_1;

    protected Region region = DEFAULT_REGION;

    // Reminder: as of today (end 2023), Titan is EN only:
    // "Sorry, this model is only accessible for English only applications. Please consider revising your content to be
    // in English."
    public static final String MODEL_TITAN_TEXT_EXPRESS_V1 = "amazon.titan-text-express-v1";

    public static final String MODEL_ANTHROPIC_CLAUDE_INSTANT_V1 = "anthropic.claude-instant-v1";

    public static final String MODEL_ANTHROPIC_CLAUDE_V2 = "anthropic.claude-v2";

    public static final String DEFAULT_MODEL = MODEL_ANTHROPIC_CLAUDE_INSTANT_V1;// MODEL_TITAN_TEXT_EXPRESS_V1;// ;

    protected String modelId = DEFAULT_MODEL;

    protected String prompt;

    protected Blob sourceBlob;

    public static final float DEFAULT_TEMPERATURE = 0.8F;

    protected float temperature = DEFAULT_TEMPERATURE;

    BedrockRuntimeClient bedrockRuntime = null;

    public InvokeBedrock() {
        getBedrockRuntime();
    }

    public InvokeBedrock(Region region, String modelId) {
        getBedrockRuntime();
        setRegion(region);
        setModelId(modelId);
    }

    protected void getBedrockRuntime() {
        if (bedrockRuntime == null) {
            bedrockRuntime = BedrockRuntimeClient.builder().region(region).build();
        }
    }

    public void setRegion(Region region) {
        if (region != null) {
            this.region = region;
        } else {
            log.warn("region is null => not changing it, using " + this.region);
        }
    }

    public void setModelId(String modelID) {
        if (StringUtils.isNotBlank(modelId)) {
            this.modelId = modelID;
        } else {
            log.warn("modelID is blank => not changing it, using " + this.modelId);
        }
    }

    public void setTemperature(float value) {
        temperature = value;
    }

    public String run(String prompt, Blob blob) {

        this.prompt = prompt;

        JSONObject jsonBody = getRequestBodyForModel();

        SdkBytes body = SdkBytes.fromUtf8String(jsonBody.toString());

        System.out.println("INVOKING MODEL " + modelId);
        InvokeModelRequest request = InvokeModelRequest.builder().modelId(modelId).body(body).build();

        InvokeModelResponse response = bedrockRuntime.invokeModel(request);

        String result = getStringResultForModel(response);

        return result;
    }

    protected JSONObject getRequestBodyForModel() {
        JSONObject jsonBody = new JSONObject();

        switch (modelId) {
        case MODEL_TITAN_TEXT_EXPRESS_V1:
            // see https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-titan-text.html
            jsonBody.put("inputText", prompt);
            JSONObject textGenerationConfig = new JSONObject();
            textGenerationConfig.put("temperature", temperature);
            // .put("topP", ...)
            // .put("maxTokenCount", ...)
            // .put("stopSequences", ...)
            jsonBody.put("textGenerationConfig", textGenerationConfig);
            break;

        case MODEL_ANTHROPIC_CLAUDE_INSTANT_V1:
        case MODEL_ANTHROPIC_CLAUDE_V2:
            // See https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-claude.html
            jsonBody.put("prompt", "Human: " + prompt + " Assistant:")
                    .put("temperature", temperature)
                    .put("max_tokens_to_sample", 1024);
            break;

        default:
            throw new IllegalArgumentException("Model shoud be Titan or Anthropic-Claude. It is " + modelId);
        }

        return jsonBody;

    }

    protected String getStringResultForModel(InvokeModelResponse response) {
        String result = null;

        JSONObject jsonResponse = new JSONObject(response.body().asString(StandardCharsets.UTF_8));

        switch (modelId) {
        case MODEL_TITAN_TEXT_EXPRESS_V1:
            // {
            // "inputTextTokenCount": 11,
            // "results": [
            // {
            // "tokenCount": 31,
            // "outputText": "\nThe distance between the Earth and the Moon is approximately 238,900 mi or 384,400 km.",
            // "completionReason": "FINISH"
            // }
            // ]
            // }
            JSONArray results = jsonResponse.getJSONArray("results");
            // check...
            // First implementaiton => assume there is at least one
            result = results.getJSONObject(0).getString("outputText");
            break;

        case MODEL_ANTHROPIC_CLAUDE_INSTANT_V1:
        case MODEL_ANTHROPIC_CLAUDE_V2:
            result = jsonResponse.getString("completion");
            break;

        default:
            throw new IllegalArgumentException("Model shoud be Titan or Anthropic-Claude. It is " + modelId);
        }

        return result;
    }

    /*
     * public static void main(String[] args) {
     * BedrockRuntimeClient runtime = BedrockRuntimeClient.builder().region(Region.US_EAST_1).build();
     * String prompt = "Hello Claude, how are you?";
     * JSONObject jsonBody = new JSONObject().put("prompt", "Human: " + prompt + " Assistant:")
     * .put("temperature", 0.8)
     * .put("max_tokens_to_sample", 1024);
     * SdkBytes body = SdkBytes.fromUtf8String(jsonBody.toString());
     * InvokeModelRequest request = InvokeModelRequest.builder().modelId("anthropic.claude-v2").body(body).build();
     * InvokeModelResponse response = runtime.invokeModel(request);
     * JSONObject jsonObject = new JSONObject(response.body().asString(StandardCharsets.UTF_8));
     * String completion = jsonObject.getString("completion");
     * System.out.println();
     * System.out.println(completion);
     * System.out.println();
     * }
     */
}
