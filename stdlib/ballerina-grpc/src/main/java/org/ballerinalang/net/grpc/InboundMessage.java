/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.net.grpc;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_ENCODING;

/**
 * Class that represents an HTTP request in MSF4J level.
 */
public class InboundMessage {

    private final HTTPCarbonMessage httpCarbonMessage;

    public InboundMessage(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

    /**
     * @return true if the request does not have body content.
     */
    public boolean isEmpty() {
        return httpCarbonMessage.isEmpty();
    }

    /**
     * @return map of headers of the HTTP request.
     */
    public HttpHeaders getHeaders() {
        return httpCarbonMessage.getHeaders();
    }

    /**
     * Get an HTTP header of the HTTP request.
     *
     * @param key name of the header
     * @return value of the header
     */
    public String getHeader(String key) {
        return httpCarbonMessage.getHeader(key);
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key property key
     * @return value of the property key
     */
    public Object getProperty(String key) {
        return httpCarbonMessage.getProperty(key);
    }

    /**
     * @return property map of the underlining CarbonMessage.
     */
    public Map<String, Object> getProperties() {
        return httpCarbonMessage.getProperties();
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key   property key
     * @param value property value
     */
    public void setProperty(String key, Object value) {
        httpCarbonMessage.setProperty(key, value);
    }

    /**
     * Remove a property from the underlining CarbonMessage object.
     *
     * @param key property key
     */
    public void removeProperty(String key) {
        httpCarbonMessage.removeProperty(key);
    }

    /**
     * @return URL of the request.
     */
    public String getPath() {
        return (String) httpCarbonMessage.getProperty("TO");
    }

    public int getStatus() {
        return  (Integer) httpCarbonMessage.getProperty("HTTP_STATUS_CODE");
    }

    /**
     * @return HTTP method of the request.
     */
    public String getHttpMethod() {
        return (String) httpCarbonMessage
                .getProperty(org.wso2.transport.http.netty.common.Constants.HTTP_METHOD);
    }

    /**
     * Get underlying HTTPCarbonMessage.
     *
     * @return HTTPCarbonMessage instance of the InboundMessage
     */
    HTTPCarbonMessage getHttpCarbonMessage() {
        return httpCarbonMessage;
    }

    /**
     * Method use to send the response to the caller.
     *
     * @param carbonMessage OutboundMessage message
     * @return true if no errors found, else otherwise
     * @throws ServerConnectorException server connector exception.
     */
    public boolean respond(HTTPCarbonMessage carbonMessage) throws ServerConnectorException {
        HttpResponseFuture statusFuture = httpCarbonMessage.respond(carbonMessage);
        return statusFuture.getStatus().getCause() == null;
    }

    public Decompressor getMessageDecompressor() {

        String contentEncodingHeader = httpCarbonMessage.getHeader(MESSAGE_ENCODING);
        if (contentEncodingHeader != null) {
            httpCarbonMessage.removeHeader(HttpHeaderNames.CONTENT_ENCODING.toString());
            Decompressor decompressor = DecompressorRegistry.getDefaultInstance().lookupDecompressor
                    (contentEncodingHeader);
            if (decompressor != null) {
                return decompressor;
            }
        }
        return null;
    }

    /**
     * Inbound Message state listener.
     */
    public abstract static class InboundStateListener implements MessageDeframer.Listener {

        private MessageDeframer deframer;

        protected InboundStateListener(int maxMessageSize) {

            deframer = new MessageDeframer(
                    this,
                    Codec.Identity.NONE,
                    maxMessageSize);
        }

        final void setMaxInboundMessageSize(int maxSize) {

            deframer.setMaxInboundMessageSize(maxSize);
        }

        /**
         * Override this method to provide a stream listener.
         */
        protected abstract StreamListener listener();

        public void messagesAvailable(StreamListener.MessageProducer producer) {

            listener().messagesAvailable(producer);
        }

        /**
         * Closes the deframer and frees any resources. After this method is called, additional calls
         * will have no effect.
         * <p>
         * <p>When {@code stopDelivery} is false, the deframer will wait to close until any already
         * queued messages have been delivered.
         * <p>
         *
         * @param stopDelivery interrupt pending deliveries and close immediately
         */
        protected final void closeDeframer(boolean stopDelivery) {

            if (stopDelivery) {
                deframer.close();
            } else {
                deframer.closeWhenComplete();
            }
        }

        /**
         * Called to parse a received frame and attempt delivery of any completed messages. Must be
         * called from the transport thread.
         */
        protected final void deframe(final ReadableBuffer frame) {

            try {
                deframer.deframe(frame);
            } catch (Throwable t) {
                deframeFailed(t);
            }
        }

        /**
         * Called to request the given number of messages from the deframer. Must be called from the
         * transport thread.
         */
        public final void requestMessagesFromDeframer(final int numMessages) {

            try {
                deframer.request(numMessages);
            } catch (Throwable t) {
                deframeFailed(t);
            }
        }

        protected final void setDecompressor(Decompressor decompressor) {

            deframer.setDecompressor(decompressor);
        }

        public void deframeFailed(Throwable cause) {

            throw new UnsupportedOperationException("Currently not supported.");
        }

    }

}
