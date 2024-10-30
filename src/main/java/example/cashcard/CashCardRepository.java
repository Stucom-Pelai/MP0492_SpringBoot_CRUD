package example.cashcard;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

// CrudRepository is an interface supplied by Spring Data. When we extend it 
// Spring Boot and Spring Data work together to automatically generate the CRUD methods that we need to interact with a database.
//  it manages the CashCard's data and CashCard ID is a Long
//interface CashCardRepository extends CrudRepository<CashCard, Long> {
//}
interface CashCardRepository extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {
}