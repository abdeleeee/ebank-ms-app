package net.abdelmoghit.customerservice.repository;

import net.abdelmoghit.customerservice.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
