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

import org.json.JSONArray;
import org.json.JSONObject;

import nuxeo.labs.genai.aws.RequestParameters;
import nuxeo.labs.genai.aws.RequestResponseHandlers;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

/**
 * 
 * @since TODO
 */
public class AWSTitan implements RequestResponseHandlers {

    // See https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-titan-text.html
    @Override
    public JSONObject getRequestBody(RequestParameters params) {
        
        JSONObject jsonBody = new JSONObject();
        
        jsonBody.put("inputText", params.getPrompt());
        
        JSONObject textGenerationConfig = new JSONObject();
        textGenerationConfig.put("temperature", params.getTemperature());

        if (params.getTopP() != null) {
            jsonBody.put("topP", params.getTopP().floatValue());
        }

        if (params.getResponseMaxTokenCount() != null) {
            textGenerationConfig.put("maxTokenCount", params.getResponseMaxTokenCount().intValue());
        }
        
        if (params.hasStopSequences()) {
            textGenerationConfig.put("stopSequences", params.getStopSequences());
        }

        jsonBody.put("textGenerationConfig", textGenerationConfig);
        
        return jsonBody;
    }

    @Override
    public String getStringResult(InvokeModelResponse response) {
        
        JSONObject jsonResponse = new JSONObject(response.body().asString(StandardCharsets.UTF_8));
        
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
        // First implementation => assume there is at least one
        String result = results.getJSONObject(0).getString("outputText");
        
        return result;
    }

}
