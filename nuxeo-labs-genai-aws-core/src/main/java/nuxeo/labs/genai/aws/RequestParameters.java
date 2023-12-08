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

/**
 * @since TODO
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

        this.prompt = prompt;
        setTemperature(temperature);
        setTopP(topP);
        setResponseMaxTokenCount(responseMaxTokenCount);
        setStopSequences(stopSequences);
    }

    public void setTemperature(Float value) {
        if(value == null) {
            temperature = DEFAULT_TEMPERATURE;
        }
        temperature = value;
    }

    // null => model will use a default value
    public void setTopP(Float value) {
        topP = value;
    }

    public String getPrompt() {
        return prompt;
    }

    public float getTemperature() {
        if(temperature == null) {
            temperature = DEFAULT_TEMPERATURE;
        }
        return temperature;
    }

    public Float getTopP() {
        return topP;
    }

    public Integer getResponseMaxTokenCount() {
        return responseMaxTokenCount;
    }

    public ArrayList<String> getStopSequences() {
        return stopSequences;
    }

    // null => model will use a default value
    public void setResponseMaxTokenCount(Integer value) {
        responseMaxTokenCount = value;
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
