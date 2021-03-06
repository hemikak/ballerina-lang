// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;

listener http:Listener backendEp = new (9109, {httpVersion: "2.0"});
http:Client clientEp = new ("http://localhost:9109", { httpVersion: "2.0", http2Settings: {
    http2PriorKnowledge: true }});

service initiator on new http:Listener(9108) {

    @http:ResourceConfig {
        path: "{svc}/{rsc}"
    }
    resource function echoResponse(http:Caller caller, http:Request request, string svc, string rsc) {
        var responseFromBackend = clientEp->forward("/" + <@untainted> svc + "/" + <@untainted> rsc, request);
        if (responseFromBackend is http:Response) {
            string trailerHeaderValue = responseFromBackend.getHeader("trailer");
            var textPayload = responseFromBackend.getTextPayload();
            string firstTrailer = responseFromBackend.getHeader("foo", position = "trailing");
            string secondTrailer = responseFromBackend.getHeader("baz", position = "trailing");

            int headerCount = responseFromBackend.getHeaderNames(position = "trailing").length();

            http:Response newResponse = new;
            newResponse.setJsonPayload({ foo: <@untainted> firstTrailer, baz: <@untainted> secondTrailer, count:
                                        <@untainted> headerCount });
            newResponse.setHeader("response-trailer", trailerHeaderValue);
            var resultSentToClient = caller->respond(<@untainted> newResponse);
        } else {
            var resultSentToClient = caller->respond("No response from backend");
        }
    }
}

service backend on backendEp {
    resource function echoResponseWithTrailer(http:Caller caller, http:Request request) {
        http:Response response = new;
        var textPayload = request.getTextPayload();
        string inPayload = textPayload is string ? textPayload : "error in accessing payload";
        response.setTextPayload(<@untainted> inPayload);
        response.setHeader("foo", "Trailer for echo payload", position = "trailing");
        response.setHeader("baz", "The second trailer", position = "trailing");
        var result = caller->respond(response);
    }

    resource function responseEmptyPayloadWithTrailer(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setTextPayload("");
        response.setHeader("foo", "Trailer for empty payload", position = "trailing");
        response.setHeader("baz", "The second trailer for empty payload", position = "trailing");
        var result = caller->respond(response);
    }
}

service passthroughsvc on backendEp {
    resource function forward(http:Caller caller, http:Request request) {
        var responseFromBackend = clientEp->forward("/backend/echoResponseWithTrailer", request);
        if (responseFromBackend is http:Response) {
            var resultSentToClient = caller->respond(<@untainted> responseFromBackend);
        } else {
            var resultSentToClient = caller->respond("No response from backend");
        }
    }

    resource function buildPayload(http:Caller caller, http:Request request) {
        var responseFromBackend = clientEp->forward("/backend/echoResponseWithTrailer", request);
        if (responseFromBackend is http:Response) {
            var textPayload = responseFromBackend.getTextPayload();
            responseFromBackend.setHeader("baz", "this trailer will get replaced", position = "trailing");
            responseFromBackend.setHeader("barr", "this is a new trailer", position = "trailing");
            var resultSentToClient = caller->respond(<@untainted> responseFromBackend);
        } else {
            var resultSentToClient = caller->respond("No response from backend");
        }
    }
}
