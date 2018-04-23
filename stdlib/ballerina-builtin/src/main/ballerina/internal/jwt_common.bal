// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

//JOSH header parameters
@final string ALG = "alg";
@final string TYP = "typ";
@final string CTY = "cty";
@final string KID = "kid";

//Payload parameters
@final string ISS = "iss";
@final string SUB = "sub";
@final string AUD = "aud";
@final string JTI = "jti";
@final string EXP = "exp";
@final string NBF = "nbf";
@final string IAT = "iat";

documentation {
    Represents a JWT header.
}
public type JwtHeader {
    string alg;
    string typ;
    string cty;
    string kid;
    map customClaims;
};

documentation {
    Represents a JWT payload.
}
public type JwtPayload {
    string iss;
    string sub;
    string[] aud;
    string jti;
    int exp;
    int nbf;
    int iat;
    map customClaims;
};
