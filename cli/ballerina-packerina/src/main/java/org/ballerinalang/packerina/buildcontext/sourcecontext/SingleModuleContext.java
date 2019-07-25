/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.buildcontext.sourcecontext;

import org.wso2.ballerinalang.compiler.tree.BLangPackage;

/**
 * Dataholder for a single module compilation.
 */
public class SingleModuleContext {
    private String moduleName;
    private BLangPackage BLangModule;
    
    public SingleModuleContext(String moduleName) {
        this.moduleName = this.moduleName;
    }
    
    public String getModuleName() {
        return moduleName;
    }
    
    public BLangPackage getBLangModule() {
        return BLangModule;
    }
    
    public void setBLangModule(BLangPackage BLangModule) {
        this.BLangModule = BLangModule;
    }
}
