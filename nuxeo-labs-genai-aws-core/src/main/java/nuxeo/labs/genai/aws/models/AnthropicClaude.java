/*
 * (C) Copyright 2023 Hyland (http://hyland.com/)  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package nuxeo.labs.genai.aws.models;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import nuxeo.labs.genai.aws.RequestParameters;
import nuxeo.labs.genai.aws.RequestResponseHandlers;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

/**
 * @since TODO
 */
public class AnthropicClaude implements RequestResponseHandlers {

    // See https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-claude.html
    @Override
    public JSONObject getRequestBody(RequestParameters params) {

        String modifiedPrompt;
        JSONObject jsonBody = new JSONObject();

        modifiedPrompt = "";

        if (params.getPrompt().indexOf("\n\n") != 0) {
            modifiedPrompt = "\n\n";
        }
        modifiedPrompt += params.getPrompt();
        jsonBody.put("prompt", "Human: " + modifiedPrompt + "\n\nAssistant: ")
                .put("temperature", params.getTemperature());

        if (params.getTopP() != null) {
            jsonBody.put("top_p", params.getTopP().floatValue());
        }

        int responseMaxTokenCount;
        if (params.getResponseMaxTokenCount() == null) {
            responseMaxTokenCount = 1024;
        } else {
            responseMaxTokenCount = params.getResponseMaxTokenCount().intValue();
        }
        jsonBody.put("max_tokens_to_sample", responseMaxTokenCount);

        if (params.hasStopSequences()) {
            jsonBody.put("stop_sequences", params.getStopSequences());
        }

        return jsonBody;
    }

    @Override
    public String getStringResult(InvokeModelResponse response) {
        
        JSONObject jsonResponse = new JSONObject(response.body().asString(StandardCharsets.UTF_8));
        String result = jsonResponse.getString("completion");
        
        return result;
    }

}
