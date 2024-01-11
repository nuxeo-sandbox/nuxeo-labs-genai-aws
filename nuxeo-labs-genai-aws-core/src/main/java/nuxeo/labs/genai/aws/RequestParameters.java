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
package nuxeo.labs.genai.aws;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * RequestParameters helps normalize input/output when calling a model. Each model has its own naming for
 * properties/parameters (one has stopSequences, another stop_sequences, etc.). To be able to call different
 * models, use this class, then pass it to the a specialize model that implements {@code RequestResponseHandlers}.
 * <br>
 * See for example {@code AnthropicClaude} or {@code AWSTitan}
 * 
 * @since 2023
 */
public class RequestParameters {

    protected String prompt;

    public static final float DEFAULT_TEMPERATURE = 0.8F;

    // Use a lower value to decrease randomness in the response.
    protected Float temperature = DEFAULT_TEMPERATURE;

    // "Use a lower value to ignore less probable options. => 0-1"
    protected Float topP = null;

    // "Specify the maximum number of tokens in the generated response"
    // Titan: 0-8000, default 512
    // Claude: 0-4096, default 200. "We recommend a limit of 4,000 tokens for optimal performance"
    protected Integer responseMaxTokenCount = null;

    protected ArrayList<String> stopSequences = null;

    public RequestParameters(String prompt, Float temperature, Float topP, Integer responseMaxTokenCount,
            ArrayList<String> stopSequences) {
        super();

        setPrompt(prompt);
        setTemperature(temperature);
        setTopP(topP);
        setResponseMaxTokenCount(responseMaxTokenCount);
        setStopSequences(stopSequences);
    }

    /**
     * See {@code RequestParameters(JSONObject json)}
     * 
     * @param jsonString
     */
    public RequestParameters(String jsonString) {
        this(new JSONObject(jsonString));
    }

    /**
     * The properties of the json <b>must</b>, of course, match the variables: temperature, topP, responseMaxTokenCount
     * and stopSequences
     * 
     * @param json
     */
    public RequestParameters(JSONObject json) {

        setPrompt(json.optString("prompt", null));

        float temperature = json.optFloat("temperature", -1F);
        if (temperature == -1F) {
            setTemperature(null);
        } else {
            setTemperature(temperature);
        }

        float topP = json.optFloat("topP", -1F);
        if (topP == -1F) {
            setTopP(null);
        } else {
            setTopP(topP);
        }

        int responseMaxTokenCount = json.optInt("responseMaxTokenCount", -1);
        if (responseMaxTokenCount == -1) {
            setResponseMaxTokenCount(null);
        } else {
            setResponseMaxTokenCount(responseMaxTokenCount);
        }

        JSONArray sequences = json.optJSONArray("stopSequences");
        ArrayList<String> list = null;
        if (sequences != null) {
            list = new ArrayList<>();
            for (int i = 0; i < sequences.length(); i++) {
                list.add(sequences.getString(i));
            }
            setStopSequences(list);
        }
        setStopSequences(list);
    }

    public void setPrompt(String value) {
        prompt = value;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setTemperature(Float value) {
        if (value == null) {
            temperature = DEFAULT_TEMPERATURE;
        }
        temperature = value;
    }

    public float getTemperature() {
        if (temperature == null) {
            temperature = DEFAULT_TEMPERATURE;
        }
        return temperature;
    }

    public Float getTopP() {
        return topP;
    }

    // null => model will use a default value
    public void setTopP(Float value) {
        topP = value;
    }

    public Integer getResponseMaxTokenCount() {
        return responseMaxTokenCount;
    }

    // null => model will use a default value
    public void setResponseMaxTokenCount(Integer value) {
        responseMaxTokenCount = value;
    }

    public ArrayList<String> getStopSequences() {
        return stopSequences;
    }

    public void setStopSequences(ArrayList<String> values) {
        stopSequences = values;
    }

    public void setStopSequences(String... values) {
        if (values.length == 0) {
            stopSequences = null;
        } else {
            stopSequences = new ArrayList<>(Arrays.asList(values));
        }

    }

    public boolean hasStopSequences() {
        return stopSequences != null && stopSequences.size() > 0;
    }

}
