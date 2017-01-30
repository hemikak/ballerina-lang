/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.builder;

import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.symbols.SymbolScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code CallableUnitGroupBuilder} builds Services and Connectors.
 * <p/>
 * A CallableUnitGroup represents a Service or a Connector
 *
 * @since 0.8.0
 */
class CallableUnitGroupBuilder implements SymbolScope {
    private NodeLocation location;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic;
    protected SymbolName symbolName;

    private List<Annotation> annotationList = new ArrayList<>();
    private List<Parameter> parameterList = new ArrayList<>();
    private List<ConnectorDcl> connectorDclList = new ArrayList<>();
    private List<VariableDef> variableDefList = new ArrayList<>();
    private List<Resource> resourceList = new ArrayList<>();
    private List<BallerinaAction> actionList = new ArrayList<>();

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap = new HashMap<>();

    CallableUnitGroupBuilder(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
    }


    public void setNodeLocation(NodeLocation location) {
        this.location = location;
    }

    void setName(String name) {
        this.name = name;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    void addAnnotation(Annotation annotation) {
        this.annotationList.add(annotation);
    }

    void addParameter(Parameter param) {
        this.parameterList.add(param);
    }

    void addConnectorDcl(ConnectorDcl connectorDcl) {
        this.connectorDclList.add(connectorDcl);
    }

    void addVariableDcl(VariableDef variableDef) {
        this.variableDefList.add(variableDef);
    }

    void addResource(Resource resource) {
        this.resourceList.add(resource);
    }

    void addAction(BallerinaAction action) {
        this.actionList.add(action);
    }

    @Override
    public String getScopeName() {
        return null;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public Symbol resolve(SymbolName name) {
        return null;
    }

    Service buildService() {
        return new Service(
                location,
                name,
                pkgPath,
                symbolName,
                annotationList.toArray(new Annotation[annotationList.size()]),
                connectorDclList.toArray(new ConnectorDcl[connectorDclList.size()]),
                variableDefList.toArray(new VariableDef[variableDefList.size()]),
                resourceList.toArray(new Resource[resourceList.size()]),
                enclosingScope,
                symbolMap);
    }

    BallerinaConnector buildConnector() {
        return new BallerinaConnector(
                location,
                name,
                pkgPath,
                isPublic,
                symbolName,
                annotationList.toArray(new Annotation[annotationList.size()]),
                parameterList.toArray(new Parameter[parameterList.size()]),
                connectorDclList.toArray(new ConnectorDcl[connectorDclList.size()]),
                variableDefList.toArray(new VariableDef[variableDefList.size()]),
                actionList.toArray(new BallerinaAction[actionList.size()]),
                enclosingScope,
                symbolMap);
    }
}
