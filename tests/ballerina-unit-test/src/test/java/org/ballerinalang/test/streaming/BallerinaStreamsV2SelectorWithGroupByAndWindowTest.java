/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test external function call in select clause in Ballerina Streaming V2.
 *
 * @since 0.980.0
 */
public class BallerinaStreamsV2SelectorWithGroupByAndWindowTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-select-with-groupby-and-window-test.bal");
    }

    @Test(description = "Test filter streaming query")
    public void testSelectWithGroupByAndWindow() {
        BValue[] outputTeacherEvents = BRunUtil.invoke(result, "startAggregationWithGroupByQuery");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputTeacherEvents);
        Assert.assertEquals(outputTeacherEvents.length, 9, "Expected events are not received");

        BMap<String, BValue> teacher0 = (BMap<String, BValue>) outputTeacherEvents[0];
        BMap<String, BValue> teacher1 = (BMap<String, BValue>) outputTeacherEvents[1];
        BMap<String, BValue> teacher2 = (BMap<String, BValue>) outputTeacherEvents[2];
        BMap<String, BValue> teacher3 = (BMap<String, BValue>) outputTeacherEvents[3];
        BMap<String, BValue> teacher4 = (BMap<String, BValue>) outputTeacherEvents[4];
        BMap<String, BValue> teacher5 = (BMap<String, BValue>) outputTeacherEvents[5];
        BMap<String, BValue> teacher6 = (BMap<String, BValue>) outputTeacherEvents[6];
        BMap<String, BValue> teacher7 = (BMap<String, BValue>) outputTeacherEvents[7];
        BMap<String, BValue> teacher8 = (BMap<String, BValue>) outputTeacherEvents[8];


        Assert.assertEquals(teacher0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher0.get("sumAge")).intValue(), 30);
        Assert.assertEquals(((BInteger) teacher0.get("count")).intValue(), 1);

        Assert.assertEquals(teacher1.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher1.get("sumAge")).intValue(), 45);
        Assert.assertEquals(((BInteger) teacher1.get("count")).intValue(), 1);

        Assert.assertEquals(teacher2.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher2.get("sumAge")).intValue(), 60);
        Assert.assertEquals(((BInteger) teacher2.get("count")).intValue(), 2);

        Assert.assertEquals(teacher3.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher3.get("sumAge")).intValue(), 90);
        Assert.assertEquals(((BInteger) teacher3.get("count")).intValue(), 3);

        Assert.assertEquals(teacher4.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher4.get("sumAge")).intValue(), 120);
        Assert.assertEquals(((BInteger) teacher4.get("count")).intValue(), 4);

        Assert.assertEquals(teacher5.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher5.get("sumAge")).intValue(), 90);
        Assert.assertEquals(((BInteger) teacher5.get("count")).intValue(), 3);

        Assert.assertEquals(teacher6.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher6.get("sumAge")).intValue(), 90);
        Assert.assertEquals(((BInteger) teacher6.get("count")).intValue(), 2);

        Assert.assertEquals(teacher7.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher7.get("sumAge")).intValue(), 45);
        Assert.assertEquals(((BInteger) teacher7.get("count")).intValue(), 1);

        Assert.assertEquals(teacher8.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher8.get("sumAge")).intValue(), 120);
        Assert.assertEquals(((BInteger) teacher8.get("count")).intValue(), 4);
    }
}
