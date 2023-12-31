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

import org.json.JSONObject;

import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

/**
 * Formatting the body of a request, and getting the result depending on the mode
 * 
 * @since 2023
 */
public interface RequestResponseHandlers {
    
    /**
     * 
     * @return a JSONObject, body to be send along with the @{code InvokeModelRequest}
     * @since 2023
     */
    public JSONObject getRequestBody(RequestParameters params);
    
    /**
     * 
     * @return a String, extracted from the @{code InvokeModelResponse}
     * @since 2023
     */
    public String getStringResult(InvokeModelResponse response);

}
