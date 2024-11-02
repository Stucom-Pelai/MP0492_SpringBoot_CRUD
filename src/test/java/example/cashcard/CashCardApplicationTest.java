package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import net.minidev.json.JSONArray;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //start our Spring Boot application and make it available for our test to perform requests
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) //Spring to start with a clean state, to clean up after creating a new Cash Card
class CashCardApplicationTest {
    @Autowired // inject HTTP requests to the locally running application.
    TestRestTemplate restTemplate;
    
    @Test
    //@DirtiesContext, clean after execution to method level
    void shouldCreateANewCashCard() {
    	// create new cashcard
       CashCard newCashCard = new CashCard(null, 250.00);
       
       //call rest POST endpoint
       ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
       // verify status code POST
       assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
       // verify id cashcard returned in headers HTTP
       URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
       
       //call rest GET endpoint to retrieve created cashcard
       ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
       // verify status code GET
       assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
       
       // add assertions for data
       DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
       Number id = documentContext.read("$.id");
       Double amount = documentContext.read("$.amount");
       assertThat(id).isNotNull();
       assertThat(amount).isEqualTo(250.00);
    }

    @Test
    void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isNotNull();
        assertThat(id).isEqualTo(99);
        
        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }
    
    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
      ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).isBlank();
    }
    
    @Test // verify list cashcards returned
    // test only status code
	//    void shouldReturnAllCashCardsWhenListIsRequested() {
	//        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
	//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	//    }
    //
    // test status code and items in any order
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
        // verify status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        // verify number of items, calculates the length of the array
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        // verify ids, retrieves the list of all id values returned
        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        // verify amount, collects all amounts returned.
        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
    }
    
    // verify pagination
    @Test
    // test to fetch them one at a time (page size of 1)
    void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }
    
    // verify sorting with page, field and direction
    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        // verify highest amount
        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }
    
    // verify sorting without parameters
    @Test
    void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
    }
    
    // verify update cashcard
    @Test
    @DirtiesContext
    void shouldUpdateAnExistingCashCard() {
    	// update request
        CashCard cashCardUpdate = new CashCard(null, 19.99);
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
        // we cannot use restTemplate.putForEntity()
        ResponseEntity<Void> response = restTemplate.exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // get request to verify update was done
        ResponseEntity<String> getResponse = restTemplate.getForEntity("/cashcards/99", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");
        assertThat(id).isEqualTo(99);
        assertThat(amount).isEqualTo(19.99);
    }
    
    // verify update non existent cashcard
    @Test
    @DirtiesContext
    void shouldUpdateANonExistingCashCard() {
    	// update request
        CashCard cashCardUpdate = new CashCard(null, 19.99);
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
        // we cannot use restTemplate.putForEntity()
        ResponseEntity<Void> response = restTemplate.exchange("/cashcards/999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);        
    }
    
    // verify delete cashcard
    @Test
    @DirtiesContext
    void shouldDeleteAnExistingCashCard() {
        ResponseEntity<Void> response = restTemplate.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Add the following code:
        ResponseEntity<String> getResponse = restTemplate.getForEntity("/cashcards/99", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    
    //verify delete non existent cashcard
    @Test
    void shouldNotDeleteACashCardThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate.exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    
    
    
    
    
    
    
    
}