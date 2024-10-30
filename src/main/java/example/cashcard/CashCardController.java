package example.cashcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.net.URI;

@RestController // configured to listen for and handle HTTP requests
@RequestMapping("/cashcards") // from url starting with
class CashCardController {
	// dependency injection (DI) framework
	@Autowired
	private final CashCardRepository cashCardRepository;

	public CashCardController(CashCardRepository cashCardRepository) {
		this.cashCardRepository = cashCardRepository;
	}
	
	// GET requests that match cashcards/{requestedID} will be handled by this
	// method
	@GetMapping("/{requestedId}")
//	private ResponseEntity<String> findById() {
	private ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
//	      return ResponseEntity.ok("{}");
//		if (requestedId.equals(99L)) {
//			CashCard cashCard = new CashCard(99L, 123.45);
//			return ResponseEntity.ok(cashCard);
//		} else {
//			return ResponseEntity.notFound().build();
//		}
		Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
		if (cashCardOptional.isPresent()) {
			return ResponseEntity.ok(cashCardOptional.get());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// POST requests that match cashcards will be handled by this method
	// RFC9110 the origin server SHOULD send a 201 (Created) response
	// containing a Location header field that provides an identifier for the
	// primary resource created
	@PostMapping
	private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb) {// ucb
																														// by
																														// Spring's
																														// IoC
																														// Container
		// it saves a new CashCard for us, and returns the saved object with a unique id
		// provided by the database.
		// insert cashcard
		CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
		// return id in headers HTTP
		URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
		return ResponseEntity.created(locationOfNewCashCard).build();
	}

	// GET requests that match cashcards will be handled by this method
	// LIST
	// return list of cashcards
	// @GetMapping()
	// private ResponseEntity<Iterable<CashCard>> findAll() {
	// return ResponseEntity.ok(cashCardRepository.findAll());
	// }
	// PAGINATION
	// return pagination to fetch them one at a time (page size of 1)
	// @GetMapping
	// private ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
	// //PageRequest is a basic Java Bean implementation of Pageable. Things that
	// want paging and sorting implementation
	// Page<CashCard> page = cashCardRepository.findAll(
	// PageRequest.of(
	// pageable.getPageNumber(),
	// pageable.getPageSize()
	// ));
	// return ResponseEntity.ok(page.getContent());
	// }
	// SORTING
	// return list of cashcards ordering their amounts sorted from highest to lowest
	@GetMapping
	private ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
		// PageRequest is a basic Java Bean implementation of Pageable. Things that want
		// paging and sorting implementation
		Page<CashCard> page = cashCardRepository
				.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
						// defined by client request
						// pageable.getSort()
						// defined y controller, page 0-20 by default
						pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))));
		return ResponseEntity.ok(page.getContent());
	}

	// PUT requests that match cashcards will be handled by this method
	@PutMapping("/{requestedId}")
	private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate) {
		Optional<CashCard> cashCard = cashCardRepository.findById(requestedId);
		if (cashCard.isPresent()) {
			CashCard updatedCashCard = new CashCard(cashCard.get().id(), cashCardUpdate.amount());
			cashCardRepository.save(updatedCashCard);
			// just return 204 NO CONTENT for now.
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// DELETE requests that match cashcards will be handled by this method
	@DeleteMapping("/{id}")
	private ResponseEntity<Void> deleteCashCard(@PathVariable Long id) {
		cashCardRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
