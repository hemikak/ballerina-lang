/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test `where` clause behaviour in Ballerina Streaming V2.
 *
 * @since 0.980.0
 */
public class BallerinaStreamsV2HavingTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-having-test.bal");
    }

    @Test(description = "Test `having` clause within streaming query")
    public void testFilterQuery() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startFilterQuery");
        System.setProperty("enable.siddhiRuntime", "true");

        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee3 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee1.get("name").stringValue(), "Mohan");
        Assert.assertEquals(employee2.get("name").stringValue(), "Gimantha");
        Assert.assertEquals(employee3.get("name").stringValue(), "Grainier");
    }
}
