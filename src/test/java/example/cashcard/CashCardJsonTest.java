//package example.cashcard;
//
//import org.junit.jupiter.api.Test;
//import static org.assertj.core.api.Assertions.assertThat;
//
//class CashCardJsonTest {
//
//   @Test
//   void myFirstTest() {
//      assertThat(1).isEqualTo(42);
//   }
//}
package example.cashcard;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest // Jackson framework to test
class CashCardJsonTest {

    @Autowired // directs Spring to create an object of the requested type.
    private JacksonTester<CashCard> json; 

    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    private CashCard[] cashCards;

    @BeforeEach
    void setUp() {
        cashCards = Arrays.array(
                new CashCard(99L, 123.45),
                new CashCard(100L, 1.00),
                new CashCard(101L, 150.00));
    }

    @Test // verify serialization, from java to json
    void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45);
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("expected.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount")
             .isEqualTo(123.45);
    }
    
    @Test // verify deserialization, from json to java
    void cashCardDeserializationTest() throws IOException {
       String expected = """
               {
                   "id":99,
                   "amount":123.45
               }
               """;
       assertThat(json.parse(expected))
               .isEqualTo(new CashCard(99L, 123.45));
       assertThat(json.parseObject(expected).id()).isEqualTo(99);
       assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }
    
    @Test //asserts that list.json should contain the same data as the serialized cashCards variable.
    void cashCardListSerializationTest() throws IOException {
       assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }
    
    @Test //asserts expected array should contain the same data as the deserialized jsonlist
    void cashCardListDeserializationTest() throws IOException {
       String expected="""
             [
                { "id": 99, "amount": 123.45 },
                { "id": 100, "amount": 1.00 },
                { "id": 101, "amount": 150.00 }
             ]
             """;
       assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }    
    
}
