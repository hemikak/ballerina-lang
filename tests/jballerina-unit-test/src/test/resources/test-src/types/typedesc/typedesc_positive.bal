function testBasicTypes() returns [typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>] {
    typedesc<int> a = int;
    typedesc<string> b = string;
    typedesc<float> c = float;
    typedesc<boolean> d = boolean;
    typedesc<byte> e = byte;
    return [a, b, c, d, e];
}

type mapOfAny map<any>;

type XmlType xml;

type Employee record {
    string name;
};

function testRefTypes(){
    typedesc<xml> a = XmlType;
    typedesc<json> b = json;
    typedesc<map<any>> c = mapOfAny;
    typedesc<table<Employee>> d = tableOfEmployee;

    [typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>] tupleValue = [a, b, c, d];

    assertEquality("typedesc xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>", tupleValue[0].toString());
    assertEquality("typedesc json", b.toString());
    assertEquality("typedesc map", c.toString());
    assertEquality("typedesc table<Employee>", d.toString());
}

class Pet { public string name = ""; }

function testObjectTypes() returns [typedesc<any>, typedesc<any>] {
    typedesc<Person> a = Person;
    typedesc<any> b = Pet;
    return [a,b];
}

class Person {
    public string name;

    function init(string name) {
        self.name = name;
    }

    public function getName() returns string {
        return self.name;
    }
}

type tableOfEmployee table<Employee>;

type intArray int[];
type intArrayArray int[][];

function testArrayTypes() returns [typedesc<any>, typedesc<any>] {
    typedesc<int[]> a = intArray;
    typedesc<int[][]> b = intArrayArray;
    return [a,b];
}

function testRecordTypes() returns [typedesc<any>, typedesc<any>] {
    typedesc<RecordA> a = RecordA;
    typedesc<any> b = RecordB;
    return [a,b];
}

type RecordA record {
    string a;
    int b;
};

type RecordB record {
    string c;
    int d;
};

type stringOrPerson [string, Person];

type intOrString int|string;

function testTupleUnionTypes() returns [typedesc<any>, typedesc<any>] {
    typedesc<any> a = stringOrPerson;
    typedesc<int|string> b = intOrString;
    return [a,b];
}

function testTuplesWithExpressions() returns typedesc<any> {
    int[] fib = [1, 1, 2, 3, 5, 8];
    typedesc<any> desc = typeof ["foo", 25, ["foo", "bar", "john"], utilFunc(), fib[4]];
    return desc;
}

function testAnyToTypedesc() returns typedesc<any>|error {
    any a = int;
    typedesc<int> desc = <typedesc<int>>a;
    return desc;
}

function utilFunc() returns string {
    return "util function";
}


typedesc<any> glbTypeDesc = json;

function testModuleLevelTypeDesc() returns typedesc<any> {
    return glbTypeDesc;
}

function testMethodLevelTypeDesc() returns typedesc<any> {
    typedesc<any> methodLocalTypeDesc = json;
    return methodLocalTypeDesc;
}

const FOO_REASON = "FooError";

type FooError distinct error;

function testCustomErrorTypeDesc() {
    typedesc<error> te = FooError;
    if (!(te is typedesc<FooError>)) {
        panic error("AssertionError", message = "expected typedesc<FooError> but found: " + te.toString());
    }
}

function testBasicTypesWithoutTypedescConstraint() {
    typedesc a = int;
    typedesc b = string;
    typedesc c = float;
    typedesc d = boolean;
    typedesc e = byte;

    assertEquality("typedesc int", a.toString());
}

function testRefTypesWithoutTypedescConstraint() {
    typedesc a = json;
    typedesc b = XmlType;

    assertEquality("typedesc json", a.toString());
}

function testObjectTypesWithoutTypedescConstraint() {
    typedesc a = Person;
    typedesc b = Pet;

    assertEquality("typedesc Person", a.toString());
}

function testArrayTypesWithoutTypedescConstraint() {
    typedesc a = intArray;
    typedesc b = intArrayArray;

    assertEquality("typedesc int[]", a.toString());
}

function testRecordTypesWithoutTypedescConstraint() {
    typedesc a = RecordA;
    typedesc b = RecordB;

    assertEquality("typedesc RecordA", a.toString());
}

function testTuplesWithExpressionsWithoutTypedescConstraint() {
    int[] fib = [1, 1, 2, 3, 5, 8];
    typedesc desc = typeof ["foo", 25, ["foo", "bar", "john"], utilFunc(), fib[4]];

    assertEquality("typedesc [string,int,[string,string,string],string,int]", desc.toString());
}

function testAnyToTypedescWithoutConstraint() {
    any a = int;
    typedesc desc = <typedesc>a;

    assertEquality("typedesc int", desc.toString());
}

typedesc glbTypeDescWithoutConstraint = json;

function testModuleLevelTypeDescWithoutConstraint() {

    assertEquality("typedesc json", glbTypeDescWithoutConstraint.toString());
}

function testMethodLevelTypeDescWithoutConstraint() {
    typedesc methodLocalTypeDesc = json;

    assertEquality("typedesc json", methodLocalTypeDesc.toString());
}

function testCustomErrorTypeDescWithoutConstraint() {
    typedesc te = FooError;

    assertEquality("typedesc FooError", te.toString());

}

type ImmutableIntArray int[] & readonly;

function testTypeDefWithIntersectionTypeDescAsTypedesc() {
    typedesc<anydata> a = ImmutableIntArray;
    (int|string)[] arr = [1, 2, 3];
    anydata|error b = arr.cloneWithType(a);
    // https://github.com/ballerina-platform/ballerina-lang/issues/28912
    //assertEquality(true, (typeof b).toString());
    //assertEquality(true, b is int[]);
    //assertEquality(true, (<int[]> checkpanic b).isReadOnly());
    //assertEquality(<int[]> [1, 2, 3], b);

    anydata|error c = arr.fromJsonWithType(ImmutableIntArray);
    // https://github.com/ballerina-platform/ballerina-lang/issues/28912
    //assertEquality(true, c is int[]);
    //assertEquality(true, (<int[]> checkpanic c).isReadOnly());
    //assertEquality(<int[]> [1, 2, 3], c);

    typedesc<readonly> d = ImmutableIntArray;
}

type Foo "foo";
type FooBar "foo"|"bar";

function testFiniteTypeAsTypedesc() {
    typedesc<string> a = Foo;
    typedesc<anydata> b = FooBar;

    string foo = "foo";
    string bar = "bar";
    string baz = "baz";

    var c = foo.cloneWithType(a);
    assertEquality(true, c is Foo);

    var d = baz.cloneWithType(a);
    assertEquality(true, d is error);

    var e = bar.fromJsonWithType(FooBar);
    assertEquality(false, e is Foo);
    assertEquality(true, e is FooBar);
}

type FunctionTypeOne function (int i) returns string;
type FunctionTypeTwo function () returns string;

function testTypeDefWithFunctionTypeDescAsTypedesc() {
    typedesc a = FunctionTypeOne;
    typedesc b = FunctionTypeTwo;

    string str = "hello";
    any c = function () returns string => str;
    typedesc d = typeof c;

    assertEquality(false, a.toString() == b.toString());
    assertEquality(true, b.toString() == d.toString());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                    message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
