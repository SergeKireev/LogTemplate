package io.logtemplate

import io.logtemplate.domain.template.{VLong, VString, Variable}
import io.logtemplate.state.DrainState
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class PostProcessingTest extends AnyFunSuite with Matchers {
  test("extracting variables") {
    val state = DrainState.deserialize("""{
               |    "lengthMap": {
               |        "6": {
               |            "children": {
               |                "User": {
               |                    "children": {
               |                        "<*>": {
               |                            "templates": {
               |                                "2ea4f76d-fed3-4489-9173-bd03b6b7e0bc": {
               |                                    "id": "2ea4f76d-fed3-4489-9173-bd03b6b7e0bc",
               |                                    "tokens": [
               |                                        {
               |                                            "FreeToken": {
               |                                                "s": "User"
               |                                            }
               |                                        },
               |                                        {
               |                                            "VariableToken": {
               |                                              "id": "2ea4f76e-fed3-4489-9173-bd03b6b7e0bc"
               |                                            }
               |                                        },
               |                                        {
               |                                            "FreeToken": {
               |                                                "s": "has"
               |                                            }
               |                                        },
               |                                        {
               |                                            "VariableToken": {
               |                                              "id": "2ea4f77e-fed3-4489-9173-bd03b6b7e0bc"
               |                                            }
               |                                        },
               |                                        {
               |                                            "FreeToken": {
               |                                                "s": "and"
               |                                            }
               |                                        },
               |                                        {
               |                                            "VariableToken": {
               |                                              "id": "2ea4f79e-fed3-4489-9173-bd03b6b7e0bc"
               |                                            }
               |                                        }
               |                                    ]
               |                                }
               |                            }
               |                        }
               |                    }
               |                }
               |            }
               |        }
               |    }
               |}""".stripMargin)
    val drain = new Drain(state)
    val newLog = "User 678 has connected and seen"
    val preprocessed = drain.preprocess(newLog)
    val (template, structured) = drain.process(preprocessed)
    val variables = drain.postProcess(template, structured)
    variables should contain theSameElementsInOrderAs(
      List(
        Variable(UUID.fromString("2ea4f76e-fed3-4489-9173-bd03b6b7e0bc"),"int2ea4f76e",VLong,"678"),
        Variable(UUID.fromString("2ea4f77e-fed3-4489-9173-bd03b6b7e0bc"),"string2ea4f77e",VString,"connected"),
        Variable(UUID.fromString("2ea4f79e-fed3-4489-9173-bd03b6b7e0bc"),"string2ea4f79e",VString,"seen"))
      )
    assert(drain.rehydrate(template, variables) === newLog)
  }
}
