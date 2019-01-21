/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.codelenses;

import com.google.gson.JsonParser;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test code lens feature in language server.
 */
public class CodeLensTest {
    private Path servicesBalPath = FileUtils.RES_DIR.resolve("codelens").resolve("services.bal");
    private Path functionsBalPath = FileUtils.RES_DIR.resolve("codelens").resolve("functions.bal");
    private Endpoint serviceEndpoint;
    private JsonParser parser = new JsonParser();
    private static final Logger log = LoggerFactory.getLogger(CodeLensTest.class);

    @BeforeClass
    public void loadLangServer() throws IOException {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, servicesBalPath);
        TestUtil.openDocument(serviceEndpoint, functionsBalPath);
    }

    @BeforeMethod
    public void clearPackageCache() {
        LSContextManager.getInstance().clearAllContexts();
    }

    @Test(description = "Test Code Lenses for functions", dataProvider = "codeLensFunctionPositions")
    public void codeLensForBuiltInFunctionTest(String expectedFile) throws IOException {
        String response = TestUtil.getCodeLensesResponse(servicesBalPath.toString(), serviceEndpoint);
        String expected = getExpectedValue(expectedFile);

        Assert.assertEquals(parser.parse(response).getAsJsonObject(), parser.parse(expected).getAsJsonObject(),
                            "Did not match the codelens content for " + expectedFile);
    }

    @Test(description = "Test Code Lenses for services", dataProvider = "codeLensServicesPositions")
    public void codeLensForCurrentPackageFunctionTest(String expectedFile) throws IOException {
        String response = TestUtil.getCodeLensesResponse(servicesBalPath.toString(), serviceEndpoint);
        String expected = getExpectedValue(expectedFile);

        Assert.assertEquals(parser.parse(response).getAsJsonObject(), parser.parse(expected).getAsJsonObject(),
                            "Did not match the codelens content for " + expectedFile);
    }

    @AfterClass
    public void shutDownLanguageServer() {
        TestUtil.closeDocument(this.serviceEndpoint, servicesBalPath);
        TestUtil.closeDocument(this.serviceEndpoint, functionsBalPath);
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "codeLensFunctionPositions")
    public Object[][] getCodeLensFunctionPositions() {
        log.info("Test textDocument/codeLens for functions");
        return new Object[][]{
                {"functions.json"},
        };
    }

    @DataProvider(name = "codeLensServicesPositions")
    public Object[][] getCodeLensServicesPositions() {
        log.info("Test textDocument/codeLens for services");
        return new Object[][]{
                {"services.json"}
        };
    }

    /**
     * Get the expected value from the expected file.
     *
     * @param expectedFile json file which contains expected content.
     * @return string content read from the json file.
     */
    private String getExpectedValue(String expectedFile) throws IOException {
        Path expectedFilePath = FileUtils.RES_DIR.resolve("codelens").resolve("expected").resolve(expectedFile);
        byte[] expectedByte = Files.readAllBytes(expectedFilePath);
        return new String(expectedByte);
    }
}
