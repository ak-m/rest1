package com.akpoc.sbdemos.rest1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class Rest1Application implements CommandLineRunner{
	
	@Autowired
	EmployeeRepo empRep;

	public static void main(String[] args) {
		SpringApplication.run(Rest1Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.debug("$$$$ Initializing Employeed DB");
		List<Employee> emps = new ArrayList<>();
		emps.add(new Employee(1, "fname1", "laname1", 13, 1000.00));
		emps.add(new Employee(2, "fname2", "laname2", 23, 2000.00));
		emps.add(new Employee(3, "fname3", "laname3", 33, 3000.00));
		emps.add(new Employee(4, "fname4", "laname4", 43, 4000.00));
		emps.add(new Employee(5, "fname5", "laname5", 53, 5000.00));
		
		log.debug("Saving employees {}", emps);
		emps.stream().forEach(e -> empRep.save(e));
		
		log.debug("Loading saved employees");
		empRep.findAll().forEach(System.out::println);
	}

}

@ControllerAdvice
@Slf4j
class GlobalExceptionHandlerAdvice {
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<HttpStatus> handleException(Exception e) {
		log.error("processing handleException()", e.getMessage());
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(EmployeeNotFoundException.class)
	public ResponseEntity<HttpStatus> handleEmployeeNotFoundException(EmployeeNotFoundException enfe) {
		log.error("processing handleEmployeeNotFoundException()", enfe.getMessage());
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(BadEmployeeRequestException.class)
	public ResponseEntity<HttpStatus> handleBadEmployeeRequest(BadEmployeeRequestException enfe) {
		log.error("processing handleBadEmployeeRequest()", enfe.getMessage());
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
}


@RestController
@Slf4j
class HostInfoController {
	
	@GetMapping("/hostinfo")
	public String getHostInfo() throws UnknownHostException {
		log.debug("getHostInfo()");
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm:ss a");
		String serverTime = ofPattern.format(LocalDateTime.now());
		InetAddress localHost = InetAddress.getLocalHost();
		String serverResponse = new StringBuilder().append("IP ").append(localHost.getHostAddress())
							.append("Host :").append(localHost.getHostName())
							.append("@ ").append(serverTime)
							.toString();
		log.debug("Server resposne {} ", serverResponse);
		
		return serverResponse;
	}
}

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Entity
@Table(name = "employees")
class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long empId;
	private String fname;
	private String lname;
	private int age;
	private double salary;
	
}

@Service
interface IEmployeeService {

	List<Employee> findEmployees();

	Optional<Employee> findEmpById(long id);

	Employee addEmployee(Employee newEmp);

	void deleteEmployee(long id);

	Employee updateEmployee(Employee updatedEmp);
}

@Slf4j
@Service
class EmployeeServiceInMemory implements IEmployeeService {
	
	private static ArrayList<Employee> emps = new ArrayList<>();
	
	static {
		emps.add(new Employee(1, "fname1", "laname1", 13, 1000.00));
		emps.add(new Employee(2, "fname2", "laname2", 23, 2000.00));
		emps.add(new Employee(3, "fname3", "laname3", 33, 3000.00));
		emps.add(new Employee(4, "fname4", "laname4", 43, 4000.00));
		emps.add(new Employee(5, "fname5", "laname5", 53, 5000.00));
	}
	@Override
	public  List<Employee> findEmployees(){
		return emps;
	}
	
	@Override
	public  Optional<Employee> findEmpById(long id) {
		return emps.stream()
				.filter(e -> e.getEmpId() == id)
				.findAny();
	}

	@Override
	public  Employee addEmployee(Employee newEmp) {
		long empId = emps.stream().max(Comparator.comparing(Employee::getEmpId)).get().getEmpId();
		newEmp.setEmpId(empId !=0? empId + 1:1);
		emps.add(newEmp);
		return newEmp;
	}

	@Override
	public  void deleteEmployee(long id) {
		 emps.removeIf(e -> e.getEmpId() == id);
	}

	@Override
	public Employee updateEmployee(Employee updatedEmp) {
		Optional<Employee> extEmp = emps.stream().filter(e -> e.getEmpId() == updatedEmp.getEmpId()).findAny();
		Employee inMemEmp = extEmp.get();
		inMemEmp.setAge(updatedEmp.getAge());
		inMemEmp.setFname(updatedEmp.getFname());
		inMemEmp.setLname(updatedEmp.getLname());
		inMemEmp.setSalary(updatedEmp.getSalary());
		return inMemEmp;
	}
	
	
}

class  BadEmployeeRequestException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BadEmployeeRequestException() {
		super();
	}

	public BadEmployeeRequestException(String message) {
		super(message);
	}

	public BadEmployeeRequestException(Throwable cause) {
		super(cause);
	}
}

class EmployeeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmployeeNotFoundException() {
		super();
	}

	public EmployeeNotFoundException(String message) {
		super(message);
	}

	public EmployeeNotFoundException(Throwable cause) {
		super(cause);
	}
	
}

@Repository
interface EmployeeRepo extends JpaRepository<Employee, Long> {
	
}


@Slf4j
@RestController
@RequestMapping(path = "api/v3/employees")
class EmloyeeRestControllerWithRepo{
	private static final AtomicInteger reqCount = new AtomicInteger(1);

	@Autowired
	EmployeeRepo empRepo;
	
	public EmloyeeRestControllerWithRepo() {
		log.debug("#### {} created ", EmloyeeRestControllerWithRepo.class.getSimpleName());
	}
	
	@GetMapping
	public ResponseEntity<List<Employee>> getAllEmployees() {
		log.debug("Got request# {} for getAllEmployees ", reqCount.getAndIncrement());
		 List<Employee> allEmps = empRepo.findAll();
		 return new ResponseEntity(allEmps, HttpStatus.OK);
	}
	
	@GetMapping(path="/{id}")
	public ResponseEntity<Employee> getEmpById(@PathVariable long id) {
		log.debug("Got request# {} for getEmpById ", reqCount.getAndIncrement());
		Optional<Employee> empOpt = empRepo.findById(id);
		if(empOpt.isPresent()) {
			log.debug("Found emp {}", empOpt.get());
		}else {
			throw new EmployeeNotFoundException("404 Employee Not Found for id " + Long.toString(id));
		}
		
		return new ResponseEntity<>(empOpt.get(), HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<Employee> addEmployee(@RequestBody Employee newEmp) {
		log.debug("Got request# {} for addEmployee ", reqCount.getAndIncrement(), newEmp);
		
		if(newEmp.getEmpId()!=0) {
			throw new BadEmployeeRequestException("Cannot set emp id when creating");
		}
		empRepo.save(newEmp);
		return new ResponseEntity<>(newEmp, HttpStatus.CREATED);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable long id) {
		log.debug("Got request# {} for deleteEmployee ", reqCount.getAndIncrement(), id);
		empRepo.findById(id)
		.orElseThrow(() -> new EmployeeNotFoundException("404 Employee Not Found for id "));
		empRepo.deleteById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee updatedEmp ) {
		log.debug("Got request# {} for updateEmployee with id {}", reqCount.getAndIncrement(), updatedEmp, id);
		
		if(id != updatedEmp.getEmpId()) {
			throw new BadEmployeeRequestException("id path value & in emp body not same!");
		}
		
		empRepo.findById(id)
			.orElseThrow(() -> new EmployeeNotFoundException("404 Employee Not Found for id " + Long.toString(id)));
		
		Employee updateEmployee = empRepo.save(updatedEmp);
		log.debug("Found emp to update to {}", updateEmployee );
		return new ResponseEntity<>(updatedEmp, HttpStatus.OK);

	}
	
}


@Slf4j
@RestController
@RequestMapping(path = "api/v2/employees")
class EmployeeRestControllerWithResponseEntity{
	
	private static final AtomicInteger reqCount = new AtomicInteger(1);
	
	@Autowired
	IEmployeeService empSevice;

	public EmployeeRestControllerWithResponseEntity() {
		log.debug("#### {} created ", EmployeeRestControllerWithResponseEntity.class.getSimpleName());
	}
	
	@GetMapping
	public ResponseEntity<List<Employee>> getAllEmployees() {
		log.debug("Got request# {} for getAllEmployees ", reqCount.getAndIncrement());
		 List<Employee> allEmps = empSevice.findEmployees();
		 return new ResponseEntity(allEmps, HttpStatus.OK);
	}
	
	@GetMapping(path="/{id}")
	public ResponseEntity<Employee> getEmpById(@PathVariable long id) {
		log.debug("Got request# {} for getEmpById ", reqCount.getAndIncrement());
		Optional<Employee> empOpt = empSevice.findEmpById(id);
		if(empOpt.isPresent()) {
			log.debug("Found emp {}", empOpt.get());
		}else {
			throw new EmployeeNotFoundException("404 Employee Not Found for id " + Long.toString(id));
		}
		
		return new ResponseEntity<>(empOpt.get(), HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<Employee> addEmployee(@RequestBody Employee newEmp) {
		log.debug("Got request# {} for addEmployee ", reqCount.getAndIncrement(), newEmp);
		empSevice.addEmployee(newEmp);
		return new ResponseEntity<>(newEmp, HttpStatus.CREATED);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable long id) {
		log.debug("Got request# {} for deleteEmployee ", reqCount.getAndIncrement(), id);
		empSevice.findEmpById(id)
		.orElseThrow(() -> new EmployeeNotFoundException("404 Employee Not Found for id "));
		empSevice.deleteEmployee(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee updatedEmp ) {
		log.debug("Got request# {} for updateEmployee with id {}", reqCount.getAndIncrement(), updatedEmp, id);
		empSevice.findEmpById(id)
			.orElseThrow(() -> new EmployeeNotFoundException("404 Employee Not Found for id " + Long.toString(id)));
		
		Employee updateEmployee = empSevice.updateEmployee(updatedEmp);
		log.debug("Found emp to update to {}", updateEmployee );
		return new ResponseEntity<>(updatedEmp, HttpStatus.OK);

	}
}

@Slf4j
@RestController
@RequestMapping(path = "api/v1/employees")
class EmployeeRestController{
	
	private static final AtomicInteger reqCount = new AtomicInteger(1);
	
	@Autowired
	IEmployeeService empSevice;

	public EmployeeRestController() {
		log.debug("#### {} created ", EmployeeRestController.class.getSimpleName());
	}
	
	@GetMapping
	public List<Employee> getAllEmployees() {
		log.debug("Got request# {} for getAllEmployees ", reqCount.getAndIncrement());
		return empSevice.findEmployees();
	}
	
	
	@GetMapping(path="/{id}")
	public Employee getEmpById(@PathVariable long id) {
		log.debug("Got request# {} for getEmpById ", reqCount.getAndIncrement());
		Optional<Employee> empOpt = empSevice.findEmpById(id);
		if(empOpt.isPresent()) {
			log.debug("Found emp {}", empOpt.get());
		}else {
			throw new EmployeeNotFoundException("404 Employee Not Found for id " + Long.toString(id));
		}
		return empOpt.get();
	}
	
	@PostMapping
	public Employee addEmployee(@RequestBody Employee newEmp) {
		log.debug("Got request# {} for addEmployee ", reqCount.getAndIncrement(), newEmp);
		return empSevice.addEmployee(newEmp);
	}
	
	@DeleteMapping("/{id}")
	public void deleteEmployee(@PathVariable long id) {
		log.debug("Got request# {} for deleteEmployee ", reqCount.getAndIncrement(), id);
		empSevice.findEmpById(id)
		.orElseThrow(() -> new EmployeeNotFoundException("404 Employee Not Found for id "));
		empSevice.deleteEmployee(id);
	}
	
	@PutMapping("/{id}")
	public void updateEmployee(@PathVariable Long id, @RequestBody Employee updatedEmp ) {
		log.debug("Got request# {} for updateEmployee with id {}", reqCount.getAndIncrement(), updatedEmp, id);
		empSevice.findEmpById(id)
			.orElseThrow(() -> new EmployeeNotFoundException("404 Employee Not Found for id " + Long.toString(id)));
		
		Employee updateEmployee = empSevice.updateEmployee(updatedEmp);
		log.debug("Found emp to update to {}", updateEmployee );

	}
}
	
